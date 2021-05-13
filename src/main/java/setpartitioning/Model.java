package setpartitioning;

import alns.Solution;
import data.Parameters;
import data.Problem;
import gurobi.*;
import objects.Installation;
import objects.Order;

import java.util.*;

public class Model {

    private GRBModel model;

    private List<List<GRBVar>> y;
    private List<GRBVar> z;

    private Solution newSolution;

    public Solution getNewSolution() {
        return this.newSolution;
    }

    private void setUpProblem() {
        Data.makeConstants();
        Data.makeArrays();
    }

    private void setUpEnvironment() throws GRBException {
        this.model = new GRBModel(Data.gurobiEnv);
        model.set(GRB.StringAttr.ModelName, "SetPartitioningModel");
    }

    private void defineY() throws GRBException {
        this.y = new ArrayList<>();
        for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
            this.y.add(new ArrayList<>());
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                String name = "y_" + vIdx + "_" + voyageIdx;
                this.y.get(vIdx).add(model.addVar(0, 1, 0, GRB.BINARY, name));
            }
        }
    }

    private void defineZ() throws GRBException {
        this.z = new ArrayList<>();
        for (int orderIdx = 0; orderIdx < Data.nbrOrders; orderIdx++) {
            String name = "z_" + orderIdx;
            this.z.add(model.addVar(0, 1, 0, GRB.BINARY, name));
        }
    }

    private void defineObjective() throws GRBException {
        GRBLinExpr objective = new GRBLinExpr();
        for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                double voyageCost = Data.costOfVoyageForVessel.get(vIdx).get(voyageIdx);
                objective.addTerm(voyageCost, this.y.get(vIdx).get(voyageIdx));
            }
        }
        for (int orderIdx = 0; orderIdx < Data.nbrOrders; orderIdx++) {
            double postponeCost = Data.ordersPostponeCost.get(orderIdx);
            objective.addTerm(postponeCost, this.z.get(orderIdx));
        }
        this.model.setObjective(objective, GRB.MINIMIZE);
    }

    private void defineSetPartitioningConstr() throws GRBException {
        for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                lhs.addTerm(1, this.y.get(vIdx).get(voyageIdx));
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "Vessel " + (vIdx + 1));
        }
    }

    private void defineNoSplitServicingConstr() throws GRBException {
        for (int instIdx = 0; instIdx < Data.installationsWithODOP.size(); instIdx++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
                for (int voyageIdx : Data.voyageODOPArray.get(instIdx).get(vIdx)) {
                    lhs.addTerm(1, this.y.get(vIdx).get(voyageIdx));
                }
            }
            Installation inst = Data.installationsWithODOP.get(instIdx);
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "Installation " + (inst.getId()));
        }
    }

    private void defineOrderAssignmentConstr() throws GRBException {
        for (int orderIdx = 0; orderIdx < Data.nbrOrders; orderIdx++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
                for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                    double isOrderInVesselRoute = Data.orderInVoyageForVessel.get(vIdx).get(voyageIdx).get(orderIdx);
                    lhs.addTerm(isOrderInVesselRoute, this.y.get(vIdx).get(voyageIdx));
                }
            }
            lhs.addTerm(1, z.get(orderIdx));
            model.addConstr(lhs, GRB.EQUAL, 1, "Order " + orderIdx);
        }
    }

    private void defineMandOrderConstr() throws GRBException {
        for (int orderIdx = 0; orderIdx < Data.nbrOrders; orderIdx++) {
            Order order = Data.orders.get(orderIdx);
            if (order.isMandatory()) {
                GRBLinExpr lhs = new GRBLinExpr();
                lhs.addTerm(1, z.get(orderIdx));
                model.addConstr(lhs, GRB.EQUAL, 0, "Mand order " + orderIdx);
            }
        }
    }

    private Solution postprocess() throws GRBException {
        List<List<Order>> orderSequences = this.postprocessChosenRoutes();
        Set<Order> postponedOrders = this.postprocessPostponedOrders();
        return new Solution(orderSequences, postponedOrders, true);
    }

    private List<List<Order>> postprocessChosenRoutes() throws GRBException {
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
            orderSequences.add(new LinkedList<>());
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                GRBVar routeByVessel = this.y.get(vIdx).get(voyageIdx);
                int value = routeByVessel.get(GRB.DoubleAttr.X) > 0.5 ? 1 : 0;
                if (value == 1) {
                    List<Order> voyage = Data.voyageArray.get(vIdx).get(voyageIdx);
                    if (Parameters.verbose) System.out.println("Sailing route: " + voyage);
                    orderSequences.set(vIdx, voyage);
                }
            }
        }
        return orderSequences;
    }

    private Set<Order> postprocessPostponedOrders() throws GRBException {
        Set<Order> postponedOrders = new HashSet<>();
        for (int orderIdx = 0; orderIdx < this.z.size(); orderIdx++) {
            GRBVar postponeOrder = this.z.get(orderIdx);
            int value = postponeOrder.get(GRB.DoubleAttr.X) > 0.5 ? 1 : 0;
            if (value == 1) {
                Order postponedOrder = Data.orders.get(orderIdx);
                if (Parameters.verbose) System.out.println("Postponing order: " + postponedOrder);
                postponedOrders.add(postponedOrder);
            }
        }
        return postponedOrders;
    }

    private void debugVoyagePool() {
        for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
            System.out.println(Problem.getVessel(vIdx));
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                List<Order> voyage = Data.vesselToVoyageIdxToVoyage.get(vIdx).get(voyageIdx);
                System.out.println("\t\tvoyageIdx " + voyageIdx + ": " + voyage);
            }
        }
    }

    private void debugSplitVoyagePool() {
        for (int instIdx = 0; instIdx < Data.installationsWithODOP.size(); instIdx++) {
            System.out.println(Data.installationsWithODOP.get(instIdx));
            for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
                System.out.println("\t" + Problem.getVessel(vIdx));
                for (int voyageIdx : Data.voyageODOPArray.get(instIdx).get(vIdx)) {
                    List<Order> voyage = Data.vesselToVoyageIdxToVoyage.get(vIdx).get(voyageIdx);
                    System.out.println("\t\tvoyageIdx " + voyageIdx + ": " + voyage);
                }
            }
        }
    }

    private void debugChosenVoyages(List<List<Order>> orderSequences, Set<Order> postponedOrders) {
        for (int vIdx = 0; vIdx < Data.nbrVessels; vIdx++) {
            System.out.println(Problem.getVessel(vIdx) + ": " + orderSequences.get(vIdx));
        }
        System.out.println("postponedOrders: " + postponedOrders);
    }

    public void run() {
        try {
            this.setUpProblem();
            this.setUpEnvironment();
            this.defineY();
            this.defineZ();
            this.defineObjective();
            this.defineSetPartitioningConstr();
            this.defineNoSplitServicingConstr();
            this.defineOrderAssignmentConstr();
            this.defineMandOrderConstr();
            model.optimize();
            this.newSolution = postprocess();
        } catch (GRBException e) {
            System.out.println(this.newSolution);
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }
}
