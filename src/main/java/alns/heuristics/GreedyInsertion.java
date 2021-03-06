package alns.heuristics;

import alns.Construction;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import objects.Order;

import java.util.List;
import java.util.Set;

public class GreedyInsertion extends Heuristic implements Repairer {

    public GreedyInsertion(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    public Solution getGreedyInsertion(Solution partialSolution, Order order) {
        /* Inserts order in an available vessel, a spot vessel, or the set of postponed orders */
        List<Solution> feasibleInsertions = Construction.getAllFeasibleInsertions(partialSolution,order);

        double bestObjective = Double.POSITIVE_INFINITY;
        Solution bestSolution = partialSolution;

        for (Solution solution : feasibleInsertions) {
            solution.setFitness();
            double tempObjective = solution.getFitness(false);
            if (tempObjective < bestObjective) {
                bestObjective = tempObjective;
                bestSolution = solution;
            }
        }

        return bestSolution;
    }

    @Override
    public Solution repair(Solution partialSolution, Set<Order> ordersToPlace) {
        Solution solution = partialSolution;
        for (Order order : ordersToPlace) solution = getGreedyInsertion(solution, order);
        return solution;
    }
}
