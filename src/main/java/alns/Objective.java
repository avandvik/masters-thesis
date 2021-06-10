package alns;

import alns.heuristics.Construction;
import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.*;
import utils.Helpers;

import java.util.*;

public class Objective {

    public static double getOrderSequenceCost(List<Order> os, int vIdx) {
        /* If caching is on, the cache must contain the orderSequence, else this will return NaN */
        return os.isEmpty() ? 0.0 : Parameters.cacheSP ? Cache.getCost(vIdx, os) : runSP(os, vIdx);
    }

    public static void setObjValAndSchedule(Solution solution) {
        /* Calculates and sets the objective value of the solution and sets the corresponding vessel schedules */

        List<List<Node>> shortestPaths = new ArrayList<>();
        double objectiveValue = 0.0;
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            SubProblem subProblem = runSPComplete(solution.getOrderSequence(vesselIdx), vesselIdx);
            shortestPaths.add(subProblem != null ? subProblem.getShortestPath() : new ArrayList<>());
            objectiveValue += subProblem != null ? subProblem.getCost() : 0.0;
        }
        double postponementPenalty = penalizePostponement(solution);
        objectiveValue += postponementPenalty;
        solution.setShortestPaths(shortestPaths);
        solution.setObjective(objectiveValue);
    }

    public static double penalizePostponement(Solution solution) {
        return solution.getAllPostponed().stream()
                .map(Order::getPostponementPenalty)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public static SubProblem runSPComplete(List<Order> orderSequence, int vesselIdx) {
        /* Runs SubProblem and returns entire object to access all solution info (schedules) */
        if (orderSequence.isEmpty()) return null;
        if (Cache.isCached(vesselIdx, orderSequence)) {
            SubProblem subProblem = new SubProblem(orderSequence, vesselIdx);
            subProblem.setCost(Cache.getCost(vesselIdx, orderSequence));
            subProblem.setShortestPath(Cache.getShortestPath(vesselIdx, orderSequence));
            return subProblem;
        }
        SubProblem subProblem = new SubProblem(orderSequence, vesselIdx);
        SubProblem.initializeResultsStructure();
        subProblem.run();
        if (Parameters.cacheSP) Cache.cacheSequence(vesselIdx, orderSequence, subProblem);
        return subProblem;
    }

    public static double runSP(List<Order> orderSequence, int vesselIdx) {
        /* Run SubProblem and returns only objective value */
        if (orderSequence.isEmpty()) return 0.0;
        if (Cache.isCached(vesselIdx, orderSequence)) return Cache.getCost(vesselIdx, orderSequence);
        SubProblem subProblem = new SubProblem(orderSequence, vesselIdx);
        SubProblem.initializeResultsStructure();
        subProblem.run();
        if (Parameters.cacheSP) Cache.cacheSequence(vesselIdx, orderSequence, subProblem);
        return subProblem.getCost();
    }

    public static void runMultipleSPInsertion(List<List<Order>> orderSequences, Set<Order> ordersToPlace) {
        SubProblemInsertion.initializeResultsStructure();
        List<Thread> threads = new ArrayList<>();
        for (Order order : ordersToPlace) {
            Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
            for (int vesselIdx : insertions.keySet()) {
                for (int insertionIdx : insertions.get(vesselIdx)) {
                    List<Order> orderSequence = Helpers.deepCopyList(orderSequences.get(vesselIdx), true);
                    orderSequence.add(insertionIdx, order);  // This should never be empty
                    boolean solvedByCache = cacheInsertion(order, orderSequence, vesselIdx, insertionIdx);
                    if (solvedByCache) continue;
                    Thread thread = new Thread(new SubProblemInsertion(orderSequence, vesselIdx, insertionIdx, order));
                    threads.add(thread);
                    thread.start();
                }
            }
        }
        collect(threads);
        // Caching happens in the run method of SubProblemInsertion
    }

    private static boolean cacheInsertion(Order order, List<Order> orderSequence, int vesselIdx, int insertionIdx) {
        if (Cache.isCached(vesselIdx, orderSequence)){
            double cost = Cache.getCost(vesselIdx, orderSequence);
            SubProblemInsertion.addToResultsStructure(order, vesselIdx, insertionIdx, cost);
            return true;
        }
        return false;
    }

    public static void runMultipleSPRemoval(List<List<Order>> orderSequences) {
        if (orderSequences.size() > Problem.getNumberOfVessels()) throw new IllegalStateException();
        SubProblemRemoval.initializeResultsStructure();
        List<Thread> threads = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            for (int removalIdx = 0; removalIdx < orderSequence.size(); removalIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.remove(removalIdx);
                if (orderSequenceCopy.isEmpty()) {
                    List<Integer> key = new ArrayList<>(Arrays.asList(vesselIdx, removalIdx));
                    SubProblemRemoval.removalToObjective.put(key, 0.0);
                    continue;
                }
                boolean solvedByCache = cacheRemoval(orderSequenceCopy, vesselIdx, removalIdx);
                if (solvedByCache) continue;
                Thread thread = new Thread(new SubProblemRemoval(orderSequenceCopy, vesselIdx, removalIdx));
                threads.add(thread);
                thread.start();
            }
        }
        collect(threads);
        // Caching happens in the run method of SubProblemRemoval
    }

    private static boolean cacheRemoval(List<Order> orderSequence, int vesselIdx, int removalIdx) {
        if (Cache.isCached(vesselIdx, orderSequence)) {
            double cost = Cache.getCost(vesselIdx, orderSequence);
            SubProblemRemoval.addToResultsStructure(vesselIdx, removalIdx, cost);
            return true;
        }
        return false;
    }

    public static void runMultipleSPEvaluate(List<List<Order>> orderSequences) {
        if (orderSequences.size() > Problem.getNumberOfVessels()) throw new IllegalStateException();
        SubProblem.initializeResultsStructure();
        List<Thread> threads = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            if (orderSequence.isEmpty()) {
                SubProblem.vesselToObjective.put(vesselIdx, 0.0);
                continue;
            }
            if (cacheEvaluate(orderSequence, vesselIdx)) continue;
            Thread thread = new Thread(new SubProblem(orderSequence, vesselIdx));
            threads.add(thread);
            thread.start();
        }
        collect(threads);
        // Caching happens in the run method of SubProblem
    }

    private static boolean cacheEvaluate(List<Order> orderSequence, int vesselIdx) {
        if (Cache.isCached(vesselIdx, orderSequence)) {
            double cost = Cache.getCost(vesselIdx, orderSequence);
            SubProblem.addToResultsStructure(vesselIdx, cost);
            return true;
        }
        return false;
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
