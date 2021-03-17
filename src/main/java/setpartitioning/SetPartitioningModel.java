package setpartitioning;

import gurobi.*;
import objects.Order;

import java.util.HashMap;
import java.util.Map;

public class SetPartitioningModel {

    public static void main(String[] args) {
        try {
            String[] Vessels =
                    new String[] {"Vessel 1", "Vessel 2", "Vessel 3"};

            String[] Orders =
                    new String[] {"Order 1", "Order 2", "Order 3", "Order 4", "Order 5"};

            double[][] costOfRouteForVessel = new double[][] {{10.0, 20.0, 30.0}, {10.0, 20.0, 30.0}, {10.0, 20.0, 30.0}};
            double[] costOfPostponingOrder = new double[] {100.0, 200.0, 300.0, 200.0, 100.0};
            int[][][] orderInVesselRoute = new int[][][] {{{1, 0, 1},{0, 1, 0},{0, 0, 0}},{{0, 0, 1},
                    {0, 1, 0},{0, 0, 0}},{{1, 0, 1},{0, 1, 0},{0, 0, 0}},{{1, 0, 1},{0, 1, 0},{0, 0, 0}},
                    {{1, 0, 1},{0, 1, 0},{0, 0, 0}}};

            int numberOfRoutes = 3;
            int numberOfVessels = 3;
            int numberOfOrders = 5;

            GRBEnv env = new GRBEnv();
            GRBModel model = new GRBModel(env);
            model.set(GRB.StringAttr.ModelName, "FirstSetPartModel");

            GRBVar[][] lambda = new GRBVar[numberOfVessels][numberOfRoutes];
            for (int v = 0; v < numberOfVessels; v++) {
                for (int r = 0; r < numberOfRoutes; r++) {
                    lambda[v][r] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "lambda");
                }
            }

            GRBVar[] y = new GRBVar[numberOfOrders];
            for (int o = 0; o < numberOfOrders; o++) {
                y[o] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
            }

            // Objective
            GRBLinExpr objective = new GRBLinExpr();

            for (int v = 0; v < numberOfVessels; v++) {
                for (int r = 0; r < numberOfRoutes; r++) {
                    objective.addTerm(costOfRouteForVessel[v][r], lambda[v][r]);
                }
            }

            for (int o = 0; o < numberOfOrders; o++) {
                objective.addTerm(costOfPostponingOrder[o], y[o]);
            }

            model.setObjective(objective, GRB.MINIMIZE);

            // Constraints
            for (int v = 0; v < numberOfVessels; v++) {
                GRBLinExpr lhs = new GRBLinExpr();
                for (int r = 0; r < numberOfRoutes; r++) {
                    lhs.addTerm(1.0, lambda[v][r]);
                }
                model.addConstr(lhs, GRB.LESS_EQUAL, 1.0, Vessels[v]);
            }

            for (int o = 0; o < numberOfOrders; o++) {
                GRBLinExpr lhs = new GRBLinExpr();
                for (int v = 0; v < numberOfVessels; v++) {
                    for (int r = 0; r < numberOfRoutes; r++) {
                        lhs.addTerm(orderInVesselRoute[o][v][r], lambda[v][r]);
                        lhs.addTerm(1.0, y[o]);
                    }
                }
                model.addConstr(lhs, GRB.EQUAL, 1.0, Orders[o]);
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
