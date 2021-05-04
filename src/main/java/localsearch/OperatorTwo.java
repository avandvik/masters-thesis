package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.List;
import java.util.Map;

public abstract class OperatorTwo extends Operator {

    static double greatestDecrease; // Lowest negative number
    static Map<Integer, Double> vesselToCost;

    static void initialize(Solution solution) {
        greatestDecrease = 0.0;
        vesselToCost = createVesselToCost(solution);
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
            double decrease = aggregatedCost - originalCost;  // Negative -> decrease
            if (decrease < greatestDecrease) {
                greatestDecrease = decrease;
                newSolution.replaceOrderSequence(vIdxOne, orderSequenceOne);
                newSolution.replaceOrderSequence(vIdxTwo, orderSequenceTwo);
            }
        }
    }

    private static double calculateAggregatedCost(List<List<Order>> orderSequences, int vIdxOne, int vIdxTwo) {
        List<Order> firOrderSeq = orderSequences.get(0);
        List<Order> secOrderSeq = orderSequences.get(1);
        return (Objective.runSP(firOrderSeq, vIdxOne) + Objective.runSP(secOrderSeq, vIdxTwo));
    }
}
