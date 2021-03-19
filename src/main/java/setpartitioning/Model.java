package setpartitioning;

import alns.Main;
import data.Parameters;
import data.Problem;
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
        GRBEnv env = new GRBEnv();
        this.model = new GRBModel(env);
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

    private void printSolution() throws GRBException {
        printChosenRoutes();
        printPostponedOrders();
    }

    private void printChosenRoutes() throws GRBException {
        for (int vesselIdx = 0; vesselIdx < this.lambda.size(); vesselIdx++) {
            for (int routeIdx = 0; routeIdx < this.lambda.get(vesselIdx).size(); routeIdx++) {
                GRBVar routeByVessel = this.lambda.get(vesselIdx).get(routeIdx);
                int value = (int) routeByVessel.get(GRB.DoubleAttr.X);
                if (value == 1) {
                    System.out.println("Sailing route: " + Data.routeArray.get(vesselIdx).get(routeIdx));
                }
            }
        }
    }

    private void printPostponedOrders() throws GRBException {
        for (int orderIdx = 0; orderIdx < this.y.size(); orderIdx++) {
            GRBVar postponeOrder = this.y.get(orderIdx);
            int value = (int) postponeOrder.get(GRB.DoubleAttr.X);
            if (value == 1) {
                System.out.println("Postponing order: " + Data.orders.get(orderIdx));
            }
        }
    }


    public void run() {
        try {

            this.setUpProblem();
            this.setUpEnvironment();

            // Variables
            this.defineLambda();
            this.defineY();

            // Objective
            this.defineObjective();

            // Constraints
            this.defineSetPartitioningConstr();
            this.defineOrderAssignmentConstr();
            this.defineMandOrderConstr();

            // Optimize
            model.optimize();

            if (Parameters.verbose) printSolution();

            int status = model.get(GRB.IntAttr.Status);
            if (status == GRB.Status.UNBOUNDED) {
                System.out.println("The model cannot be solved "
                        + "because it is unbounded");
                return;
            }
            if (status == GRB.Status.OPTIMAL) {
                System.out.println("The optimal objective is " +
                        model.get(GRB.DoubleAttr.ObjVal));
                return;
            }
            if (status != GRB.Status.INF_OR_UNBD &&
                    status != GRB.Status.INFEASIBLE) {
                System.out.println("Optimization was stopped with status " + status);
                return;
            }


            // Compute IIS
            System.out.println("The model is infeasible; computing IIS");
            model.computeIIS();
            System.out.println("\nThe following constraint(s) "
                    + "cannot be satisfied:");
            for (GRBConstr c : model.getConstrs()) {
                if (c.get(GRB.IntAttr.IISConstr) == 1) {
                    System.out.println(c.get(GRB.StringAttr.ConstrName));
                }
            }

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }

    public static void main(String[] args) {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Parameters.verbose = false;
        Parameters.totalIterations = 40;
        Parameters.maxIterSolution = 20;
        Parameters.noiseRate = 0.5;
        Main.run();
        Model model = new Model();
        model.run();
    }
}
