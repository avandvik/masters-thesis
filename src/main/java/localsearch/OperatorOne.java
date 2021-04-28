package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import subproblem.SubProblem;
import utils.Helpers;

import java.util.*;

public abstract class OperatorOne extends Operator {

    protected static List<List<Installation>> seenInstSequences;
    protected static Map<Integer, Double> vesselToBestObjective;

    public static void initialize(Solution solution) {
        seenInstSequences = new ArrayList<>();
        vesselToBestObjective = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            int hash = SubProblem.getSubProblemHash(orderSequence, vesselIdx);
            double bestObjective = orderSequence.isEmpty() ? 0.0 : Objective.hashToCost.get(hash);
            vesselToBestObjective.put(vesselIdx, bestObjective);
        }
        originalSolution = solution;
        newSolution = Helpers.deepCopySolution(solution);
    }

    public static void updateFields(List<Order> newOrderSequence, int vesselIdx) {
        double newObjective = Objective.runSPLean(newOrderSequence, vesselIdx);
        if (newObjective < vesselToBestObjective.get(vesselIdx)) {
            vesselToBestObjective.put(vesselIdx, newObjective);
            newSolution.replaceOrderSequence(vesselIdx, newOrderSequence);
        }
    }

}
