package alns.heuristics;

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

    public static void greedyVol2(Solution partialSolution, Set<Order> ordersToPlace) {

        Solution solutionCopy = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = solutionCopy.getOrderSequences();

        Objective.runMultipleSPInsertion(orderSequences, ordersToPlace);
        Objective.runMultipleSPEvaluate(orderSequences);

        double leastIncrease = Double.POSITIVE_INFINITY;
        List<Integer> bestInsertion = null;
        Order bestOrder = null;
        for (Order order : ordersToPlace) {
            Map<List<Integer>, Double> insertionToObj = SubProblemInsertion.orderToInsertionToObjective.get(order);
            for (List<Integer> insertion : insertionToObj.keySet()) {
                int vesselIdx = insertion.get(0);
                double increase = insertionToObj.get(insertion) - SubProblem.vesselToObjective.get(vesselIdx);
                if (increase < leastIncrease) {
                    leastIncrease = increase;
                    bestInsertion = insertion;
                    bestOrder = order;
                }
            }
        }

        // Postponement

        int vesselIdx = bestInsertion.get(0);
        int insertionIdx = bestInsertion.get(1);
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
        List<Order> sortedUnplacedOrders = Helpers.sortUnplacedOrders(partialSolution.getUnplacedOrders());
        Solution solution = partialSolution;
        for (Order order : sortedUnplacedOrders) solution = getGreedyInsertion(solution, order);
        return new Solution(solution.getOrderSequences(), solution.getPostponedOrders(), true);
    }
}
