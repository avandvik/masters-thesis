package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Messages;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorPostponeScheduled extends Operator {

    private static Map<Integer, Double> vesselToCost;
    private static double greatestDecrease;  // Negative means decrease in objective

    public static Solution postponeScheduled(Solution solution) {
        initialize(solution);
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            List<Order> orderSequence = originalSolution.getOrderSequence(vIdx);
            for (Order order : orderSequence) {
                if (order.isMandatory()) continue;
                List<Order> newOrderSequence = Helpers.deepCopyList(orderSequence, true);
                newOrderSequence.remove(order);
                double decrease = calculateDecrease(vIdx, newOrderSequence, order);
                updateFields(decrease, vIdx, newOrderSequence, order);
            }
        }
        if (!Evaluator.isSolutionFeasible(newSolution)) throw new IllegalStateException(Messages.infSolCreated);
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }

    public static void initialize(Solution solution) {
        originalSolution = solution;
        newSolution = Helpers.deepCopySolution(solution);
        vesselToCost = createVesselToCost(solution);
        greatestDecrease = 0.0;
    }

    private static void updateFields(double decrease, int vIdx, List<Order> newOrderSequence, Order scheduledOrder) {
        if (decrease < greatestDecrease) {
            greatestDecrease = decrease;
            newSolution.replaceOrderSequence(vIdx, newOrderSequence);
            newSolution.addPostponedOrder(scheduledOrder);
        }
    }

    private static double calculateDecrease(int vIdx, List<Order> newOrderSequence, Order scheduledOrder) {
        double decrease = scheduledOrder.getPostponementPenalty();
        decrease -= vesselToCost.get(vIdx) - Objective.runSP(newOrderSequence, vIdx);
        return decrease;
    }
}
