package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorPostponeScheduled extends Operator {

    private static Map<Integer, Double> vesselToCost;
    private static double postponedOrdersCost;

    public static Solution postponeScheduled(Solution solution) {
        initialize(solution);
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            double bestCost = calculateOriginalCost(vIdx); // best cost of order sequence and postponed order set
            List<Order> orderSequence = originalSolution.getOrderSequence(vIdx);
            for (Order order : orderSequence) {
                if (order.isMandatory()) continue;
                Solution candidateSolution = Helpers.deepCopySolution(originalSolution);
                candidateSolution.getOrderSequence(vIdx).remove(order);
                candidateSolution.getPostponedOrders().add(order);
                bestCost = updateFields(order, candidateSolution, vIdx, bestCost);
            }
        }
        return newSolution;
    }

    public static void initialize(Solution solution) {
        originalSolution = solution;
        vesselToCost = createVesselToCost(solution);
        postponedOrdersCost = originalSolution.getPenaltyCosts();
        newSolution = Helpers.deepCopySolution(solution);
    }

    private static double calculateOriginalCost(int vIdx) {
        return (vesselToCost.get(vIdx) + postponedOrdersCost);
    }

    private static double calculateNewCost(Solution solution, int vIdx) {
        return (Objective.runSPLean(solution.getOrderSequence(vIdx), vIdx) + solution.getPenaltyCosts());
    }

    private static double updateFields(Order order, Solution solution, int vIdx, double bestCost) {
        double newCost = calculateNewCost(solution, vIdx);
        if (newCost < bestCost) {
            List<Order> orderSequence = solution.getOrderSequence(vIdx);
            newSolution.replaceOrderSequence(vIdx, orderSequence);
            replacePostponedOrders(order, newSolution, vIdx);
            return newCost;
        }
        return bestCost;
    }

    private static void replacePostponedOrders(Order order, Solution solution, int vIdx) {
        for (Order candidateOrder : originalSolution.getOrderSequence(vIdx)) {
            // Remove orders from same order sequence that have been postponed
            solution.getPostponedOrders().remove(candidateOrder);
        }
        solution.getPostponedOrders().add(order);
    }

}
