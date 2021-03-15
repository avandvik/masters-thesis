package alns;

import alns.heuristics.Construction;
import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;
import subproblem.SubProblemInsertion;
import utils.Helpers;

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
            SubProblem.initialize();
            subProblem.run();
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
            SubProblem.initialize();
            subProblem.run();
            return subProblem.getShortestPathCost();
        } catch (IllegalArgumentException e) {
            // System.out.println(e.getMessage());
        }
        return 0.0;
    }

    public static void runMultipleSPInsertion(List<List<Order>> orderSequences, Set<Order> ordersToPlace) {
        SubProblemInsertion.initialize();
        List<Thread> threads = new ArrayList<>();
        for (Order order : ordersToPlace) {
            Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
            for (int vesselIdx : insertions.keySet()) {
                for (int insertionIdx : insertions.get(vesselIdx)) {
                    List<Order> orderSequence = Helpers.deepCopyList(orderSequences.get(vesselIdx), true);
                    orderSequence.add(insertionIdx, order);
                    Thread thread = new Thread(new SubProblemInsertion(orderSequence, vesselIdx, insertionIdx, order));
                    threads.add(thread);
                    thread.start();
                }
            }
        }
        collect(threads);
    }

    public static void runMultipleSPEvaluate(List<List<Order>> orderSequences) {
        SubProblem.initialize();
        List<Thread> threads = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < orderSequences.size(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            if (orderSequence.isEmpty()) {
                SubProblem.vesselToObjective.put(vesselIdx, 0.0);
                continue;
            }
            Thread thread = new Thread(new SubProblem(orderSequence, vesselIdx));
            threads.add(thread);
            thread.start();
        }
        collect(threads);
    }

    private static void collect(List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
