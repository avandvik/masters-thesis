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

    public int getSelections() {
        return this.selections;
    }

    public String getName() {
        return name;
    }

    private void smoothenWeights() {
        if (this.selections == 0) return;
        double nWeight = (1 - Parameters.reaction) * this.weight + Parameters.reaction * (this.score / this.selections);
        this.weight = Math.max(nWeight, Parameters.initialWeight);
    }

    static int getNbrOrdersToRemove(Solution solution) {
        int nbrScheduledOrders = 0;
        for (List<Order> orderSequence : solution.getOrderSequences()) {
            nbrScheduledOrders += orderSequence.size();
        }
        int nbrOrdersToRemove = (int) Math.ceil((nbrScheduledOrders * Parameters.percentageOrdersRemove));
        return Math.max(Parameters.minOrdersRemove, nbrOrdersToRemove);
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

    @Override
    public String toString() {
        return this.name;
    }
}
