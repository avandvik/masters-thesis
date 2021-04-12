package setpartitioning;

import data.Problem;
import objects.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VoyageScoring {

    private static List<Double> calculateDeckUtilization(List<Order> orderSequence, int vesselIdx) {
        int capacity = Problem.getVessel(vesselIdx).getCapacity();
        List<Double> utilizationSequence = new ArrayList<>();
        int load = orderSequence.stream().filter(Order::isDelivery).mapToInt(Order::getSize).sum();
        utilizationSequence.add(load / (double) capacity);
        for (Order order : orderSequence) {
            load += order.isDelivery() ? -order.getSize() : order.getSize();
            utilizationSequence.add(load / (double) capacity);
        }
        double avgDeckUtilization = utilizationSequence.stream().mapToDouble(a -> a).average().orElse(0.0);
        double maxDeckUtilization = Collections.max(utilizationSequence);
        double totalIncreaseUtilization = 0.0;
        for (int i = 1; i < utilizationSequence.size(); i++) {
            double increase = utilizationSequence.get(i) - utilizationSequence.get(i - 1);
            if (increase > 0) totalIncreaseUtilization += increase;
        }
        return new ArrayList<>(Arrays.asList(avgDeckUtilization, maxDeckUtilization, totalIncreaseUtilization));
    }
}
