package subproblem;

import alns.Cache;
import data.Parameters;
import objects.Order;
import setpartitioning.Pool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblemInsertion extends SubProblem implements Runnable {

    private final int insertionIdx;
    private final Order orderToPlace;

    // orderToPlace -> (vesselIdx, insertionIdx) -> objective
    public static Map<Order, Map<List<Integer>, Double>> orderToInsertionToObjective;

    public SubProblemInsertion(List<Order> orderSequence, int vesselIdx, int insertionIdx, Order orderToPlace) {
        super(orderSequence, vesselIdx);
        this.insertionIdx = insertionIdx;
        this.orderToPlace = orderToPlace;
    }

    public static void initializeResultsStructure() {
        orderToInsertionToObjective = new ConcurrentHashMap<>();
    }

    public static void addToResultsStructure(Order orderToPlace, int vesselIdx, int insertionIdx, double cost) {
        List<Integer> insertionKey = new ArrayList<>(Arrays.asList(vesselIdx, insertionIdx));
        if (!orderToInsertionToObjective.containsKey(orderToPlace)) {
            orderToInsertionToObjective.put(orderToPlace, new HashMap<>());
        }
        orderToInsertionToObjective.get(orderToPlace).put(insertionKey, cost);
    }

    @Override
    public void run() {
        super.solveSubProblem();
        addToResultsStructure(this.orderToPlace, super.getVIdx(), this.insertionIdx, super.getCost());
        if (Parameters.cacheSP) Cache.cacheSequence(super.getVIdx(), super.getOrderSequence(), this);
        if (Parameters.setPartitioning) Pool.saveVoyage(super.getVIdx(), super.getOrderSequence(), super.getCost());
    }
}
