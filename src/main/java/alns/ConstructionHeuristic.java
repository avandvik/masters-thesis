package alns;

import objects.Order;

import java.util.ArrayList;
import java.util.List;

public class ConstructionHeuristic {

    public static List<Solution> getAllFeasibleInsertions(Solution solution, Order order) {
        List<Solution> solutions = new ArrayList<>();
        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (List<Order> orderSequence : orderSequences) {
            int vesselNumber = 0;
            if (orderSequence.isEmpty()) {
                orderSequence.add(order);
                Solution newSolution = new Solution(orderSequences);
                if (!Evaluator.isFeasibleLoad(newSolution) || !Evaluator.isFeasibleDuration(newSolution) ||
                        !Evaluator.isFeasibleVisits(newSolution)) {
                    orderSequence.remove(0);
                    continue;
                }
                solutions.add(newSolution);
            }
            for (int i = 0; i < orderSequence.size(); i++) {
                orderSequence.add(i,order);
                Solution newSolution = new Solution(orderSequences);

                if(!Evaluator.isFeasibleLoad(newSolution) || !Evaluator.isFeasibleDuration(newSolution) ||
                !Evaluator.isFeasibleVisits(newSolution)) {
                    orderSequence.remove(i);
                    continue;
                }
                solutions.add(newSolution);
            }
        }
        return solutions;
    }
}
