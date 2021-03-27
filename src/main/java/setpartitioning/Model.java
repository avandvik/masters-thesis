package setpartitioning;

import alns.Solution;
import data.Parameters;
import gurobi.*;
import objects.Order;

import java.util.*;

public class Model {

    private GRBModel model;

    private int numberOfOrders;
    private int numberOfVessels;
    private Map<Integer, Integer> vesselToNumberOfRoutes;

    private List<List<GRBVar>> lambda;
    private List<GRBVar> y;

    private Solution newSolution;

    public Solution getNewSolution() {
        return this.newSolution;
    }

    private void setUpProblem() {
        Data.makeArrays();

        this.numberOfOrders = Data.orders.size();
        this.numberOfVessels = Data.orderInRouteForVessel.size();

        this.vesselToNumberOfRoutes = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
            this.vesselToNumberOfRoutes.put(vesselIdx, Data.orderInRouteForVessel.get(vesselIdx).size());
        }
    }

    private void setUpEnvironment() throws GRBException {
        this.model = new GRBModel(Data.gurobiEnv);
        model.set(GRB.StringAttr.ModelName, "SetPartitioningModel");
    }

    private void defineLambda() throws GRBException {
        this.lambda = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
            this.lambda.add(new ArrayList<>());
            for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                String name = "lambda_" + vesselIdx + "_" + routeIdx;
                this.lambda.get(vesselIdx).add(model.addVar(0, 1, 0, GRB.BINARY, name));
            }
        }
    }

    private void defineY() throws GRBException {
        this.y = new ArrayList<>();
        for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
            String name = "y_" + orderIdx;
            this.y.add(model.addVar(0, 1, 0, GRB.BINARY, name));
        }
    }

    private void defineObjective() throws GRBException {
        GRBLinExpr objective = new GRBLinExpr();
        for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
            for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                double routeCost = Data.costOfRouteForVessel.get(vesselIdx).get(routeIdx);
                objective.addTerm(routeCost, this.lambda.get(vesselIdx).get(routeIdx));
            }
        }
        for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
            double postponeCost = Data.costOfPostponedOrders.get(orderIdx);
            objective.addTerm(postponeCost, this.y.get(orderIdx));
        }
        this.model.setObjective(objective, GRB.MINIMIZE);
    }

    private void defineSetPartitioningConstr() throws GRBException {
        for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                lhs.addTerm(1, this.lambda.get(vesselIdx).get(routeIdx));
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "Vessel " + (vesselIdx + 1));
        }
    }

    private void defineOrderAssignmentConstr() throws GRBException {
        for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
                for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                    double isOrderInVesselRoute =
                            Data.orderInRouteForVessel.get(vesselIdx).get(routeIdx).get(orderIdx);
                    lhs.addTerm(isOrderInVesselRoute, this.lambda.get(vesselIdx).get(routeIdx));
                }
            }
            lhs.addTerm(1, y.get(orderIdx));
            model.addConstr(lhs, GRB.EQUAL, 1, "Order " + orderIdx);
        }
    }

    private void defineMandOrderConstr() throws GRBException {
        for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
            Order order = Data.orders.get(orderIdx);
            if (order.isMandatory()) {
                GRBLinExpr lhs = new GRBLinExpr();
                lhs.addTerm(1, y.get(orderIdx));
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
        for (int vesselIdx = 0; vesselIdx < this.lambda.size(); vesselIdx++) {
            orderSequences.add(new LinkedList<>());
            for (int routeIdx = 0; routeIdx < this.lambda.get(vesselIdx).size(); routeIdx++) {
                GRBVar routeByVessel = this.lambda.get(vesselIdx).get(routeIdx);
                int value = (int) routeByVessel.get(GRB.DoubleAttr.X);
                if (value == 1) {
                    List<Order> route = Data.routeArray.get(vesselIdx).get(routeIdx);
                    if (Parameters.verbose) System.out.println("Sailing route: " + route);
                    orderSequences.set(vesselIdx, route);
                }
            }
        }
        return orderSequences;
    }

    private Set<Order> postprocessPostponedOrders() throws GRBException {
        Set<Order> postponedOrders = new HashSet<>();
        for (int orderIdx = 0; orderIdx < this.y.size(); orderIdx++) {
            GRBVar postponeOrder = this.y.get(orderIdx);
            int value = (int) postponeOrder.get(GRB.DoubleAttr.X);
            if (value == 1) {
                Order postponedOrder = Data.orders.get(orderIdx);
                if (Parameters.verbose) System.out.println("Postponing order: " + postponedOrder);
                postponedOrders.add(postponedOrder);
            }
        }
        return postponedOrders;
    }

    public void run() {
        try {
            this.setUpProblem();
            this.setUpEnvironment();
            this.defineLambda();
            this.defineY();
            this.defineObjective();
            this.defineSetPartitioningConstr();
            this.defineOrderAssignmentConstr();
            this.defineMandOrderConstr();

            model.optimize();

            this.newSolution = postprocess();

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }
}
