package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Problem;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class RemovalSpread extends Heuristic implements Destroyer {

    public RemovalSpread(String name) {
        super(name);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        Solution newSolution = Helpers.deepCopySolution(solution);
        List<Order> orders = Helpers.deepCopyList(Problem.orders, true);
        orders.removeIf(candidateOrder -> newSolution.getPostponedOrders().contains(candidateOrder));

        while (newSolution.getUnplacedOrders().size() < numberOfOrders) {
            Order removalOrder = removeSpread(orders);
            List<Order> removalOrders = getOrdersToRemove(removalOrder);
            for (Order order : removalOrders) {
                newSolution.removeOrderFromSequences(order);
                newSolution.getPostponedOrders().remove(order);
                newSolution.getUnplacedOrders().add(order);
                orders.remove(removalOrder);
            }
        }

        newSolution.clearSubProblemResults();

        return newSolution;
    }

    private Order removeSpread(List<Order> orders) {
        Map<Order, Double> orderToMinTravelDistance = new LinkedHashMap<>();
        for (Order currentOrder : orders) {
            double min_distance_to_order = Double.POSITIVE_INFINITY;
            for (Order nextOrder : orders) {
                if (currentOrder.getInstallationId() == nextOrder.getInstallationId()) continue;
                double distance = DistanceCalculator.distance(currentOrder, nextOrder, "N");
                if (distance < min_distance_to_order) {
                    min_distance_to_order = distance;
                }
            }
            orderToMinTravelDistance.put(currentOrder, min_distance_to_order);
        }

        List<Map.Entry<Order, Double>> orderToDistance = new ArrayList<>(orderToMinTravelDistance.entrySet());
        orderToDistance.sort(Comparator.comparing(Map.Entry<Order, Double>::getValue));
        Collections.reverse(orderToDistance);
        return orderToDistance.get(0).getKey();
    }
}
