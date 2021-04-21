package alns;

import alns.heuristics.Construction;
import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;
import subproblem.SubProblemInsertion;
import subproblem.SubProblemRemoval;
import utils.Helpers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Objective {

    // Cache
    public static Map<Integer, Double> hashToCost;
    public static Map<Integer, List<Node>> hashToShortestPath;

    public static void initializeCache() {
        hashToCost = new ConcurrentHashMap<>();
        hashToShortestPath = new ConcurrentHashMap<>();
    }

    private static boolean isCached(int hash) {
        return hashToCost.containsKey(hash);
    }

    public static void cacheSubProblemResults(int hash, SubProblem subProblem) {
        hashToCost.put(hash, subProblem.getShortestPathCost());
        hashToShortestPath.put(hash, subProblem.getShortestPath());
    }

    public static void setObjValAndSchedule(Solution solution) {
        /* Calculates and sets the objective value of the solution and sets the corresponding vessel schedules */

        List<List<Node>> shortestPaths = new ArrayList<>();
        double objectiveValue = 0.0;
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            SubProblem subProblem = runSPComplete(solution.getOrderSequence(vesselIdx), vesselIdx);
            shortestPaths.add(subProblem != null ? subProblem.getShortestPath() : new ArrayList<>());
            objectiveValue += subProblem != null ? subProblem.getShortestPathCost() : 0.0;
        }

        double postponementPenalty = penalizePostponement(solution);
        objectiveValue += postponementPenalty;

        solution.setShortestPaths(shortestPaths);
        solution.setObjective(objectiveValue);
    }

    public static double penalizePostponement(Solution solution) {
        return solution.getPostponedOrders().stream()
                .map(Order::getPostponementPenalty)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public static SubProblem runSPComplete(List<Order> orderSequence, int vesselIdx) {
        /* Runs SubProblem and returns entire object to access all solution info (schedules) */

        if (orderSequence.isEmpty()) return null;

        int hash = SubProblem.getSubProblemHash(orderSequence, vesselIdx);
        if (isCached(hash)) {
            SubProblem subProblem = new SubProblem(orderSequence, vesselIdx);
            subProblem.setShortestPath(hashToShortestPath.get(hash));
            subProblem.setShortestPathCost(hashToCost.get(hash));
            return subProblem;
        }

        SubProblem subProblem = new SubProblem(orderSequence, vesselIdx);
        SubProblem.initializeResultsStructure();
        subProblem.run();

        cacheSubProblemResults(hash, subProblem);

        return subProblem;
    }

    public static double runSPLean(List<Order> orderSequence, int vesselIdx) {
        /* Run SubProblem and returns only objective value */

        if (orderSequence.isEmpty()) return 0.0;

        int hash = SubProblem.getSubProblemHash(orderSequence, vesselIdx);
        if (isCached(hash)) return hashToCost.get(hash);

        SubProblem subProblem = new SubProblem(orderSequence, vesselIdx);
        SubProblem.initializeResultsStructure();
        subProblem.run();

        cacheSubProblemResults(hash, subProblem);

        return subProblem.getShortestPathCost();
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
        int hash = SubProblem.getSubProblemHash(orderSequence, vesselIdx);
        if (isCached(hash)) {
            double cost = hashToCost.get(hash);
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
        int hash = SubProblem.getSubProblemHash(orderSequence, vesselIdx);
        if (isCached(hash)) {
            double cost = hashToCost.get(hash);
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

            boolean solvedByCache = cacheEvaluate(orderSequence, vesselIdx);
            if (solvedByCache) continue;

            Thread thread = new Thread(new SubProblem(orderSequence, vesselIdx));
            threads.add(thread);
            thread.start();
        }
        collect(threads);
        // Caching happens in the run method of SubProblem
    }

    private static boolean cacheEvaluate(List<Order> orderSequence, int vesselIdx) {
        int hash = SubProblem.getSubProblemHash(orderSequence, vesselIdx);
        if (isCached(hash)) {
            double cost = hashToCost.get(hash);
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
