package alns.heuristics;

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

    public InsertionGreedy(String name) {
        super(name);
    }

    private double leastIncrease;
    private List<Integer> bestInsertion;
    private Order bestOrder;

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        while (!solution.getUnplacedOrders().isEmpty()) solution = getGreedyInsertion(solution);
        Objective.setObjValAndSchedule(solution);
        return solution;
    }

    private Solution getGreedyInsertion(Solution partialSolution) {
        /* Finds and inserts the order in partialSolution's unplacedOrders that increases the objective the least */

        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> ordersToPlace = newSolution.getUnplacedOrders();
        if (Parameters.parallelHeuristics) {
            Objective.runMultipleSPInsertion(orderSequences, ordersToPlace);
            Objective.runMultipleSPEvaluate(orderSequences);
            this.findLeastIncreaseOrderSequencesPar(ordersToPlace);
        } else {
            this.findLeastIncreaseOrderSequencesSeq(orderSequences, ordersToPlace);
        }
        boolean postponement = this.findLeastIncreaseInsertionPostponedOrders(ordersToPlace);
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

    private void findLeastIncreaseOrderSequencesPar(Set<Order> ordersToPlace) {
        this.leastIncrease = Double.POSITIVE_INFINITY;
        this.bestInsertion = null;
        this.bestOrder = null;
        outer:
        for (Order order : ordersToPlace) {
            Map<List<Integer>, Double> insertionToObj = SubProblemInsertion.orderToInsertionToObjective.get(order);
            if (insertionToObj == null) continue;  // No valid insertions for order
            for (Map.Entry<List<Integer>, Double> entry : insertionToObj.entrySet()) {
                List<Integer> insertion = entry.getKey();
                double obj = entry.getValue();
                obj += Helpers.getRandomDouble(-Parameters.maxNoise, Parameters.maxNoise);
                double increase = obj - SubProblem.vesselToObjective.get(insertion.get(0));
                if (increase < this.leastIncrease) {
                    if (instHasMandUnplacedOrder(order, ordersToPlace)) continue outer;
                    this.leastIncrease = increase;
                    this.bestInsertion = insertion;
                    this.bestOrder = order;
                }
            }
        }
    }

    private void findLeastIncreaseOrderSequencesSeq(List<List<Order>> orderSequences, Set<Order> ordersToPlace) {
        this.leastIncrease = Double.POSITIVE_INFINITY;
        this.bestInsertion = null;
        this.bestOrder = null;
        outer:
        for (Order order : ordersToPlace) {
            Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
            for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
                List<Order> orderSequence = orderSequences.get(vIdx);
                double currentObj = Objective.runSP(orderSequence, vIdx);
                for (int insertionIdx : insertions.get(vIdx)) {
                    double increase = calculateIncrease(orderSequence, order, vIdx, insertionIdx, currentObj);
                    if (increase < this.leastIncrease) {
                        if (instHasMandUnplacedOrder(order, ordersToPlace)) continue outer;
                        this.leastIncrease = increase;
                        this.bestInsertion = new ArrayList<>(Arrays.asList(vIdx, insertionIdx));
                        this.bestOrder = order;
                    }
                }
            }
        }
    }

    private boolean findLeastIncreaseInsertionPostponedOrders(Set<Order> ordersToPlace) {
        boolean postponedOrder = false;
        for (Order order : ordersToPlace) {
            double increase = order.getPostponementPenalty();
            if (!order.isMandatory() && (increase < this.leastIncrease || this.bestOrder == null)) {
                this.leastIncrease = increase;
                this.bestOrder = order;
                postponedOrder = true;
            }
        }
        return postponedOrder;
    }

    public static Solution insertGreedilyInSolution(Solution partialSolution, Order order) {
        double leastIncrease = Double.POSITIVE_INFINITY;
        int bestVesselIdx = -1;
        List<Order> bestOrderSequence = null;
        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            List<Order> orderSequence = orderSequences.get(vIdx);
            double currentObj = Objective.runSP(orderSequence, vIdx);
            for (int insertionIdx : insertions.get(vIdx)) {
                double increase = calculateIncrease(orderSequence, order, vIdx, insertionIdx, currentObj);
                List<Order> newOrderSequence = Helpers.deepCopyList(orderSequence, true);
                newOrderSequence.add(insertionIdx, order);
                if (increase < leastIncrease) {
                    leastIncrease = increase;
                    bestVesselIdx = vIdx;
                    bestOrderSequence = newOrderSequence;
                }
            }
        }
        if (!order.isMandatory() && order.getPostponementPenalty() < leastIncrease) {
            newSolution.addPostponedOrder(order);
            newSolution.removeUnplacedOrder(order);
            return newSolution;
        }
        if (bestOrderSequence == null) throw new IllegalStateException(Messages.cannotPlaceMDOrder);
        newSolution.replaceOrderSequence(bestVesselIdx, bestOrderSequence);
        newSolution.removeUnplacedOrder(order);
        return newSolution;
    }
}
