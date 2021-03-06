package setpartitioning;

import alns.Evaluator;
import data.Parameters;
import data.Problem;
import objects.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Pool {

    public static Map<Integer, Map<List<Order>, Double>> vesselToVoyageToCost;  // Voyage is always feasible

    public static void initialize() {
        vesselToVoyageToCost = new ConcurrentHashMap<>();
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            vesselToVoyageToCost.put(vIdx, new ConcurrentHashMap<>());
        }
    }

    public static int getTotalPoolSize() {
        int size = 0;
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            size += vesselToVoyageToCost.get(vIdx).keySet().size();
        }
        return size;
    }

    public static int getVesselPoolSize(int vIdx) {
        return vesselToVoyageToCost.get(vIdx).keySet().size();
    }

    public static void saveVoyage(int vIdx, List<Order> orderSequence, double cost) {
        if (!Evaluator.isVoyageFeasible(orderSequence, vIdx)) return;
        if (vesselToVoyageToCost.get(vIdx).containsKey(orderSequence)) return;
        int vesselPoolSize = getVesselPoolSize(vIdx);
        if (vesselPoolSize > Parameters.vesselPoolSize) prunePool(vIdx, vesselPoolSize);
        vesselToVoyageToCost.get(vIdx).put(orderSequence, cost);
    }

    private static void prunePool(int vIdx, int vesselPoolSize) {
        List<List<Order>> keys = new ArrayList<>(vesselToVoyageToCost.get(vIdx).keySet());
        Collections.shuffle(keys, Problem.random);
        int pruneSize = vesselPoolSize / 2;
        if (pruneSize > keys.size()) return;
        keys.subList(0, pruneSize).forEach(vesselToVoyageToCost.get(vIdx).keySet()::remove);
    }
}
