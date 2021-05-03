package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorSchedulePostponed extends Operator {

    private static Set<Order> postponedOrders;
    private static Map<Integer, Double> vesselToCost;
    private static double greatestDecrease;

    public static Solution schedulePostponed(Solution solution) {
        initialize(solution);
        for (Order order : postponedOrders) {
            Installation inst = Problem.getInstallation(order);
            List<Order> scheduledOrders = Problem.getScheduledOrdersFromInstallation(inst, postponedOrders);
            List<List<Order>> rmOrderSequences = removeOrdersFromSequences(scheduledOrders);
            for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
                List<Order> rmOrderSequence = rmOrderSequences.get(vIdx);
                List<Installation> rmInstSequence = Helpers.getInstSequence(rmOrderSequence);
                for (int insertIdx = 0; insertIdx <= rmInstSequence.size(); insertIdx++) {
                    List<Installation> newInstSequence = addInstToPosition(rmInstSequence, inst, insertIdx);
                    List<Order> ordersToPlace = getOrdersToPlace(scheduledOrders, order);
                    List<Order> newOrderSequence = createNewOrderSequence(newInstSequence, ordersToPlace, insertIdx);
                    List<List<Order>> newOrderSequences = Helpers.deepCopy2DList(rmOrderSequences);
                    newOrderSequences.set(vIdx, newOrderSequence);
                    updateFields(newOrderSequences, order);
                }
            }
        }
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }

    private static void initialize(Solution solution) {
        originalSolution = solution;
        newSolution = Helpers.deepCopySolution(solution);
        postponedOrders = solution.getPostponedOrders();
        vesselToCost = createVesselToCost(solution);
        greatestDecrease = 0.0;
    }

    private static List<List<Order>> removeOrdersFromSequences(List<Order> orders) {
        List<List<Order>> orderSequences = new ArrayList<>();
        for (List<Order> orderSequence : originalSolution.getOrderSequences()) {
            List<Order> rmOrderSequence = Helpers.deepCopyList(orderSequence, true);
            rmOrderSequence.removeAll(orders);
            orderSequences.add(rmOrderSequence);
        }
        return orderSequences;
    }

    private static List<Order> getOrdersToPlace(List<Order> scheduledOrders, Order postponedOrder) {
        List<Order> ordersToPlace = Helpers.deepCopyList(scheduledOrders, false);
        ordersToPlace.add(postponedOrder);
        return ordersToPlace;
    }

    private static List<Order> createNewOrderSequence(List<Installation> newInstSequence, List<Order> orders, int idx) {
        List<Order> newOrderSequence = new ArrayList<>();
        for (int instIdx = 0; instIdx < newInstSequence.size(); instIdx++) {
            if (instIdx == idx) {
                newOrderSequence.addAll(orders);
            } else {
                Installation inst = newInstSequence.get(instIdx);
                List<Order> scheduledOrdersFromInst = Problem.getScheduledOrdersFromInstallation(inst, postponedOrders);
                Collections.sort(scheduledOrdersFromInst);  // TODO: This is fragile! Sort MD - OD - OP
                newOrderSequence.addAll(scheduledOrdersFromInst);
            }
        }
        return newOrderSequence;
    }

    private static void updateFields(List<List<Order>> newOrderSequences, Order postponedOrder) {
        double decrease = calculateDecrease(newOrderSequences, postponedOrder);
        if (decrease < greatestDecrease) {
            greatestDecrease = decrease;
            newSolution.replaceOrderSequences(newOrderSequences);
            newSolution.removePostponedOrder(postponedOrder);
        }
    }

    private static double calculateDecrease(List<List<Order>> newOrderSequences, Order postponedOrder) {
        double decrease = -postponedOrder.getPostponementPenalty();
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            decrease += Objective.runSPLean(newOrderSequences.get(vIdx), vIdx) - vesselToCost.get(vIdx);
        }
        return decrease;
    }
}
