package alns;

import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    public static Map<Integer, Map<List<Order>, Double>> vesselToSequenceToCost;  // Sequence may not be feasible
    public static Map<Integer, Map<List<Order>, List<Node>>> vesselToSequenceToShortestPath;

    public static void initialize() {
        vesselToSequenceToCost = new ConcurrentHashMap<>();
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            vesselToSequenceToCost.put(vIdx, new ConcurrentHashMap<>());
        }
        vesselToSequenceToShortestPath = new ConcurrentHashMap<>();
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            vesselToSequenceToShortestPath.put(vIdx, new ConcurrentHashMap<>());
        }
    }

    public static boolean isCached(int vIdx, List<Order> orderSequence) {
        /* Both caches must contain the keys */
        return vesselToSequenceToCost.get(vIdx).containsKey(orderSequence)
                && vesselToSequenceToShortestPath.get(vIdx).containsKey(orderSequence);
    }

    public static double getCost(int vIdx, List<Order> orderSequence) {
        /* Must only be called after a call to isCached returns true */
        return vesselToSequenceToCost.get(vIdx).get(orderSequence);
    }

    public static List<Node> getShortestPath(int vIdx, List<Order> orderSequence) {
        /* Must only be called after a call to isCached returns true */
        return vesselToSequenceToShortestPath.get(vIdx).get(orderSequence);
    }

    public static int getTotalCacheSize() {
        int size = 0;
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            size += vesselToSequenceToCost.get(vIdx).keySet().size();
        }
        return size;
    }

    public static int getVesselCacheSize(int vIdx) {
        return vesselToSequenceToCost.get(vIdx).keySet().size();
    }

    public static void cacheSequence(int vIdx, List<Order> orderSequence, SubProblem subProblem) {
        if (vesselToSequenceToCost.get(vIdx).containsKey(orderSequence)) return;
        if (vesselToSequenceToShortestPath.get(vIdx).containsKey(orderSequence)) return;
        vesselToSequenceToCost.get(vIdx).put(orderSequence, subProblem.getCost());
        vesselToSequenceToShortestPath.get(vIdx).put(orderSequence, subProblem.getShortestPath());
    }
}
