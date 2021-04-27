package localsearch;

import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class Operator {

    protected static Solution originalSolution;
    protected static Solution newSolution;

    public static List<Order> createNewOrderSequence(List<Installation> newInstSequence) {
        List<Order> newOrderSequence = new LinkedList<>();
        for (Installation installation : newInstSequence) {
            Set<Order> postponed = originalSolution.getPostponedOrders();
            List<Order> ordersFromInst = Problem.getScheduledOrdersFromInstallation(installation, postponed);
            Collections.sort(ordersFromInst);
            newOrderSequence.addAll(ordersFromInst);
        }
        return newOrderSequence;
    }
}
