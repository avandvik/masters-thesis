package subproblem;

import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblem implements Runnable {

    private final List<Order> orderSequence;
    private final int vesselIdx;
    private final boolean isSpotVessel;
    private List<Node> shortestPath;
    private double shortestPathCost;

    private int insertionIdx;
    private Order orderToPlace;

    // orderToPlace -> (vesselIdx, insertionIdx) -> objective
    public static Map<Order, Map<List<Integer>, Double>> sharedObjectiveValues;

    // Cache
    private final static Map<Integer, Double> hashToCost = new HashMap<>();
    private final static Map<Integer, List<Node>> hashToShortestPath = new HashMap<>();

    public SubProblem(List<Order> orderSequence, int vesselIdx) throws IllegalArgumentException {
        isOrderSequenceValid(orderSequence);
        isVesselIdxValid(vesselIdx);
        this.orderSequence = orderSequence;
        this.vesselIdx = vesselIdx;
        this.isSpotVessel = Problem.isSpotVessel(vesselIdx);
    }

    public SubProblem(List<Order> orderSequence, int vesselIdx, int insertionIdx, Order order) throws IllegalArgumentException {
        isOrderSequenceValid(orderSequence);
        isVesselIdxValid(vesselIdx);
        this.orderSequence = orderSequence;
        this.vesselIdx = vesselIdx;
        this.insertionIdx = insertionIdx;
        this.orderToPlace = order;
        this.isSpotVessel = Problem.isSpotVessel(vesselIdx);
    }

    public void solve() {
        int hash = Helpers.generateSubProblemHash(this.orderSequence, this.isSpotVessel);
        if (hashToCost.containsKey(hash)) {
            this.shortestPath = hashToShortestPath.get(hash);
            this.shortestPathCost = hashToCost.get(hash);
        } else {
            Tree tree = new Tree();
            tree.generateTree(this.orderSequence, this.isSpotVessel);
            this.shortestPath = tree.findShortestPath();
            hashToShortestPath.put(hash, this.shortestPath);
            this.shortestPathCost = tree.getGlobalBestCost();
            hashToCost.put(hash, this.shortestPathCost);
        }
    }

    @Override
    public void run() {
        Tree tree = new Tree();
        tree.generateTree(this.orderSequence, this.isSpotVessel);
        tree.findShortestPath();
        double cost = tree.getGlobalBestCost();

        List<Integer> insertionKey = new ArrayList<>(Arrays.asList(vesselIdx, insertionIdx));

        if (!sharedObjectiveValues.containsKey(this.orderToPlace)) {
            sharedObjectiveValues.put(this.orderToPlace, new HashMap<>());
        }
        sharedObjectiveValues.get(this.orderToPlace).put(insertionKey, cost);
    }

    public static void initializeParallelRuns() {
        sharedObjectiveValues = new ConcurrentHashMap<>();
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public double getShortestPathCost() {
        return shortestPathCost;
    }

    private void isOrderSequenceValid(List<Order> orderSequence) throws IllegalArgumentException {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException("Empty order sequence passed to SubProblem, " +
                "skipping.");

        // Can be expanded
    }

    private void isVesselIdxValid(int vesselNumber) throws IllegalArgumentException {
        if (vesselNumber < 0 || vesselNumber >= Problem.getNumberOfVessels()) throw new IllegalArgumentException(
                "Invalid vesselNumber passed to SubProblem.");
    }
}
