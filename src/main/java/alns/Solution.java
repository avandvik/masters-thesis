package alns;

import data.Problem;
import objects.Order;
import subproblem.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Solution {

    private final List<List<Order>> orderSequences;
    private List<List<Node>> shortestPaths;
    private double fitness = Double.POSITIVE_INFINITY;

    public Solution(List<List<Order>> orderSequences) {
        this.orderSequences = orderSequences;
    }

    public Solution(List<List<Order>> orderSequences, List<List<Node>> shortestPaths, double fitness) {
        this.orderSequences = orderSequences;
        this.shortestPaths = shortestPaths;
        this.fitness = fitness;
    }

    public List<List<Order>> getOrderSequences() {
        return orderSequences;
    }

    public List<Order> getOrderSequence(int vesselNumber) {
        return this.orderSequences.get(vesselNumber);
    }

    public List<List<Node>> getShortestPaths() {
        return shortestPaths;
    }

    public void setShortestPaths(List<List<Node>> shortestPaths) {
        this.shortestPaths = shortestPaths;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<List<Integer>> getInstSequences() {
        List<List<Integer>> instSequences = new ArrayList<>();
        for (List<Order> orderSequence : this.orderSequences) {
            instSequences.add(orderSequence.stream().map(Order::getInstallationId).collect(Collectors.toList()));
        }
        return instSequences;
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
}
