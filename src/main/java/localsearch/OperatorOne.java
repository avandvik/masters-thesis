package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import subproblem.SubProblem;
import utils.Helpers;

import java.util.*;

public abstract class OperatorOne {

    protected static List<List<Installation>> seenInstSequences;  // TODO: Remove when OneRelocate is updated
    protected static Map<Integer, Double> vesselToBestObjective;
    protected static Solution originalSolution;
    protected static Solution newSolution;

    public static void initialize(Solution solution) {
        seenInstSequences = new ArrayList<>();  // TODO: Remove when OneRelocate is updated
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

    public static List<Order> createNewOrderSequence(List<Installation> newInstSequence) {
        List<Order> newOrderSequence = new LinkedList<>();
        for (Installation installation : newInstSequence) {
            List<Order> ordersFromInst = Problem.getOrdersFromInstallation(installation);
            ordersFromInst.removeAll(originalSolution.getPostponedOrders());
            Collections.sort(ordersFromInst);
            newOrderSequence.addAll(ordersFromInst);
        }
        return newOrderSequence;
    }

    public static void updateFields(List<Order> newOrderSequence, int vesselIdx) {
        double newObjective = Objective.runSPLean(newOrderSequence, vesselIdx);
        if (newObjective < vesselToBestObjective.get(vesselIdx)) {
            vesselToBestObjective.put(vesselIdx, newObjective);
            newSolution.replaceOrderSequence(vesselIdx, newOrderSequence);
        }
    }

}
