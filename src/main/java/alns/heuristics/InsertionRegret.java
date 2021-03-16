package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class InsertionRegret extends Heuristic implements Repairer {

    public InsertionRegret(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    public Solution getRegretSolution(Solution partialSolution, int regretParameter) {
        Solution solutionCopy = Helpers.deepCopySolution(partialSolution);

        // Calculate regret for each order
        Map<Order, Double> orderToRegret = new HashMap<>();
        for (Order order : solutionCopy.getUnplacedOrders()) {
            List<Double> increases = new ArrayList<>();
            List<List<Order>> orderSequences = solutionCopy.getOrderSequences();
            Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
            for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
                List<Order> orderSequence = orderSequences.get(vesselIdx);
                double currentObj = Objective.runSPLean(orderSequence, vesselIdx);
                for (int insertionIdx : insertions.get(vesselIdx)) {
                    List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                    orderSequenceCopy.add(insertionIdx, order);
                    double increase = Objective.runSPLean(orderSequenceCopy, vesselIdx) - currentObj;
                    increases.add(increase);
                }
            }
            orderToRegret.put(order, calculateRegret(increases, regretParameter));
        }

        // Apply
        while (!solutionCopy.getUnplacedOrders().isEmpty()) {
            Order orderToPlace = Collections.max(orderToRegret.entrySet(), Map.Entry.comparingByValue()).getKey();
            orderToRegret.remove(orderToPlace);
            solutionCopy.getUnplacedOrders().remove(orderToPlace);
            solutionCopy = InsertionGreedy.getGreedyInsertion(solutionCopy, orderToPlace);
        }
        return solutionCopy;
    }

    private double calculateRegret(List<Double> increases, int regretParameter) {
        double regret = 0.0;
        Collections.sort(increases);
        for (int i = 0; i < Math.min(regretParameter, increases.size()); i++)
            regret += increases.get(i) - increases.get(0);
        return regret;
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        int regretParameter = 2;
        solution = getRegretSolution(solution, regretParameter);
        return new Solution(solution.getOrderSequences(), solution.getPostponedOrders(), true);
    }
}
