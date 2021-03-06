package alns.heuristics.protocols;

import alns.Solution;
import objects.Order;

import java.util.Set;

public interface Destroyer {

    Set<Order> findOrdersToRemove(Solution solution, int numberOfOrders);
    Solution destroy(Solution solution, Set<Order> ordersToRemove);

}
