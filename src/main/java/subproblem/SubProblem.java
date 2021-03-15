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
    private final static Map<Integer, Double> hashToCost = new HashMap<>();
    private final static Map<Integer, List<Node>> hashToShortestPath = new HashMap<>();

    public SubProblem(List<Order> orderSequence, int vesselIdx) throws IllegalArgumentException {
        isOrderSequenceValid(orderSequence);
        isVesselIdxValid(vesselIdx);
        this.orderSequence = orderSequence;
        this.vesselIdx = vesselIdx;
        this.isSpotVessel = Problem.isSpotVessel(vesselIdx);
    }

    public static void initialize() {
        vesselToObjective = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        boolean cachedSolution = checkCache(this.hashCode());
        if (!cachedSolution) {
            Tree tree = new Tree();
            tree.generateTree(this.orderSequence, this.isSpotVessel);
            this.shortestPath = tree.findShortestPath();
            hashToShortestPath.put(this.hashCode(), this.shortestPath);
            this.shortestPathCost = tree.getGlobalBestCost();
            hashToCost.put(this.hashCode(), this.shortestPathCost);
        }
        vesselToObjective.put(vesselIdx, this.shortestPathCost);
    }

    public boolean checkCache(int hash) {
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

    public void setShortestPath(List<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public double getShortestPathCost() {
        return shortestPathCost;
    }

    public void setShortestPathCost(double shortestPathCost) {
        this.shortestPathCost = shortestPathCost;
    }

    public List<Order> getOrderSequence() {
        return orderSequence;
    }

    public int getVesselIdx() {
        return vesselIdx;
    }

    public boolean isSpotVessel() {
        return isSpotVessel;
    }

    private void isOrderSequenceValid(List<Order> orderSequence) throws IllegalArgumentException {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException(Messages.emptySequenceSP);
    }

    private void isVesselIdxValid(int vesselIdx) throws IllegalArgumentException {
        if (vesselIdx < 0 || vesselIdx >= Problem.getNumberOfVessels())
            throw new IllegalArgumentException(Messages.invalidVesselIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSequence, isSpotVessel);
    }
}
