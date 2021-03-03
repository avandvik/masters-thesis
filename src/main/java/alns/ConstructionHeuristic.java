package alns;

import objects.Order;

import java.util.List;

public class ConstructionHeuristic {

    public static void getAllFeasibleInsertions(Solution solution, Order order) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (List<Order> orderSequence : orderSequences) {
            int vesselNumber = 0;
            for (int i = 0; i < orderSequence.size(); i++) {
                orderSequence.add(i,order);
                /*
                if (!Evaluator.isFeasibleLoad(orderSequence, Problem.getVessel(vesselNumber))) ||
                (!Evaluator.isFeasibleDuration(orderSequence)) || (!Evaluator.isFeasibleVisits());

                 */
            }
        }
    }
}
