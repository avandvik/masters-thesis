package alns.heuristics;

import data.Problem;
import objects.Installation;
import objects.Order;

import java.util.ArrayList;
import java.util.List;

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

    // TODO: Parameterize r (replace 0.8 with 1 - r and 0.2 with r)
    private void smoothenWeights() {
        this.weight = 0.8 * this.weight + 0.2 * (this.score / this.selections);
    }

    static List<Order> getOrdersToRemove(Order orderToRemove) {
        Installation instWithOrder = Problem.getInstallation(orderToRemove);
        return new ArrayList<>(Problem.getOrdersFromInstallation(instWithOrder));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
