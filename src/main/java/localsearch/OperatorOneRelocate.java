package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import subproblem.SubProblem;
import utils.Helpers;

import java.util.*;

public class OperatorOneRelocate {

    private static List<List<Installation>> seenInstSequences;  // History of seen sequences in the search
    private static Map<Integer, Double> vesselToBestObjective;
    private static Solution originalSolution;
    private static Solution newSolution;

    public static Solution oneRelocate(Solution solution) {
        initialize(solution);
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            if (orderSequence.isEmpty()) continue;
            List<Installation> instSequence = Helpers.getInstSequence(orderSequence);
            seenInstSequences.add(instSequence);
            for (Installation installation : instSequence) {
                List<Installation> rmInstSequence = rmInstFromSequence(instSequence, installation);
                for (int insertionIdx = 0; insertionIdx <= rmInstSequence.size(); insertionIdx++) {
                    List<Installation> newInstSequence = addInstToPosition(rmInstSequence, installation, insertionIdx);
                    if (seenInstSequences.contains(newInstSequence)) continue;
                    seenInstSequences.add(newInstSequence);
                    List<Order> newOrderSequence = createNewOrderSequence(newInstSequence);
                    updateFields(newOrderSequence, vesselIdx);
                }
            }
        }
        return newSolution;
    }

    private static void initialize(Solution solution) {
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

    private static List<Installation> rmInstFromSequence(List<Installation> instSequence, Installation inst) {
        List<Installation> instSequenceExclInst = Helpers.deepCopyList(instSequence, false);
        instSequenceExclInst.remove(inst);
        return instSequenceExclInst;
    }

    private static List<Installation> addInstToPosition(List<Installation> instSequence, Installation inst, int idx) {
        List<Installation> newInstSequence = Helpers.deepCopyList(instSequence, false);
        newInstSequence.add(idx, inst);
        return newInstSequence;
    }

    private static List<Order> createNewOrderSequence(List<Installation> newInstSequence) {
        List<Order> newOrderSequence = new LinkedList<>();
        for (Installation installation : newInstSequence) {
            List<Order> ordersFromInst = Problem.getOrdersFromInstallation(installation);
            ordersFromInst.removeAll(originalSolution.getPostponedOrders());
            Collections.sort(ordersFromInst);
            newOrderSequence.addAll(ordersFromInst);
        }
        return newOrderSequence;
    }

    private static void updateFields(List<Order> newOrderSequence, int vesselIdx) {
        double newObjective = Objective.runSPLean(newOrderSequence, vesselIdx);
        if (newObjective < vesselToBestObjective.get(vesselIdx)) {
            vesselToBestObjective.put(vesselIdx, newObjective);
            newSolution.replaceOrderSequence(vesselIdx, newOrderSequence);
        }
    }
}
