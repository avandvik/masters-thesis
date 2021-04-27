package alns.heuristics;

import alns.Solution;
import data.Parameters;
import data.Problem;
import objects.Installation;
import objects.Order;

import java.util.*;

public abstract class Heuristic {

    private final String name;
    private double weight;
    private double score;
    private int selections;

    public Heuristic(String name) {
        this.name = name;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return this.weight;
    }

    public void addToScore(double points) {
        this.score += points;
    }

    public void resetScoreAndUpdateWeight() {
        this.smoothenWeights();
        this.score = 0.0;
    }

    public void incrementSelections() {
        this.selections++;
    }

    public String getName() {
        return name;
    }

    private void smoothenWeights() {
        this.weight = (1 - Parameters.reaction) * this.weight + Parameters.reaction * (this.score / this.selections);
    }

    static List<Order> getOrdersToRemove(Order orderToRemove) {
        Installation instWithOrder = Problem.getInstallation(orderToRemove);
        return new ArrayList<>(Problem.getOrdersFromInstallation(instWithOrder));
    }

    static boolean instHasMandUnplacedOrder(Order order, Set<Order> unplacedOrders) {
        if (!order.isMandatory()) {
            Order mandOrder = Problem.getMandatoryOrder(order);
            return mandOrder != null && unplacedOrders.contains(mandOrder);
        }
        return false;
    }

    static List<Order> sortOrders(Solution partialSolution, boolean penalty, boolean size) {
        List<Order> sortedOrders = new ArrayList<>();
        List<Order> optionalOrders = new ArrayList<>();
        for (Order order : partialSolution.getUnplacedOrders()) {
            if (order.isMandatory()) {
                sortedOrders.add(order);
            } else {
                optionalOrders.add(order);
            }
        }
        Collections.sort(sortedOrders);  // Sort by id for predictability in test
        Collections.sort(optionalOrders);  // Sort by id for predictability in test
        if (penalty) {
            optionalOrders.sort(Comparator.comparing((Order::getPostponementPenalty)).reversed());
        } else if (size) {
            optionalOrders.sort(Comparator.comparing((Order::getSize)).reversed());
        }
        sortedOrders.addAll(optionalOrders);
        return sortedOrders;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
