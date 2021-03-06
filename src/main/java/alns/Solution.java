package alns;

import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.Node;
import utils.Helpers;

import java.util.*;

public class Solution {

    private List<List<Order>> orderSequences;
    private final Set<Order> postponedOrders;
    private final Set<Order> unplacedOrders;
    private List<List<Node>> shortestPaths;
    private double objective = Double.POSITIVE_INFINITY;

    public Solution(List<List<Order>> orderSequences, Set<Order> postponedOrders, boolean setFitness) {
        /* Use this constructor when generating a complete solution */
        this.orderSequences = orderSequences;
        this.postponedOrders = postponedOrders;
        this.unplacedOrders = new HashSet<>();  // Solutions from this constructor must have no unplaced orders
        if (!Evaluator.isSolutionFeasible(this)) throw new IllegalStateException(Messages.infSolCreated);
        if (setFitness) Objective.setObjValAndSchedule(this);
    }

    public Solution(List<List<Order>> orderSequences, Set<Order> postponedOrders, Set<Order> unplacedOrders) {
        /* Use this constructor when generating a partial solution */
        this.orderSequences = orderSequences;
        this.postponedOrders = postponedOrders;
        this.unplacedOrders = unplacedOrders;
    }

    public List<List<Order>> getOrderSequences() {
        return orderSequences;
    }

    public List<Order> getOrderSequence(int vesselIdx) {
        return this.orderSequences.get(vesselIdx);
    }

    public void insertInOrderSequence(int vesselIdx, int insertionIdx, Order order) {
        this.orderSequences.get(vesselIdx).add(insertionIdx, order);
    }

    public void replaceOrderSequence(int vesselIdx, List<Order> newSequence) {
        this.orderSequences.set(vesselIdx, newSequence);
    }

    public void replaceOrderSequences(List<List<Order>> newSequences) {
        this.orderSequences = newSequences;
    }

    public void removeOrderFromSequences(Order rmOrder) {
        for (List<Order> orderSequence : this.orderSequences) {
            boolean removed = orderSequence.removeIf(order -> order.equals(rmOrder));
            if (removed) break;
        }
    }

    public void removeFromOrderSequences(List<Order> orders) {
        this.orderSequences.forEach(orderSequence -> orderSequence.removeAll(orders));
    }

    public void removeOrderFromSequence(int vesselIdx, Order rmOrder) {
        this.orderSequences.get(vesselIdx).remove(rmOrder);
    }

    public Set<Order> getAllPostponed() {
        return postponedOrders;
    }

    public void addPostponedOrder(Order order) {
        this.postponedOrders.add(order);
    }

    public void removePostponedOrder(Order order) {
        this.postponedOrders.remove(order);
    }

    public void removeFromPostponedOrders(List<Order> orders) {
        orders.forEach(this.postponedOrders::remove);
    }

    public Set<Order> getUnplacedOrders() {
        return unplacedOrders;
    }

    public void removeUnplacedOrder(Order order) {
        this.unplacedOrders.remove(order);
    }

    public List<List<Node>> getShortestPaths() {
        return shortestPaths;
    }

    public void setShortestPaths(List<List<Node>> shortestPaths) {
        this.shortestPaths = shortestPaths;
    }

    public double getObjective(boolean noise) {
        return objective + (noise ? Helpers.getRandomDouble(-Parameters.maxNoise, Parameters.maxNoise) : 0.0);
    }

    public void setObjective(double objective) {
        this.objective = objective;
    }

    public double getPenaltyCosts() {
        double penaltyCosts = 0.0;
        for (Order order : postponedOrders) penaltyCosts += order.getPostponementPenalty();
        return penaltyCosts;
    }

    public double getFuelCosts() {
        double fuelCosts = 0.0;
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            Node prevNode = null;
            for (Node node : this.shortestPaths.get(vesselIdx)) {
                if (prevNode != null) fuelCosts += prevNode.getCostOfChild(node);
                prevNode = node;
            }
        }
        return fuelCosts;
    }

    public void clearSubProblemResults() {
        this.shortestPaths = null;
        this.objective = Double.POSITIVE_INFINITY;
    }

    public void printSchedules() {
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            System.out.println("Schedule for " + Problem.getVessel(vesselIdx));
            Node prevNode = null;
            for (Node node : this.shortestPaths.get(vesselIdx)) {
                String orderName;
                String schedule;
                if (node.getOrder() != null) {
                    double speed = node.getSpeed(prevNode);
                    orderName = node.getOrder().toString();
                    schedule = "\t\tArrives at: " + node.getArrTime(prevNode) + " (" + speed + ")"
                            + "\n\t\tServices at: " + node.getServiceStartTime(prevNode)
                            + "\n\t\tFinished at: " + node.getDiscreteTime();
                } else {
                    orderName = "Depot";
                    schedule = "\t\t" + (node.getChildren().size() > 0 ? "Leaves at: " : "Arrives at ")
                            + node.getDiscreteTime();
                }
                if (prevNode != null) schedule += "\n\t\tCost: " + prevNode.getCostOfChild(node);
                System.out.println("\t" + orderName + "\n" + schedule);
                prevNode = node;
            }
        }
        System.out.println();
    }

    @Override
    public String toString() {
        StringBuilder outStr = new StringBuilder();
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            outStr.append(Problem.getVessel(vIdx).toString()).append(": ");
            outStr.append(this.orderSequences.get(vIdx));
            if (vIdx != Problem.getNumberOfVessels() - 1) outStr.append("\n");
        }
        outStr.append("\nPOSTPONED: ").append(this.postponedOrders.toString());
        outStr.append("\nUNPLACED: ").append(this.unplacedOrders.toString());
        return outStr.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Objects.equals(orderSequences, solution.orderSequences) && Objects.equals(postponedOrders,
                solution.postponedOrders) && Objects.equals(unplacedOrders, solution.unplacedOrders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderSequences, postponedOrders);
    }
}
