package alns;

import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.Node;
import utils.Helpers;

import java.util.*;

public class Solution {

    private final List<List<Order>> orderSequences;
    private final Set<Order> postponedOrders;
    private List<List<Node>> shortestPaths;
    private double fitness = Double.POSITIVE_INFINITY;

    public Solution(List<List<Order>> orderSequences, Set<Order> postponedOrders, boolean setFitness) {
        this.orderSequences = orderSequences;
        this.postponedOrders = postponedOrders;
        if (setFitness) Objective.setObjValAndSchedule(this);
    }

    public List<List<Order>> getOrderSequences() {
        return orderSequences;
    }

    public List<Order> getOrderSequence(int vesselNumber) {
        return this.orderSequences.get(vesselNumber);
    }

    public Set<Order> getPostponedOrders() {
        return postponedOrders;
    }

    public List<List<Node>> getShortestPaths() {
        return shortestPaths;
    }

    public void setShortestPaths(List<List<Node>> shortestPaths) {
        this.shortestPaths = shortestPaths;
    }

    public double getFitness(boolean noise) {
        return fitness + (noise ? Helpers.getRandomDouble(-Parameters.maxNoise, Parameters.maxNoise) : 0.0);
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void printSchedules() {
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            System.out.println("Schedule for " + Problem.getVessel(vesselNumber));
            for (Node node : this.shortestPaths.get(vesselNumber)) {
                String orderName = "";
                String schedule = "";
                if (node.getOrder() != null) {
                    orderName = node.getOrder().toString();
                    schedule = "\t\tArrives at: " + node.getArrTime()
                            + "\n\t\tServices at: " + node.getServiceStartTime()
                            + "\n\t\tFinished at: " + node.getDiscreteTime();
                } else {
                    orderName = "Depot";
                    schedule = "\t\t" + (node.getChildren().size() > 0 ? "Leaves at: " : "Arrives at ")
                            + node.getDiscreteTime();
                }
                System.out.println("\t" + orderName + "\n" + schedule);
            }
        }
    }

    @Override
    public String toString() {
        return this.orderSequences.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Objects.equals(orderSequences, solution.getOrderSequences());
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSequences);
    }
}
