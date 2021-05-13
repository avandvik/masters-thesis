package subproblem;

import data.Parameters;
import data.Problem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private static Map<Integer, Double> hashToCostCurrent;
    private static Map<Integer, List<Node>> hashToShortestPathCurrent;
    private static Map<Integer, Double> hashToCostLongTerm;
    private static Map<Integer, List<Node>> hashToShortestPathLongTerm;

    public static void initialize() {
        initializeCurrent();
        hashToCostLongTerm = new ConcurrentHashMap<>();
        hashToShortestPathLongTerm = new ConcurrentHashMap<>();
    }

    public static void initializeCurrent() {
        /* Call at the beginning or end of each ALNS iteration */
        hashToCostCurrent = new ConcurrentHashMap<>();
        hashToShortestPathCurrent = new ConcurrentHashMap<>();
    }

    public static boolean isCached(int hash) {
        return hashToCostCurrent.containsKey(hash) || hashToCostLongTerm.containsKey(hash);
    }

    public static double getCost(int hash) {
        /* Must only be called after a call to isCached returns true */
        Double costCurrent = hashToCostCurrent.get(hash);
        if (costCurrent != null) return costCurrent;
        return hashToCostLongTerm.get(hash);
    }

    public static List<Node> getShortestPath(int hash) {
        /* Must only be called after a call to isCached returns true */
        List<Node> shortestPathCurrent = hashToShortestPathCurrent.get(hash);
        if (shortestPathCurrent != null) return shortestPathCurrent;
        return hashToShortestPathLongTerm.get(hash);
    }

    public static int getCacheSize() {
        return hashToCostLongTerm.keySet().size();
    }

    public static void cacheCurrent(int hash, SubProblem subProblem) {
        hashToCostCurrent.put(hash, subProblem.getCost());
        hashToShortestPathCurrent.put(hash, subProblem.getShortestPath());
    }

    public static void cacheLongTerm() {
        /* Cache the (key, value) pairs in the current iteration's cache while not exceeding storage limits */
        if (hashToCostLongTerm.keySet().size() > Parameters.cacheSize) {
            List<Integer> hashes = new ArrayList<>(hashToCostLongTerm.keySet());
            Collections.shuffle(hashes, Problem.random);
            int mappingsToDelete = hashToCostCurrent.keySet().size();
            hashes.subList(0, mappingsToDelete).forEach(hashToCostLongTerm.keySet()::remove);
            hashes.subList(0, mappingsToDelete).forEach(hashToShortestPathLongTerm.keySet()::remove);
        }
        for (Map.Entry<Integer, Double> entry : hashToCostCurrent.entrySet()) {
            int hash = entry.getKey();
            double cost = entry.getValue();
            List<Node> shortestPath = hashToShortestPathCurrent.get(hash);
            hashToCostLongTerm.put(hash, cost);
            hashToShortestPathLongTerm.put(hash, shortestPath);
        }
        initializeCurrent();  // Can be done at this point as this is called at the end of an ALNS iteration
    }
}
