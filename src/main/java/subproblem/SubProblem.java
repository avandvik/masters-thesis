package subproblem;

import alns.Cache;
import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import setpartitioning.Pool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblem implements Runnable {

    private final List<Order> orderSequence;
    private final int vesselIdx;
    private final boolean isSpotVessel;
    private double cost;
    private List<Node> shortestPath;

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
        addToResultsStructure(this.vesselIdx, this.cost);
        if (Parameters.cacheSP) Cache.cacheSequence(vesselIdx, orderSequence, this);
        if (Parameters.setPartitioning) Pool.saveVoyage(vesselIdx, orderSequence, this.cost);
    }

    public void solveSubProblem() {
        Tree tree = new Tree(this.vesselIdx);
        tree.generateTree(this.orderSequence, this.isSpotVessel);
        this.shortestPath = tree.findShortestPath();
        this.cost = tree.getGlobalBestCost();
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(List<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public List<Order> getOrderSequence() {
        return orderSequence;
    }

    public int getVIdx() {
        return vesselIdx;
    }

    private void isOrderSequenceValid(List<Order> orderSequence) {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException(Messages.emptySequenceSP);
    }

    private void isVesselIdxValid(int vesselIdx) {
        if (vesselIdx < 0 || vesselIdx >= Problem.getNumberOfVessels())
            throw new IllegalArgumentException(Messages.invalidVesselIdx);
    }
}
