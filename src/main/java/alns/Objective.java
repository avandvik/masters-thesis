package alns;

import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;

import java.util.*;

public class Objective {

    public static void setObjValAndSchedule(Solution solution) {
        /* Calculates and sets the objective value of the solution and sets the corresponding vessel schedules */
        List<List<Node>> shortestPaths = new ArrayList<>();
        double objectiveValue = 0.0;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            SubProblem subProblem = runSubProblemComplete(solution.getOrderSequence(vesselNumber), vesselNumber);
            shortestPaths.add(subProblem != null ? subProblem.getShortestPath() : new ArrayList<>());
            objectiveValue += subProblem != null ? subProblem.getShortestPathCost() : 0.0;
        }

        double postponementPenalty = penalizePostponement(solution);
        objectiveValue += postponementPenalty;

        solution.setShortestPaths(shortestPaths);
        solution.setFitness(objectiveValue);
    }

    public static double penalizePostponement(Solution solution) {
        return solution.getPostponedOrders().stream()
                .map(o -> o.getSize() * o.getPostponementPenalty())
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public static SubProblem runSubProblemComplete(List<Order> orderSequence, int vesselNumber) {
        /* Runs SubProblem and returns entire object to access all solution info (schedules) */
        try {
            SubProblem subProblem = new SubProblem(orderSequence, vesselNumber);
            subProblem.solve();
            return subProblem;
        } catch (IllegalArgumentException e) {
            // System.out.println(e.getMessage());
        }
        return null;
    }

    public static double runSubProblemLean(List<Order> orderSequence, int vesselNumber) {
        /* Run SubProblem and returns only objective value */
        try {
            SubProblem subProblem = new SubProblem(orderSequence, vesselNumber);
            subProblem.solve();
            return subProblem.getShortestPathCost();
        } catch (IllegalArgumentException e) {
            // System.out.println(e.getMessage());
        }
        return 0.0;
    }

    public static double runSubProblemLean(List<List<Order>> orderSequences) {
        double obj = 0.0;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            obj += runSubProblemLean(orderSequences.get(vesselNumber), vesselNumber);
        }
        return obj;
    }
}
