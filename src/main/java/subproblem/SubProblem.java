package subproblem;

import alns.Objective;
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

    public SubProblem(List<Order> orderSequence, int vesselIdx) {
        isOrderSequenceValid(orderSequence);
        isVesselIdxValid(vesselIdx);
        this.orderSequence = orderSequence;
        this.vesselIdx = vesselIdx;
        this.isSpotVessel = Problem.isSpotVessel(vesselIdx);
    }

    public static void initializeResultsStructure() {
        vesselToObjective = new ConcurrentHashMap<>();
    }

    public static void addToResultsStructure(int vesselIdx, double cost) {
        vesselToObjective.put(vesselIdx, cost);
    }

    @Override
    public void run() {
        this.solveSubProblem();
        addToResultsStructure(vesselIdx, this.shortestPathCost);
        Objective.cacheSubProblemResults(this.hashCode(), this);
    }

    public void solveSubProblem() {
        Tree tree = new Tree();
        tree.generateTree(this.orderSequence, this.isSpotVessel);
        this.shortestPath = tree.findShortestPath();
        this.shortestPathCost = tree.getGlobalBestCost();
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

    private void isOrderSequenceValid(List<Order> orderSequence) {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException(Messages.emptySequenceSP);
    }

    private void isVesselIdxValid(int vesselIdx) {
        if (vesselIdx < 0 || vesselIdx >= Problem.getNumberOfVessels())
            throw new IllegalArgumentException(Messages.invalidVesselIdx);
    }

    public static int getSubProblemHash(List<Order> orderSequence, int vesselIdx) {
        boolean isSpotVessel = Problem.isSpotVessel(vesselIdx);
        return Objects.hash(orderSequence, isSpotVessel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSequence, isSpotVessel);
    }
}
