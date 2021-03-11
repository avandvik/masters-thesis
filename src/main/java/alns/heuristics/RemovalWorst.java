package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RemovalWorst extends Heuristic implements Destroyer {

    private double greatestDecrease;

    public RemovalWorst(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());

        if (unplacedOrders.size() > 0) throw new IllegalStateException(Messages.unplacedOrdersNotEmpty);

        while (unplacedOrders.size() < numberOfOrders) {
            this.greatestDecrease = Double.NEGATIVE_INFINITY;
            List<Integer> orderIdxToRemove = findWorstRemovalOrderSequences(orderSequences);
            Order postponedOrderToRemove = findWorstRemovalPostponedOrders(postponedOrders);

            if (postponedOrderToRemove != null) {  // Removing the postponed order will then be best
                List<Order> ordersToRemove = getOrdersToRemove(postponedOrderToRemove);
                unplacedOrders.addAll(ordersToRemove);
                postponedOrders.removeAll(ordersToRemove);
            } else if (orderIdxToRemove != null) {
                int vesselIdx = orderIdxToRemove.get(0);
                int orderIdx = orderIdxToRemove.get(1);
                Order orderToRemove = orderSequences.get(vesselIdx).remove(orderIdx);
                List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
                unplacedOrders.addAll(ordersToRemove);
                orderSequences.get(vesselIdx).removeIf(unplacedOrders::contains);
            } else {
                break;
            }
        }

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private List<Integer> findWorstRemovalOrderSequences(List<List<Order>> orderSequences) {
        List<Integer> worstRemovalIndices = null;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            double currentObj = Objective.runSubProblemLean(orderSequence, vesselNumber);
            for (int orderIdx = 0; orderIdx < orderSequence.size(); orderIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.remove(orderSequenceCopy.get(orderIdx));
                double decrease = currentObj - Objective.runSubProblemLean(orderSequenceCopy, vesselNumber);
                if (decrease > this.greatestDecrease) {
                    this.greatestDecrease = decrease;
                    worstRemovalIndices = new ArrayList<>(Arrays.asList(vesselNumber, orderIdx));
                }
            }
        }
        return worstRemovalIndices;
    }

    private Order findWorstRemovalPostponedOrders(Set<Order> postponedOrders) {
        Order worstRemovalPostponedOrder = null;
        for (Order postponedOrder : postponedOrders) {
            double decrease = postponedOrder.getPostponementPenalty();
            if (decrease > this.greatestDecrease) {
                this.greatestDecrease = decrease;
                worstRemovalPostponedOrder = postponedOrder;
            }
        }
        return worstRemovalPostponedOrder;
    }
}
