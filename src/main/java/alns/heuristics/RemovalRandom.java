package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class RemovalRandom extends Heuristic implements Destroyer {

    public RemovalRandom(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());

        while (unplacedOrders.size() < numberOfOrders) {
            int rnSequenceIdx = Problem.random.nextInt(orderSequences.size() + 1);

            if (rnSequenceIdx == orderSequences.size() && postponedOrders.size() > 0) {
                Order orderToRemove = Helpers.removeRandomElementFromSet(postponedOrders);
                List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
                unplacedOrders.addAll(ordersToRemove);
                postponedOrders.removeAll(ordersToRemove);
                continue;
            }

            if (rnSequenceIdx == orderSequences.size() || orderSequences.get(rnSequenceIdx).size() == 0) continue;

            int randomOrderNumber = Problem.random.nextInt(orderSequences.get(rnSequenceIdx).size());
            Order orderToRemove = orderSequences.get(rnSequenceIdx).remove(randomOrderNumber);
            List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
            unplacedOrders.addAll(ordersToRemove);
            orderSequences.get(rnSequenceIdx).removeIf(unplacedOrders::contains);
        }

        // Return a partial solution
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
