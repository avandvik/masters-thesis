package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Repairer;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class InsertionMaxPenaltyCost extends Heuristic implements Repairer {

    public InsertionMaxPenaltyCost(String name) {
        super(name);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<Order> sortedOrders = Helpers.sortOrdersByPenalty(partialSolution);

        for (Order order : sortedOrders) {
            newSolution = InsertionGreedy.insertGreedilyInSolution(newSolution, order);
        }
        return newSolution;
    }
}
