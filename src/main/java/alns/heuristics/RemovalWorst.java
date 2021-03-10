package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RemovalWorst extends Heuristic implements Destroyer {

    public RemovalWorst(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        List<Order> postponedOrdersList = new ArrayList<Order>();
        postponedOrdersList.addAll(postponedOrders);

        double greatestDecrease = Double.NEGATIVE_INFINITY;
        List<Integer> worstRemovalIndices = null;
        Order worstRemovalPostponedOrder = null;

        while (numberOfOrders > 0) {

            for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
                List<Order> orderSequence = orderSequences.get(vesselNumber);
                double currentObj = Objective.runSubProblemLean(orderSequence, vesselNumber);
                for (int orderIdx = 0; orderIdx < orderSequence.size(); orderIdx++) {
                    List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                    orderSequenceCopy.remove(orderSequenceCopy.get(orderIdx));
                    double decrease = currentObj - Objective.runSubProblemLean(orderSequenceCopy,vesselNumber);
                    if (decrease > greatestDecrease) {
                        greatestDecrease = decrease;
                        worstRemovalIndices = new ArrayList<>(Arrays.asList(vesselNumber, orderIdx));
                    }
                }
            }

            for (int orderIdx = 0; orderIdx < postponedOrdersList.size(); orderIdx++) {
                List<Order> postponedOrdersListCopy = Helpers.deepCopyList(postponedOrdersList, true);
                Order postponedOrder = postponedOrdersListCopy.get(orderIdx);
                double decrease = postponedOrder.getPostponementPenalty();
                if (decrease > greatestDecrease) {
                    greatestDecrease = decrease;
                    worstRemovalPostponedOrder = postponedOrdersList.get(orderIdx);
                }
            }

            if (worstRemovalPostponedOrder == null && worstRemovalIndices != null) {
                Order worstRemovalOrder = orderSequences.get(worstRemovalIndices.get(0)).get(worstRemovalIndices.get(1));
                unplacedOrders.add(worstRemovalOrder);
                orderSequences.remove(worstRemovalOrder);
            } else {
                unplacedOrders.add(worstRemovalPostponedOrder);
                postponedOrders.remove(worstRemovalPostponedOrder);
            }

            numberOfOrders--;
        }

        for (List<Order> orderSequence : orderSequences) orderSequence.removeIf(unplacedOrders::contains);
        postponedOrders.removeIf(unplacedOrders::contains);

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
