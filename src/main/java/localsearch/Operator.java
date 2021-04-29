package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public abstract class Operator {

    static Solution originalSolution;
    static Solution newSolution;

    static List<Order> createNewOrderSequence(List<Installation> newInstSequence) {
        List<Order> newOrderSequence = new LinkedList<>();
        for (Installation installation : newInstSequence) {
            Set<Order> postponed = originalSolution.getPostponedOrders();
            List<Order> ordersFromInst = Problem.getScheduledOrdersFromInstallation(installation, postponed);
            Collections.sort(ordersFromInst);
            newOrderSequence.addAll(ordersFromInst);
        }
        return newOrderSequence;
    }

    static List<Installation> rmInstFromSequence(List<Installation> instSequence, Installation inst) {
        List<Installation> instSequenceExclInst = Helpers.deepCopyList(instSequence, false);
        instSequenceExclInst.remove(inst);
        return instSequenceExclInst;
    }

    static List<Installation> addInstToPosition(List<Installation> instSequence, Installation inst, int idx) {
        List<Installation> newInstSequence = Helpers.deepCopyList(instSequence, false);
        newInstSequence.add(idx, inst);
        return newInstSequence;
    }

    static Map<Integer, Double> createVesselToCost(Solution solution) {
        Map<Integer, Double> vesselToCostMap = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            double cost = orderSequence.isEmpty() ? 0.0 : Objective.runSPLean(orderSequence, vesselIdx); // Cached
            vesselToCostMap.put(vesselIdx, cost);
        }
        return vesselToCostMap;
    }
}
