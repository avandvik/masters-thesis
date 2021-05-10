package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;
import subproblem.SubProblemRemoval;
import utils.Helpers;

import java.util.*;

public class RemovalWorst extends Heuristic implements Destroyer {

    private Map<Order, Double> orderToDecrease;

    public RemovalWorst(String name) {
        super(name);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        Solution newSolution = solution;
        while (newSolution.getUnplacedOrders().size() < numberOfOrders) newSolution = getWorstRemoval(newSolution);
        // if (!Evaluator.isPartFeasible(newSolution)) throw new IllegalStateException(Messages.solutionInfeasible);
        newSolution.clearSubProblemResults();
        return newSolution;
    }

    private Solution getWorstRemoval(Solution solution) {
        /*  */

        Solution newSolution = Helpers.deepCopySolution(solution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> postponedOrders = newSolution.getAllPostponed();

        if (Parameters.parallelHeuristics) {
            Objective.runMultipleSPRemoval(orderSequences);
            Objective.runMultipleSPEvaluate(orderSequences);
            this.mapOrderToDecreasePar(orderSequences, postponedOrders);
        } else {
            this.mapOrderToDecreaseSeq(orderSequences, postponedOrders);
        }

        List<Order> ordersToRemove = findOrdersToRemove();
        newSolution.getUnplacedOrders().addAll(ordersToRemove);
        ordersToRemove.forEach(newSolution.getAllPostponed()::remove);
        for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);

        return newSolution;
    }

    private void mapOrderToDecreasePar(List<List<Order>> orderSequences, Set<Order> postponedOrders) {
        this.orderToDecrease = new HashMap<>();
        for (List<Integer> removalIndices : SubProblemRemoval.removalToObjective.keySet()) {
            int vesselIdx = removalIndices.get(0);
            int orderIdx = removalIndices.get(1);
            double removalObj = SubProblemRemoval.removalToObjective.get(removalIndices);
            double currentObj = SubProblem.vesselToObjective.get(vesselIdx);
            double decrease = currentObj - removalObj;
            Order order = orderSequences.get(vesselIdx).get(orderIdx);
            this.orderToDecrease.put(order, decrease);
        }
        for (Order order : postponedOrders) this.orderToDecrease.put(order, order.getPostponementPenalty());
    }

    private void mapOrderToDecreaseSeq(List<List<Order>> orderSequences, Set<Order> postponedOrders) {
        this.orderToDecrease = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            double currentObj = Objective.runSP(orderSequence, vesselIdx);
            for (int orderIdx = 0; orderIdx < orderSequence.size(); orderIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.remove(orderIdx);
                double decrease = currentObj - Objective.runSP(orderSequenceCopy, vesselIdx);
                Order order = orderSequences.get(vesselIdx).get(orderIdx);
                this.orderToDecrease.put(order, decrease);
            }
        }
        for (Order order : postponedOrders) this.orderToDecrease.put(order, order.getPostponementPenalty());
    }

    private List<Order> findOrdersToRemove() {
        List<Map.Entry<Order, Double>> ordersWithDecrease = new ArrayList<>(this.orderToDecrease.entrySet());
        ordersWithDecrease.sort(Comparator.comparing(Map.Entry<Order, Double>::getValue));
        Collections.reverse(ordersWithDecrease);
        int removeIdx = (int) (Math.pow(Problem.random.nextDouble(), Parameters.rnWorst) * ordersWithDecrease.size());
        Order worstRemovalOrder = ordersWithDecrease.get(removeIdx).getKey();
        return getOrdersToRemove(worstRemovalOrder);
    }
}
