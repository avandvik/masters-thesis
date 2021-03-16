package subproblem;

import objects.Order;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblemInsertion extends SubProblem implements Runnable{

    private final int insertionIdx;
    private final Order orderToPlace;

    // orderToPlace -> (vesselIdx, insertionIdx) -> objective
    public static Map<Order, Map<List<Integer>, Double>> orderToInsertionToObjective;

    public SubProblemInsertion(List<Order> orderSequence, int vesselIdx, int insertionIdx, Order orderToPlace) {
        super(orderSequence, vesselIdx);
        this.insertionIdx = insertionIdx;
        this.orderToPlace = orderToPlace;
    }

    public static void initialize() {
        orderToInsertionToObjective = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        boolean cachedSolution = super.checkCache(this.hashCode());
        if (!cachedSolution) {
            Tree tree = new Tree();
            tree.generateTree(super.getOrderSequence(), super.isSpotVessel());
            super.setShortestPath(tree.findShortestPath());
            super.setShortestPathCost(tree.getGlobalBestCost());
        }
        List<Integer> insertionKey = new ArrayList<>(Arrays.asList(super.getVesselIdx(), this.insertionIdx));
        if (!orderToInsertionToObjective.containsKey(this.orderToPlace)) {
            orderToInsertionToObjective.put(this.orderToPlace, new HashMap<>());
        }
        orderToInsertionToObjective.get(this.orderToPlace).put(insertionKey, super.getShortestPathCost());
    }
}