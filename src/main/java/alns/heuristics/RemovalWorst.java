package alns.heuristics;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;
import subproblem.SubProblemRemoval;
import utils.Helpers;

import java.util.*;

public class RemovalWorst extends Heuristic implements Destroyer {

    private double greatestDecrease;
    private List<Integer> worstRemovalIndices;
    private Order postponedOrder;

    public RemovalWorst(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    public Solution newDestroy(Solution solution, int numberOfOrders) {
        Solution newSolution = solution;
        while (newSolution.getUnplacedOrders().size() < numberOfOrders) newSolution = getWorstRemoval(newSolution);
        if (!Evaluator.isPartFeasible(newSolution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return newSolution;
    }

    private Solution getWorstRemoval(Solution solution) {
        /*  */

        Solution newSolution = Helpers.deepCopySolution(solution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();

        if (Parameters.parallelHeuristics) {
            Objective.runMultipleSPRemoval(orderSequences);
            Objective.runMultipleSPEvaluate(orderSequences);
            this.findGreatestDecreaseOrderSequencesPar();
        } else {
            this.findGreatestDecreaseOrderSequencesSeq(orderSequences);
        }

        this.findGreatestDecreasePostponement(orderSequences);

        List<Order> ordersToRemove = findOrdersToRemove(orderSequences);
        newSolution.getUnplacedOrders().addAll(ordersToRemove);
        newSolution.getPostponedOrders().removeAll(ordersToRemove);
        for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);

        return newSolution;
    }

    private void findGreatestDecreaseOrderSequencesPar() {
        this.greatestDecrease = Double.NEGATIVE_INFINITY;
        this.worstRemovalIndices = null;
        for (List<Integer> removalIndices : SubProblemRemoval.removalToObjective.keySet()) {
            int vesselIdx = removalIndices.get(0);
            double removalObj = SubProblemRemoval.removalToObjective.get(removalIndices);
            double currentObj = SubProblem.vesselToObjective.get(vesselIdx);
            double decrease = currentObj - removalObj;
            if (decrease > this.greatestDecrease) {
                this.greatestDecrease = decrease;
                this.worstRemovalIndices = removalIndices;
            }
        }
    }

    private void findGreatestDecreaseOrderSequencesSeq(List<List<Order>> orderSequences) {
        this.worstRemovalIndices = null;
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            double currentObj = Objective.runSPLean(orderSequence, vesselIdx);
            for (int orderIdx = 0; orderIdx < orderSequence.size(); orderIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.remove(orderIdx);
                double decrease = currentObj - Objective.runSPLean(orderSequenceCopy, vesselIdx);
                if (decrease > this.greatestDecrease) {
                    this.greatestDecrease = decrease;
                    worstRemovalIndices = new ArrayList<>(Arrays.asList(vesselIdx, orderIdx));
                }
            }
        }
    }

    private void findGreatestDecreasePostponement(List<List<Order>> orderSequences) {
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            for (Order order : orderSequence) {
                double decrease = order.getPostponementPenalty();
                if (!order.isMandatory() && (decrease > this.greatestDecrease || this.worstRemovalIndices == null)) {
                    this.greatestDecrease = decrease;
                    this.postponedOrder = order;
                }
            }
        }
    }

    private List<Order> findOrdersToRemove(List<List<Order>> orderSequences) {
        List<Order> ordersToRemove = new ArrayList<>();
        if (this.postponedOrder != null) {
            ordersToRemove = getOrdersToRemove(this.postponedOrder);
        } else if (this.worstRemovalIndices != null) {
            int vesselIdx = this.worstRemovalIndices.get(0);
            int orderIdx = this.worstRemovalIndices.get(1);
            Order order = orderSequences.get(vesselIdx).get(orderIdx);
            ordersToRemove = getOrdersToRemove(order);
        }
        return ordersToRemove;
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
                for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);
            } else if (orderIdxToRemove != null) {
                int vesselIdx = orderIdxToRemove.get(0);
                int orderIdx = orderIdxToRemove.get(1);
                Order orderToRemove = orderSequences.get(vesselIdx).remove(orderIdx);
                List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
                unplacedOrders.addAll(ordersToRemove);
                orderSequences.get(vesselIdx).removeAll(ordersToRemove);
                postponedOrders.removeAll(ordersToRemove);
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
            double currentObj = Objective.runSPLean(orderSequence, vesselNumber);
            for (int orderIdx = 0; orderIdx < orderSequence.size(); orderIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.remove(orderSequenceCopy.get(orderIdx));
                double decrease = currentObj - Objective.runSPLean(orderSequenceCopy, vesselNumber);
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
