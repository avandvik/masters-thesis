package alns.heuristics.protocols;

import alns.Solution;
import objects.Order;

import java.util.Set;

public interface Repairer {

    Solution repair(Solution partialSolution, Set<Order> ordersToPlace);

}
