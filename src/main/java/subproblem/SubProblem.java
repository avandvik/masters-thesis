package subproblem;

import data.Messages;
import data.Problem;
import objects.Order;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblem implements Runnable {

    private final List<Order> orderSequence;
    private final int vesselIdx;
    private final boolean isSpotVessel;
    private List<Node> shortestPath;
    private double shortestPathCost;

    // vesselIdx -> objective value
    public static Map<Integer, Double> vesselToObjective;

    // Cache
    public static Map<Integer, Double> hashToCost;
    public static Map<Integer, List<Node>> hashToShortestPath;

    public SubProblem(List<Order> orderSequence, int vesselIdx) {
        isOrderSequenceValid(orderSequence);
        isVesselIdxValid(vesselIdx);
        this.orderSequence = orderSequence;
        this.vesselIdx = vesselIdx;
        this.isSpotVessel = Problem.isSpotVessel(vesselIdx);
    }

    public static void initializeCache() {
        hashToCost = new HashMap<>();
        hashToShortestPath = new HashMap<>();
    }

    public static void initializeResultsStructure() {
        vesselToObjective = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        this.solveSubProblem();
        vesselToObjective.put(vesselIdx, this.shortestPathCost);
    }

    public void solveSubProblem() {
        boolean cachedSolution = checkCache(this.hashCode());
        if (!cachedSolution) {
            Tree tree = new Tree();
            tree.generateTree(this.orderSequence, this.isSpotVessel);
            this.shortestPath = tree.findShortestPath();
            hashToShortestPath.put(this.hashCode(), this.shortestPath);
            this.shortestPathCost = tree.getGlobalBestCost();
            hashToCost.put(this.hashCode(), this.shortestPathCost);
        }
    }

    private boolean checkCache(int hash) {
        if (hashToCost.containsKey(hash)) {
            this.shortestPath = hashToShortestPath.get(hash);
            this.shortestPathCost = hashToCost.get(hash);
            return true;
        }
        return false;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public double getShortestPathCost() {
        return shortestPathCost;
    }

    public List<Order> getOrderSequence() {
        return orderSequence;
    }

    public int getVesselIdx() {
        return vesselIdx;
    }

    private void isOrderSequenceValid(List<Order> orderSequence) {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException(Messages.emptySequenceSP);
    }

    private void isVesselIdxValid(int vesselIdx) {
        if (vesselIdx < 0 || vesselIdx >= Problem.getNumberOfVessels())
            throw new IllegalArgumentException(Messages.invalidVesselIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSequence, isSpotVessel);
    }
}
