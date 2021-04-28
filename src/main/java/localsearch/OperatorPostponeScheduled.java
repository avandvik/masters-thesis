package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorPostponeScheduled extends Operator {

    private static Map<Integer, Double> vesselToCost;
    private static double postponedOrdersCost;

    public static Solution postponeScheduled(Solution solution) {
        initialize(solution);
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            double bestCost = calculateOriginalCost(vesselIdx);
            List<Order> orderSequence = originalSolution.getOrderSequence(vesselIdx);
            for (Order order : orderSequence) {
                Solution tempSolution = Helpers.deepCopySolution(originalSolution);
                tempSolution.getOrderSequence(vesselIdx).remove(order);
                tempSolution.getPostponedOrders().add(order);
                bestCost = updateFields(order, tempSolution, vesselIdx, bestCost);
            }
        }
        return newSolution;
    }

    public static void initialize(Solution solution) {
        originalSolution = solution;
        vesselToCost = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            double cost = orderSequence.isEmpty() ? 0.0 : Objective.runSPLean(orderSequence, vesselIdx); // Cached
            vesselToCost.put(vesselIdx, cost);
        }
        postponedOrdersCost = originalSolution.getPenaltyCosts();
        newSolution = Helpers.deepCopySolution(solution);
    }

    private static double calculateOriginalCost(int vesselIdx) {
        return (vesselToCost.get(vesselIdx) + postponedOrdersCost);
    }

    private static double calculateNewCost(Solution solution, int vesselIdx) {
        return (Objective.runSPLean(solution.getOrderSequence(vesselIdx), vesselIdx) + solution.getPenaltyCosts());
    }

    private static double updateFields(Order order, Solution solution, int vesselIdx, double bestCost) {
        double newCost = calculateNewCost(solution, vesselIdx);
        if (newCost < bestCost) {
            bestCost = newCost;
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            newSolution.replaceOrderSequence(vesselIdx, orderSequence);
            replacePostponedOrders(order, newSolution, vesselIdx);
        }
        return bestCost;
    }

    private static void replacePostponedOrders(Order order, Solution solution, int vesselIdx) {
        for (Order candidateOrder : originalSolution.getOrderSequence(vesselIdx)) {
            // Remove orders from same order sequence that have been postponed
            solution.getPostponedOrders().remove(candidateOrder);
        }
        solution.getPostponedOrders().add(order);
    }

}
