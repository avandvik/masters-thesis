package localsearch;

import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
}
