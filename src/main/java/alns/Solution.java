package alns;

import data.Problem;
import objects.Order;
import subproblem.Node;

import java.util.*;
import java.util.stream.Collectors;

public class Solution {

    private final List<List<Order>> orderSequences;
    private final Set<Order> postponedOrders;
    private List<List<Node>> shortestPaths;
    private double fitness = Double.POSITIVE_INFINITY;

    public Solution(List<List<Order>> orderSequences) {
        this.orderSequences = orderSequences;
        this.postponedOrders = inferPostponedOrders(orderSequences);
    }

    public Solution(List<List<Order>> orderSequences, Set<Order> postponedOrders) {
        this.orderSequences = orderSequences;
        this.postponedOrders = postponedOrders;
    }

    public Solution(List<List<Order>> orderSequences, List<List<Node>> shortestPaths, double fitness) {
        this.orderSequences = orderSequences;
        this.postponedOrders = inferPostponedOrders(orderSequences);
        this.shortestPaths = shortestPaths;
        this.fitness = fitness;
    }

    public Solution(List<List<Order>> orderSequences, Set<Order> postponedOrders, List<List<Node>> shortestPaths,
                    double fitness) {
        this.orderSequences = orderSequences;
        this.postponedOrders = postponedOrders;
        this.shortestPaths = shortestPaths;
        this.fitness = fitness;
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

    private Set<Order> inferPostponedOrders(List<List<Order>> orderSequences) {
        return orderSequences.stream()
                .flatMap(Collection::stream)
                .filter(order -> Problem.orders.contains(order))
                .collect(Collectors.toSet());
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
        return Double.compare(solution.fitness, fitness) == 0 && Objects.equals(orderSequences,
                solution.orderSequences) && Objects.equals(shortestPaths, solution.shortestPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSequences);
    }
}
