package alns.heuristics;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Messages;
import data.Parameters;
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

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        while (!solution.getUnplacedOrders().isEmpty()) solution = getGreedyInsertion(solution);
        Objective.setObjValAndSchedule(solution);
        if (!Evaluator.isSolutionFeasible(solution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return solution;
    }

    public Solution getGreedyInsertion(Solution partialSolution) {
        /* Finds and inserts the order in ordersToPlace that increases the objective the least */

        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> ordersToPlace = newSolution.getUnplacedOrders();
        if (Parameters.parallelHeuristics) {
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
        outer:
        for (Order order : ordersToPlace) {
            Map<List<Integer>, Double> insertionToObj = SubProblemInsertion.orderToInsertionToObjective.get(order);
            for (List<Integer> insertion : insertionToObj.keySet()) {
                int vesselIdx = insertion.get(0);
                double increase = insertionToObj.get(insertion) - SubProblem.vesselToObjective.get(vesselIdx);
                if (increase < this.leastIncrease) {
                    if (instHasMandUnplacedOrder(order, ordersToPlace)) continue outer;
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
        outer:
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
                        if (instHasMandUnplacedOrder(order, ordersToPlace)) continue outer;
                        this.leastIncrease = increase;
                        this.bestInsertion = new ArrayList<>(Arrays.asList(vesselIdx, insertionIdx));
                        this.bestOrder = order;
                    }
                }
            }
        }
    }

    private boolean instHasMandUnplacedOrder(Order order, Set<Order> ordersToPlace) {
        if (!order.isMandatory()) {
            Order mandOrder = Problem.getMandatoryOrder(order);
            return mandOrder != null && ordersToPlace.contains(mandOrder);
        }
        return false;
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
}
