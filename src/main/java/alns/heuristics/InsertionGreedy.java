package alns.heuristics;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Messages;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;
import subproblem.SubProblemInsertion;
import utils.Helpers;

import java.util.*;

public class InsertionGreedy extends Heuristic implements Repairer {

    public InsertionGreedy(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    private double leastIncrease;
    private List<Integer> bestInsertion;
    private Order bestOrder;

    public Solution getGreedyInsertion2(Solution partialSolution, boolean parallel) {
        /* Finds and inserts the order in ordersToPlace that increases the objective the least */

        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> ordersToPlace = newSolution.getUnplacedOrders();

        if (parallel) {
            Objective.runMultipleSPInsertion(orderSequences, ordersToPlace);
            Objective.runMultipleSPEvaluate(orderSequences);
            this.findBestInsertionOrderSequencesPar(ordersToPlace);
        } else {
            this.findBestInsertionOrderSequencesSeq(orderSequences, ordersToPlace);
        }

        boolean postponement = this.findBestInsertionPostponedOrders(ordersToPlace);

        if (postponement) {
            newSolution.addPostponedOrder(bestOrder);
            newSolution.removeUnplacedOrder(bestOrder);
            return newSolution;
        }

        if (bestOrder == null) throw new IllegalStateException(Messages.cannotPlaceMDOrder);

        newSolution.insertInOrderSequence(bestInsertion.get(0), bestInsertion.get(1), bestOrder);
        newSolution.removeUnplacedOrder(bestOrder);
        return newSolution;
    }

    private void findBestInsertionOrderSequencesPar(Set<Order> ordersToPlace) {
        this.leastIncrease = Double.POSITIVE_INFINITY;
        this.bestInsertion = null;
        this.bestOrder = null;
        for (Order order : ordersToPlace) {
            Map<List<Integer>, Double> insertionToObj = SubProblemInsertion.orderToInsertionToObjective.get(order);
            for (List<Integer> insertion : insertionToObj.keySet()) {
                int vesselIdx = insertion.get(0);
                double increase = insertionToObj.get(insertion) - SubProblem.vesselToObjective.get(vesselIdx);
                if (increase < this.leastIncrease) {
                    this.leastIncrease = increase;
                    this.bestInsertion = insertion;
                    this.bestOrder = order;
                }
            }
        }
    }

    private void findBestInsertionOrderSequencesSeq(List<List<Order>> orderSequences, Set<Order> ordersToPlace) {
        this.leastIncrease = Double.POSITIVE_INFINITY;
        this.bestInsertion = null;
        this.bestOrder = null;
        for (Order order : ordersToPlace) {
            Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
            for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
                List<Order> orderSequence = orderSequences.get(vesselIdx);
                double currentObjective = Objective.runSPLean(orderSequence, vesselIdx);
                for (int insertionIdx : insertions.get(vesselIdx)) {
                    List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                    orderSequenceCopy.add(insertionIdx, order);
                    double increase = Objective.runSPLean(orderSequenceCopy, vesselIdx) - currentObjective;
                    if (increase < this.leastIncrease) {
                        this.leastIncrease = increase;
                        this.bestInsertion = new ArrayList<>(Arrays.asList(vesselIdx, insertionIdx));
                        this.bestOrder = order;
                    }
                }
            }
        }
    }

    private boolean findBestInsertionPostponedOrders(Set<Order> ordersToPlace) {
        for (Order order : ordersToPlace) {
            double increase = order.getPostponementPenalty();
            if (!order.isMandatory() && (increase < this.leastIncrease || this.bestOrder == null)) {
                this.leastIncrease = increase;
                this.bestOrder = order;
                return true;
            }
        }
        return false;
    }

    public static Solution getGreedyInsertion(Solution partialSolution, Order orderToPlace) {
        /* Inserts order in an available vessel, a spot vessel, or the set of postponed orders */

        List<List<Order>> orderSequences = Helpers.deepCopy2DList(partialSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(partialSolution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(partialSolution.getUnplacedOrders());
        Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, orderToPlace);
        double leastIncrease = Double.POSITIVE_INFINITY;
        List<Integer> bestInsertion = null;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            double currentObj = Objective.runSPLean(orderSequence, vesselNumber);
            for (int insertionIdx : insertions.get(vesselNumber)) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.add(insertionIdx, orderToPlace);
                double increase = Objective.runSPLean(orderSequenceCopy, vesselNumber) - currentObj;
                if (increase < leastIncrease) {
                    leastIncrease = increase;
                    bestInsertion = new ArrayList<>(Arrays.asList(vesselNumber, insertionIdx));
                }
            }
        }
        double increase = orderToPlace.getPostponementPenalty();
        if (!orderToPlace.isMandatory() && (increase < leastIncrease || bestInsertion == null)) {
            postponedOrders.add(orderToPlace);
            unplacedOrders.remove(orderToPlace);
            return new Solution(orderSequences, postponedOrders, unplacedOrders);
        }

        if (bestInsertion == null) throw new IllegalStateException(Messages.cannotPlaceMDOrder);

        orderSequences.get(bestInsertion.get(0)).add(bestInsertion.get(1), orderToPlace);
        unplacedOrders.remove(orderToPlace);

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        while (!solution.getUnplacedOrders().isEmpty()) {
            solution = getGreedyInsertion2(solution, false);
        }
        Objective.setObjValAndSchedule(solution);
        if (!Evaluator.isSolutionFeasible(solution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return solution;
        /*
        List<Order> sortedUnplacedOrders = Helpers.sortUnplacedOrders(partialSolution.getUnplacedOrders());
        Solution solution = partialSolution;
        for (Order order : sortedUnplacedOrders) solution = getGreedyInsertion(solution, order);
        return new Solution(solution.getOrderSequences(), solution.getPostponedOrders(), true);

         */
    }
}
