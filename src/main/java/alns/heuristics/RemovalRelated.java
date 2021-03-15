package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Problem;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class RemovalRelated extends Heuristic implements Destroyer {

    public RemovalRelated(String name, boolean destroy, boolean repair) { super(name, destroy, repair); }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        double rand_parameter = 1;

        if (unplacedOrders.size() > 0) throw new IllegalStateException(Messages.unplacedOrdersNotEmpty);

        while (unplacedOrders.size() == 0) {

            if (unplacedOrders.size() == numberOfOrders) break;

            int rnSequenceIdx = Problem.random.nextInt(orderSequences.size() + 1);

            if (rnSequenceIdx == orderSequences.size() && postponedOrders.size() > 0) {
                Order orderToRemove = Helpers.removeRandomElementFromSet(postponedOrders);
                List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
                unplacedOrders.addAll(ordersToRemove);
                postponedOrders.removeAll(ordersToRemove);
                for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);
                continue;
            }

            if (rnSequenceIdx == orderSequences.size() || orderSequences.get(rnSequenceIdx).size() == 0) continue;

            int randomOrderNumber = Problem.random.nextInt(orderSequences.get(rnSequenceIdx).size());
            Order orderToRemove = orderSequences.get(rnSequenceIdx).remove(randomOrderNumber);
            List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
            unplacedOrders.addAll(ordersToRemove);
            orderSequences.get(rnSequenceIdx).removeAll(ordersToRemove);
            postponedOrders.removeAll(ordersToRemove);
        }

        List<Order> unplacedOrdersList = new ArrayList<>(unplacedOrders);

        while (unplacedOrders.size() < numberOfOrders) {
            Order order = unplacedOrdersList.get(unplacedOrdersList.size() - 1);
            Order orderToRemove = null;
            Map<Order, Double> distanceToRemovalOrder = findRelatedRemovals(orderSequences, postponedOrders, order);

            Iterator iterator = distanceToRemovalOrder.entrySet().iterator();
            while (iterator.hasNext()) {
                if (Problem.random.nextDouble() > (1 - rand_parameter)) {
                    Map.Entry distanceToRemovalPair = (Map.Entry) iterator.next();
                    orderToRemove = (Order) distanceToRemovalPair.getKey();
                    break;
                }
                iterator.remove();
            }
            List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
            unplacedOrders.addAll(ordersToRemove);
            postponedOrders.removeAll(ordersToRemove);
            for (List<Order> orderSequence : orderSequences) {
                for (int orderIdx = 0; orderIdx < orderSequence.size(); orderIdx++) {
                    if (ordersToRemove.contains(orderSequence.get(orderIdx))) {
                        orderSequence.remove(orderIdx);
                    }
                }
            }
            unplacedOrdersList.addAll(ordersToRemove);
        }

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Map<Order, Double> findRelatedRemovals(List<List<Order>> orderSequences, Set<Order> postponedOrders, Order order) {
        Map<Order, Double> distanceToRemovalOrder = new HashMap<>();
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
            for (Order orderToRelate : orderSequenceCopy)
                if (orderToRelate != order) {
                    double distance = DistanceCalculator.distance(order, orderToRelate, "N");
                    distanceToRemovalOrder.put(orderToRelate, distance);
                }
        }

        Set<Order> copyOfPostponedOrders = Helpers.deepCopySet(postponedOrders);
        for (Order orderToRelate : copyOfPostponedOrders) {
            if (orderToRelate != order) {
                double distance = DistanceCalculator.distance(order, orderToRelate, "N");
                distanceToRemovalOrder.put(orderToRelate, distance);
            }
        }

        LinkedHashMap<Order, Double> sortedDistanceToRemovalOrder = new LinkedHashMap<>();
        distanceToRemovalOrder.entrySet().stream().sorted(Map.Entry.comparingByValue()).
                forEachOrdered(entryOrder -> sortedDistanceToRemovalOrder.put(entryOrder.getKey(), entryOrder.getValue()));

        return sortedDistanceToRemovalOrder;
    }

}
