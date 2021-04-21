package alns.heuristics;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Messages;
import objects.Order;
import utils.Helpers;
import java.util.List;

public class InsertionMaxOrderSize extends Heuristic implements Repairer {

    public InsertionMaxOrderSize(String name) {
        super(name);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = Helpers.deepCopySolution(partialSolution);
        List<Order> sortedOrders = Helpers.sortOrdersBySize(partialSolution);

        for (Order order : sortedOrders) {
            solution = InsertionGreedy.insertGreedilyInSolution(solution, order);
        }
        Objective.setObjValAndSchedule(solution);
        if (!Evaluator.isSolutionFeasible(solution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return solution;
    }

}
