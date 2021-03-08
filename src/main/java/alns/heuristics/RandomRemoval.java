package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class RandomRemoval extends Heuristic implements Destroyer {

    private final static Random random = new Random();

    public RandomRemoval(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    // TODO: This should also remove postponed orders
    @Override
    public Set<Order> findOrdersToRemove(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> removedOrders = new HashSet<>();
        while (numberOfOrders > 0) {
            int randomSequenceNumber = random.nextInt(orderSequences.size());
            if (orderSequences.get(randomSequenceNumber).size() == 0) continue;
            int randomOrderNumber = random.nextInt(orderSequences.get(randomSequenceNumber).size());
            Order removedOrder = orderSequences.get(randomSequenceNumber).remove(randomOrderNumber);
            removedOrders.add(removedOrder);
            numberOfOrders--;
        }
        return removedOrders;
    }

    // TODO: This should be in Heuristic and must be verified
    @Override
    public Solution destroy(Solution solution, Set<Order> ordersToRemove) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        for (List<Order> orderSequence : orderSequences) orderSequence.removeIf(ordersToRemove::contains);

        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        postponedOrders.removeIf(postponedOrders::contains);

        return new Solution(orderSequences, postponedOrders, false);
    }
}
