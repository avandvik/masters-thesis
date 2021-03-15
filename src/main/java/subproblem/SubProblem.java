package subproblem;

import data.Problem;
import objects.Order;
import objects.Vessel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubProblem {

    private final List<Order> orderSequence;
    private final boolean isSpotVessel;
    private List<Node> shortestPath;
    private double shortestPathCost;

    // Cache
    private final static Map<Integer, Double> hashToCost = new HashMap<>();
    private final static Map<Integer, List<Node>> hashToShortestPath = new HashMap<>();

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
        int hash = this.orderSequence.hashCode();
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

    private void isOrderSequenceValid(List<Order> orderSequence) throws IllegalArgumentException {
        if (orderSequence.isEmpty()) throw new IllegalArgumentException("Empty order sequence passed to SubProblem, " +
                "skipping.");

        // Can be expanded
    }

    private void isVesselNumberValid(int vesselNumber) throws IllegalArgumentException {
        if (vesselNumber < 0 || vesselNumber >= Problem.getNumberOfVessels()) throw new IllegalArgumentException(
                "Invalid vesselNumber passed to SubProblem.");
    }
}
