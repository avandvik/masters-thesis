package subproblem;

import data.Problem;
import objects.Order;
import objects.Vessel;

import java.util.List;

public class SubProblem {

    private final List<Order> orderSequence;
    private final boolean isSpotVessel;
    private List<Node> shortestPath;
    private double shortestPathCost;

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
        if (orderSequence.isEmpty()) throw new IllegalArgumentException("Empty order sequence passed to Subproblem");

        // Can be expanded
    }

    private void isVesselNumberValid(int vesselNumber) throws IllegalArgumentException {
        if (vesselNumber < 0 || vesselNumber >= Problem.getNumberOfVessels()) throw new IllegalArgumentException(
                "Invalid vesselNumber passed to Subproblem");
    }
}
