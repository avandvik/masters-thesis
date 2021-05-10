package alns.heuristics;

import alns.Objective;
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
        Solution solution = Helpers.deepCopySolution(partialSolution);
        List<Order> sortedOrders = Helpers.sortOrders(partialSolution, true, false);
        for (Order order : sortedOrders) solution = InsertionGreedy.insertGreedilyInSolution(solution, order);
        Objective.setObjValAndSchedule(solution);
        return solution;
    }
}
