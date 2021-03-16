package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class RemovalRelated extends Heuristic implements Destroyer {

    public RemovalRelated(String name, boolean destroy, boolean repair) { super(name, destroy, repair); }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {

        // Copying order sequences and sets for manipulation
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());

        if (unplacedOrders.size() > 0) throw new IllegalStateException(Messages.unplacedOrdersNotEmpty);
        if (numberOfOrders == 0) return solution;

        // Getting random order and removing other orders to same inst.
        while (unplacedOrders.size() == 0) {

            int rnSequenceIdx = Problem.random.nextInt(orderSequences.size() + 1);

            // If we are in postponed orders set and there are postponed orders
            if (rnSequenceIdx == orderSequences.size() && postponedOrders.size() > 0) {
                Order orderToRemove = Helpers.removeRandomElementFromSet(postponedOrders);
                List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
                unplacedOrders.addAll(ordersToRemove);
                postponedOrders.removeAll(ordersToRemove);
                for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);
                continue;
            }

            // If we are inn postponed orders set but there are no orders
            if (rnSequenceIdx == orderSequences.size() || orderSequences.get(rnSequenceIdx).size() == 0) continue;

            int randomOrderNumber = Problem.random.nextInt(orderSequences.get(rnSequenceIdx).size());
            Order orderToRemove = orderSequences.get(rnSequenceIdx).remove(randomOrderNumber);
            List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
            unplacedOrders.addAll(ordersToRemove);
            orderSequences.get(rnSequenceIdx).removeAll(ordersToRemove);
            postponedOrders.removeAll(ordersToRemove);
        }

        List<Order> unplacedOrdersList = new ArrayList<>(unplacedOrders);

        // Finding related removals and choosing order to remove
        while (unplacedOrders.size() < numberOfOrders) {
            Order order = unplacedOrdersList.get(unplacedOrdersList.size() - 1);
            Order orderToRemove = null;
            Map<Order, Double> distanceToRemovalOrder = findRelatedRemovals(orderSequences, postponedOrders, order);

            // Choosing order to remove with randomization
            Iterator<Map.Entry<Order, Double>> iterator = distanceToRemovalOrder.entrySet().iterator();
            while (iterator.hasNext()) {
                if (Problem.random.nextDouble() > Parameters.randomParameter) {
                    Map.Entry<Order, Double> distanceToRemovalPair = iterator.next();
                    orderToRemove = distanceToRemovalPair.getKey();
                    break;
                }
                iterator.remove();
            }

            // Updating sequences and sets
            List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
            unplacedOrders.addAll(ordersToRemove);
            postponedOrders.removeAll(ordersToRemove);
            for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);
            unplacedOrdersList.addAll(ordersToRemove);
        }

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Map<Order, Double> findRelatedRemovals(List<List<Order>> orderSequences, Set<Order> postponedOrders, Order order) {
        Map<Order, Double> distanceToRemovalOrder = new HashMap<>();

        // Creating hashmap with related orders and distances
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            for (Order orderToRelate : orderSequence)
                if (orderToRelate != order) {
                    double distance = DistanceCalculator.distance(order, orderToRelate, "N");
                    distanceToRemovalOrder.put(orderToRelate, distance);
                }
        }

        for (Order orderToRelate : postponedOrders) {
            if (orderToRelate != order) {
                double distance = DistanceCalculator.distance(order, orderToRelate, "N");
                distanceToRemovalOrder.put(orderToRelate, distance);
            }
        }

        // Sorting orders by ascending distance
        Map<Order, Double> sortedDistanceToRemovalOrder = new LinkedHashMap<>();
        distanceToRemovalOrder.entrySet().stream().sorted(Map.Entry.comparingByValue()).
                forEachOrdered(entryOrder -> sortedDistanceToRemovalOrder.put(entryOrder.getKey(), entryOrder.getValue()));

        return sortedDistanceToRemovalOrder;
    }

}
