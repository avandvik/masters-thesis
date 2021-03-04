package subproblem;

import alns.Solution;
import data.Problem;
import objects.Order;
import objects.Vessel;

import java.util.ArrayList;
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
        if (orderSequence.isEmpty()) throw new IllegalArgumentException("Empty order sequence passed to SubProblem, " +
                "skipping.");

        // Can be expanded
    }

    private void isVesselNumberValid(int vesselNumber) throws IllegalArgumentException {
        if (vesselNumber < 0 || vesselNumber >= Problem.getNumberOfVessels()) throw new IllegalArgumentException(
                "Invalid vesselNumber passed to SubProblem.");
    }

    public static void runSubProblem(Solution solution) {
        List<List<Node>> shortestPaths = new ArrayList<>();
        double objectiveValue = 0.0;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            SubProblem subProblem = runSingleSubProblem(solution.getOrderSequence(vesselNumber), vesselNumber);
            shortestPaths.add(subProblem != null ? subProblem.getShortestPath() : new ArrayList<>());
            objectiveValue += subProblem != null ? subProblem.getShortestPathCost() : 0.0;
        }
        solution.setShortestPaths(shortestPaths);
        solution.setFitness(objectiveValue);

        solution.printSchedules();
    }

    public static SubProblem runSingleSubProblem(List<Order> orderSequence, int vesselNumber) {
        try {
            SubProblem subProblem = new SubProblem(orderSequence, vesselNumber);
            subProblem.solve();
            System.out.println(subProblem.getShortestPath() + ": " + subProblem.getShortestPathCost());
            return subProblem;
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }
}
