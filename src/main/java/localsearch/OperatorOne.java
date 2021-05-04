package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public abstract class OperatorOne extends Operator {

    protected static List<List<Installation>> seenInstSequences;
    protected static Map<Integer, Double> vesselToBestObjective;

    public static void initialize(Solution solution) {
        seenInstSequences = new ArrayList<>();
        vesselToBestObjective = createVesselToCost(solution);
        originalSolution = solution;
        newSolution = Helpers.deepCopySolution(solution);
    }

    public static void updateFields(List<Order> newOrderSequence, int vesselIdx) {
        if (!Evaluator.isOrderSequenceFeasible(newOrderSequence, Problem.getVessel(vesselIdx))) return;
        double newObjective = Objective.runSP(newOrderSequence, vesselIdx);
        if (newObjective < vesselToBestObjective.get(vesselIdx)) {
            vesselToBestObjective.put(vesselIdx, newObjective);
            newSolution.replaceOrderSequence(vesselIdx, newOrderSequence);
        }
    }
}
