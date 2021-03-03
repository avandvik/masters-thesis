package alns;

import objects.Order;

import java.util.*;
import java.util.stream.Collectors;

public class Solution {

    private final List<List<Order>> orderSequences;
    private double objectiveValue = Double.POSITIVE_INFINITY;

    public Solution(List<List<Order>> orderSequences) {
        this.orderSequences = orderSequences;
    }

    public Solution(List<List<Order>> orderSequences, double objectiveValue) {
        this.orderSequences = orderSequences;
        this.objectiveValue = objectiveValue;
    }

    public List<List<Order>> getOrderSequences() {
        return orderSequences;
    }

    public List<Order> getOrderSequence(int vesselNumber) {
        return this.orderSequences.get(vesselNumber);
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

    public void setObjectiveValue(double objectiveValue) {
        this.objectiveValue = objectiveValue;
    }

    public List<List<Integer>> getInstSequences() {
        List<List<Integer>> instSequences = new ArrayList<>();
        for (List<Order> orderSequence : this.orderSequences) {
            instSequences.add(orderSequence.stream().map(Order::getInstallationId).collect(Collectors.toList()));
        }
        return instSequences;
    }

    @Override
    public String toString() {
        return this.orderSequences.toString();
    }
}
