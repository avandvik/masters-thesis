package localsearch;

import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class Operator {

    protected static Solution originalSolution;
    protected static Solution newSolution;

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


}
