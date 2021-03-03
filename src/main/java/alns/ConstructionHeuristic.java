package alns;

import data.Problem;
import objects.Order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConstructionHeuristic {

    public static List<Solution> getAllFeasibleInsertions(Solution solution, Order order) {
        List<Solution> solutions = new ArrayList<>();
        List<List<Order>> orderSequences = solution.getOrderSequences();

        System.out.println(orderSequences);
        for (List<Order> orderSequence : orderSequences) {
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

    public static void main(String[] args) {
        Problem.setUpProblem("example.json",false);
        List<Order> orders = Problem.orders;
        List<List<Order>> orderSequences = new ArrayList<>();
        for(int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());

        for (int i = 0; i < 2; i++) {
            orderSequences.get(0).add(orders.get(i));
        }
        for (int j = 2; j < 5; j++) {
            orderSequences.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 7; k++) {
            orderSequences.get(2).add(orders.get(k));
        }

        Solution newSolution = new Solution(orderSequences);
        List<Solution> insertions = getAllFeasibleInsertions(newSolution,Problem.orders.get(Problem.orders.size()-1));
    }
}
