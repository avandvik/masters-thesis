package subproblem;

import data.Problem;
import objects.Order;
import objects.Vessel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblem implements Runnable {

    private final List<Order> orderSequence;
    private final boolean isSpotVessel;
    private List<Node> shortestPath;
    private double shortestPathCost;

    public static Map<Integer, Double> sharedObjectiveValues;

    public SubProblem(List<Order> orderSequence, int vesselNumber) throws IllegalArgumentException {
        isOrderSequenceValid(orderSequence);
        isVesselNumberValid(vesselNumber);
        this.orderSequence = orderSequence;
        Vessel vessel = Problem.getVessel(vesselNumber);
        this.isSpotVessel = Problem.isSpotVessel(vessel);
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public double getShortestPathCost() {
        return shortestPathCost;
    }

    public void solve() {
        Tree tree = new Tree();
        tree.generateTree(this.orderSequence, this.isSpotVessel);
        this.shortestPath = tree.findShortestPath();
        this.shortestPathCost = tree.getGlobalBestCost();
    }

    private void isOrderSequenceValid(List<Order> orderSequence) throws IllegalArgumentException {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException("Empty order sequence passed to SubProblem, " +
                "skipping.");

        // Can be expanded
    }

    private void isVesselNumberValid(int vesselNumber) throws IllegalArgumentException {
        if (vesselNumber < 0 || vesselNumber >= Problem.getNumberOfVessels()) throw new IllegalArgumentException(
                "Invalid vesselNumber passed to SubProblem.");
    }

    public static void initializeParallelRuns() {
        sharedObjectiveValues = new ConcurrentHashMap<>();
    }

    public static Map<Integer, Double> getSharedObjectiveValues() {
        return sharedObjectiveValues;
    }

    @Override
    public void run() {
        Tree tree = new Tree();
        tree.generateTree(this.orderSequence, this.isSpotVessel);
        tree.findShortestPath();

        Map<List<Order>, Boolean> hashStructure = new HashMap<>();
        hashStructure.put(orderSequence, isSpotVessel);
        int hash = hashStructure.hashCode();

        sharedObjectiveValues.put(hash, tree.getGlobalBestCost());
    }
}
