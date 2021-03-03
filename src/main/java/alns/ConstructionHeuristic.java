package alns;

import data.Problem;
import objects.Order;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ConstructionHeuristic {

    public static List<Solution> getAllFeasibleInsertions(Solution solution, Order order) {
        List<Solution> solutions = new ArrayList<>();

        // Copy orderSequences of original solution
        List<List<Order>> originalOrderSequences = new ArrayList<>();
        for (List<Order> orderSequence : solution.getOrderSequences()) {
            originalOrderSequences.add(new LinkedList<>(orderSequence));
        }

        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {

            for (int i = 0; i <= originalOrderSequences.get(vesselNumber).size(); i++) {

                List<Order> orderSequence = new LinkedList<>(originalOrderSequences.get(vesselNumber));
                orderSequence.add(i, order);

                List<List<Order>> orderSequences = new ArrayList<>();
                for (int j = 0; j < Problem.getNumberOfVessels(); j++) {
                    if (j == vesselNumber) {
                        orderSequences.add(orderSequence);
                        continue;
                    }
                    orderSequences.add(j, new LinkedList<>(originalOrderSequences.get(j)));
                }

                Solution newSolution = new Solution(orderSequences);
                if (Evaluator.isFeasible(newSolution)) solutions.add(newSolution);
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
            for (int i = 0; i <= orderSequence.size(); i++) {
                orderSequence.add(i, order);
                if (Evaluator.isFeasible(new Solution(orderSequencesCopy))) vesselIndices.add(i);
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

        // System.out.println(orderSequences);
        // System.out.println();

        Solution solution = new Solution(orderSequences);
        Order orderToBePlaced = Problem.orders.get(Problem.orders.size() - 1);
        List<Solution> insertions = getAllFeasibleInsertions(solution, orderToBePlaced);
        // insertions.stream().forEach(System.out::println);

        List<List<Integer>> indices = getAllFeasibleInsertions(orderSequences, orderToBePlaced);
        // System.out.println(indices);

    }
}
