package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OperatorTwo extends Operator {

    static Map<Integer, Double> vesselToCost;
    static double greatestDecrease; // Lowest negative number

    static void initialize(Solution solution) {
        greatestDecrease = 0.0;
        vesselToCost = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            double cost = orderSequence.isEmpty() ? 0.0 : Objective.runSPLean(orderSequence, vesselIdx); // Cached
            vesselToCost.put(vesselIdx, cost);
        }
        originalSolution = solution;
        newSolution = Helpers.deepCopySolution(solution);
    }

    static double calculateOriginalCost(int vIdxOne, int vIdxTwo) {
        return (vesselToCost.get(vIdxOne) + vesselToCost.get(vIdxTwo));
    }

    static void updateFields(List<List<Order>> orderSequences, int vIdxOne, int vIdxTwo, double originalCost) {
        List<Order> orderSequenceOne = orderSequences.get(0);
        List<Order> orderSequenceTwo = orderSequences.get(1);
        boolean firstFeasible = Evaluator.isOrderSequenceFeasible(orderSequenceOne, Problem.getVessel(vIdxOne));
        boolean secondFeasible = Evaluator.isOrderSequenceFeasible(orderSequenceTwo, Problem.getVessel(vIdxTwo));
        if (firstFeasible && secondFeasible) {
            double aggregatedCost = calculateAggregatedCost(orderSequences, vIdxOne, vIdxTwo);
            double decrease = aggregatedCost - originalCost; // Negative number
            if (decrease < 0 && decrease < greatestDecrease) {
                greatestDecrease = decrease;
                newSolution.replaceOrderSequence(vIdxOne, orderSequenceOne);
                newSolution.replaceOrderSequence(vIdxTwo, orderSequenceTwo);
            }
        }
    }

    private static double calculateAggregatedCost(List<List<Order>> orderSequences, int vIdxOne, int vIdxTwo) {
        List<Order> firOrderSeq = orderSequences.get(0);
        List<Order> secOrderSeq = orderSequences.get(1);
        return (Objective.runSPLean(firOrderSeq, vIdxOne) + Objective.runSPLean(secOrderSeq, vIdxTwo));
    }
}
