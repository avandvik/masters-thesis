package subproblem;

import alns.Cache;
import data.Parameters;
import objects.Order;
import setpartitioning.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblemRemoval extends SubProblem implements Runnable {

    private final int removalIdx;

    public static Map<List<Integer>, Double> removalToObjective;

    public SubProblemRemoval(List<Order> orderSequence, int vesselIdx, int removalIdx) {
        super(orderSequence, vesselIdx);
        this.removalIdx = removalIdx;
    }

    public static void initializeResultsStructure() {
        removalToObjective = new ConcurrentHashMap<>();
    }

    public static void addToResultsStructure(int vesselIdx, int removalIdx, double cost) {
        List<Integer> removalKey = new ArrayList<>(Arrays.asList(vesselIdx, removalIdx));
        removalToObjective.put(removalKey, cost);
    }

    @Override
    public void run() {
        super.solveSubProblem();
        addToResultsStructure(super.getVIdx(), this.removalIdx, super.getCost());
        if (Parameters.cacheSP) Cache.cacheSequence(super.getVIdx(), super.getOrderSequence(), this);
        if (Parameters.setPartitioning) Pool.saveVoyage(super.getVIdx(), super.getOrderSequence(), super.getCost());
    }
}
