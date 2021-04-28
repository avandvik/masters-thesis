package localsearch;

import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperatorSchedulePostponed extends OperatorTwo {

    public static Solution schedulePostponedOrder(Solution solution) {
        initialize(solution);
        Set<Order> postponedOrders = originalSolution.getPostponedOrders();
        for (Order order : postponedOrders) {
            Installation inst = Problem.getInstallation(order);
            List<Order> scheduledOrders = Problem.getScheduledOrdersFromInstallation(inst, postponedOrders);
            List<List<Order>> rmOrderSequences = removeOrdersFromSequences(scheduledOrders);
            for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
                List<Order> rmOrderSequence = rmOrderSequences.get(vesselIdx);
                List<Installation> rmInstSequence = Helpers.getInstSequence(rmOrderSequence);
                for (int insertIdx = 0; insertIdx <= rmInstSequence.size(); insertIdx++) {
                    List<Installation> newInstSequence = addInstToPosition(rmInstSequence, inst, insertIdx);
                    scheduledOrders.add(order);  // Orders to place
                    List<Order> newOrderSequence = createNewOrderSequence(newInstSequence, scheduledOrders, insertIdx);
                }
            }
        }

        return solution;
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

    private static List<Order> createNewOrderSequence(List<Installation> newInstSequence, List<Order> orders, int idx) {
        List<Order> newOrderSequence = new ArrayList<>();
        return newOrderSequence;
    }
}
