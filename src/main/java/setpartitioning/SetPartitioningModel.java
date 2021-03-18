package setpartitioning;

import gurobi.*;

import java.util.*;

public class SetPartitioningModel {

    public static void main(String[] args) {
        try {

            // Setting up parameters
            SetPartitioningParameters.makeParameters();
            List<List<Double>> costOfRouteForVessel = SetPartitioningParameters.costOfRouteForVessel;
            List<Double> costOfPostponedOrders = SetPartitioningParameters.costOfPostponedOrders;
            List<List<List<Double>>> orderInRouteForVessel = SetPartitioningParameters.orderInRouteForVessel;

            // Setting up dimensions
            int numberOfOrders = SetPartitioningParameters.orders.size();
            int numberOfVessels = orderInRouteForVessel.size();
            Map<Integer, Integer> vesselToNumberOfRoutes = new HashMap<>();
            for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
                vesselToNumberOfRoutes.put(vesselIdx, orderInRouteForVessel.get(vesselIdx).size());
            }

            // Setting up Gurobi environment
            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "SetPartitioningModel");

            // Variables
            List<List<GRBVar>> lambda = new ArrayList<>();
            for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
                lambda.add(new ArrayList<>());
                for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                    String name = "lambda_" + vesselIdx + "_" + routeIdx;
                    lambda.get(vesselIdx).add(model.addVar(0, 1, 0, GRB.BINARY, name));
                }
            }
            List<GRBVar> y = new ArrayList<>();
            for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
                String name = "y_" + orderIdx;
                y.add(model.addVar(0, 1, 0, GRB.BINARY, name));
            }

            // Objective
            GRBLinExpr objective = new GRBLinExpr();

            for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
                for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                    double routeCost = costOfRouteForVessel.get(vesselIdx).get(routeIdx);
                    objective.addTerm(routeCost, lambda.get(vesselIdx).get(routeIdx));
                }
            }

            for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
                double postponeCost = costOfPostponedOrders.get(orderIdx);
                objective.addTerm(postponeCost, y.get(orderIdx));
            }

            model.setObjective(objective, GRB.MINIMIZE);

            // Constraints
            for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
                GRBLinExpr lhs = new GRBLinExpr();
                for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                    lhs.addTerm(1, lambda.get(vesselIdx).get(routeIdx));
                }
                model.addConstr(lhs, GRB.LESS_EQUAL, 1, "Vessel " + (vesselIdx + 1));
            }

            for (int orderIdx = 0; orderIdx < numberOfOrders; orderIdx++) {
                GRBLinExpr lhs = new GRBLinExpr();
                for (int vesselIdx = 0; vesselIdx < numberOfVessels; vesselIdx++) {
                    for (int routeIdx = 0; routeIdx < vesselToNumberOfRoutes.get(vesselIdx); routeIdx++) {
                        double isOrderInVesselRoute = orderInRouteForVessel.get(vesselIdx).get(routeIdx).get(orderIdx);
                        lhs.addTerm(isOrderInVesselRoute, lambda.get(vesselIdx).get(routeIdx));
                    }
                }

                lhs.addTerm(1, y.get(orderIdx));
                model.addConstr(lhs, GRB.EQUAL, 1, "Order " + orderIdx);
            }

            // Optimize
            model.optimize();

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
                    status != GRB.Status.INFEASIBLE    ){
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

}
