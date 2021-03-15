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
            SubProblem subProblem = runSPComplete(solution.getOrderSequence(vesselNumber), vesselNumber);
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

    public static SubProblem runSPComplete(List<Order> orderSequence, int vesselNumber) {
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

    public static double runSPLean(List<Order> orderSequence, int vesselNumber) {
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

    public static Map<Integer, List<Double>> runMultipleSPs(Map<Integer, List<List<Order>>> vesselToOrderSequences) {
        SubProblem.initializeParallelRuns();
        List<Thread> threads = new ArrayList<>();
        for (int vesselIdx : vesselToOrderSequences.keySet()) {
            for (List<Order> orderSequence : vesselToOrderSequences.get(vesselIdx)) {
                if (orderSequence.isEmpty()) {
                    SubProblem.sharedObjectiveValues.put(vesselIdx, new ArrayList<>());
                } else {
                    Thread thread = new Thread(new SubProblem(orderSequence, vesselIdx));
                    threads.add(thread);
                    thread.start();
                }
            }
        }
        collectThreads(threads);
        return SubProblem.sharedObjectiveValues;
    }

    private static void collectThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
