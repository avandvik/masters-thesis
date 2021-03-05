package alns;

import data.Problem;
import objects.Order;
import subproblem.SubProblem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GreedyInsertion {

    public static Solution getGreedyInsertion(Solution partialSolution, Order order) {
        List<Solution> feasibleInsertions = Construction.getAllFeasibleInsertions(partialSolution,order);
        if (feasibleInsertions.isEmpty()) return null;

        double bestObjective = Double.POSITIVE_INFINITY;
        partialSolution.setFitness(bestObjective);
        Solution bestSolution = partialSolution;

        for (Solution solution : feasibleInsertions) {
            SubProblem.runSubProblem(solution);
            double tempObjective = solution.getFitness();
            if (tempObjective < bestObjective) {
                bestObjective = tempObjective;
                bestSolution = solution;
            }
        }

        return bestSolution;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false);
        List<Order> orders = Problem.orders;
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());

        for (int i = 0; i < 2; i++) {
            orderSequences.get(0).add(orders.get(i));
        }
        for (int j = 2; j < 5; j++) {
            orderSequences.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 7; k++) {
            orderSequences.get(2).add(orders.get(k));
        }

        Solution solution = new Solution(orderSequences);
        Order orderToBePlaced = Problem.orders.get(Problem.orders.size() - 1);
        Solution basicGreedySolution = getGreedyInsertion(solution, orderToBePlaced);
    }
}
