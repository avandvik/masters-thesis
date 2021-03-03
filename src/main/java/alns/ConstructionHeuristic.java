package alns;

import data.Problem;
import objects.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ConstructionHeuristic {

    public static List<Solution> getAllFeasibleInsertions(Solution solution, Order order) {
        List<Solution> solutions = new ArrayList<>();

        // Copy orderSequences of original solution
        List<List<Order>> orderSequences = new ArrayList<>();
        for (List<Order> orderSequence : solution.getOrderSequences()) {
            orderSequences.add(new LinkedList<>(orderSequence));
        }

        Solution newSolution;
        for (List<Order> orderSequence : orderSequences) {
            orderSequence.add(0, order);
            newSolution = new Solution(orderSequences);
            if (Evaluator.isFeasible(newSolution)) solutions.add(newSolution);
            orderSequence.remove(0);

            for (int i = 1; i <= orderSequence.size(); i++) {
                orderSequence.add(i, order);
                newSolution = new Solution(orderSequences);
                if (Evaluator.isFeasible(newSolution)) solutions.add(newSolution);
                orderSequence.remove(i);
            }
        }
        return solutions;
    }

    public static List<List<Integer>> getAllFeasibleInsertions(List<List<Order>> orderSequences, Order order) {
        // Copy orderSequences of original solution
        List<List<Order>> orderSequencesCopy = new ArrayList<>();
        for (List<Order> orderSequence : orderSequences) orderSequencesCopy.add(new LinkedList<>(orderSequence));

        List<List<Integer>> insertionIndices = new ArrayList<>();
        for (List<Order> orderSequence : orderSequencesCopy) {
            List<Integer> vesselIndices = new ArrayList<>();
            orderSequence.add(0, order);
            if (Evaluator.isFeasible(new Solution(orderSequences))) vesselIndices.add(0);
            orderSequence.remove(0);
            for (int i = 1; i <= orderSequence.size(); i++) {
                orderSequence.add(i, order);
                if (Evaluator.isFeasible(new Solution(orderSequences))) vesselIndices.add(i);
                orderSequence.remove(i);
            }
            insertionIndices.add(vesselIndices);
        }

        return insertionIndices;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false);
        List<Order> orders = Problem.orders;
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());

        for (int i = 0; i < 2; i++) {
            orderSequences.get(0).add(orders.get(i));
        }
        for (int j = 2; j < 5; j++) {
            orderSequences.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 7; k++) {
            orderSequences.get(2).add(orders.get(k));
        }

        System.out.println(orderSequences);
        System.out.println();

        Solution solution = new Solution(orderSequences);
        Order orderToBePlaced = Problem.orders.get(Problem.orders.size() - 1);
        List<Solution> insertions = getAllFeasibleInsertions(solution, orderToBePlaced);
        System.out.println(insertions);
        List<List<Integer>> indices = getAllFeasibleInsertions(orderSequences, orderToBePlaced);
        System.out.println(indices);
    }
}
