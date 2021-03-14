package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import utils.Helpers;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RemovalWorstTest {

    @Test
    @DisplayName("test RemovalWorst")
    public void removalWorstTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        RemovalWorst removalWorst = new RemovalWorst("worst removal", true, false);
        Solution solution = createInitialSolution();
        setPostponementPenaltyMaxOrMin(solution, true);
        testNumberOfRemovals(removalWorst, solution);
        testNoRemovals(removalWorst, solution);
        testRemovalsAsExpectedHighPenalty(removalWorst, solution);
        setPostponementPenaltyMaxOrMin(solution, false);
        testRemovalsAsExpectedLowPenalty(removalWorst, solution);
    }

    private void testNumberOfRemovals(RemovalWorst removalWorst, Solution solution) {
        int ordersToRemove = 3;
        int ordersBefore = 0;
        int ordersAfter = 0;
        for (List<Order> orderSequence : solution.getOrderSequences()) ordersBefore += orderSequence.size();
        ordersBefore += solution.getPostponedOrders().size();
        Solution partialSolution = removalWorst.destroy(solution, ordersToRemove);
        for (List<Order> orderSequence : partialSolution.getOrderSequences()) ordersAfter += orderSequence.size();
        assertEquals(ordersBefore, ordersAfter + ordersToRemove);
    }

    private void testNoRemovals(RemovalWorst removalWorst, Solution solution) {
        Solution partialSolution = removalWorst.destroy(solution, 0);
        assertEquals(solution, partialSolution);
    }

    private void testRemovalsAsExpectedHighPenalty(RemovalWorst removalWorst, Solution solution) {
        Solution solutionCopy = Helpers.deepCopySolution(solution);
        Solution partialSolution = removalWorst.destroy(solutionCopy, 3);
        Solution expectedSolution = createExpectedSolutionHighPenalty();
        assertEquals(expectedSolution, partialSolution);
    }

    private void testRemovalsAsExpectedLowPenalty(RemovalWorst removalWorst, Solution solution) {
        Solution solutionCopy = Helpers.deepCopySolution(solution);
        Solution partialSolution = removalWorst.destroy(solutionCopy, 2);
        Solution expectedSolution = createExpectedSolutionLowPenalty();
        assertEquals(expectedSolution, partialSolution);
    }

    private void setPostponementPenaltyMaxOrMin(Solution solution, boolean isMax) {
        for (List<Order> orderSequence : solution.getOrderSequences()) {
            for (Order order : orderSequence) {
                if (isMax) {
                    order.setPostponementPenalty(Double.POSITIVE_INFINITY);
                } else {
                    order.setPostponementPenalty(0.0);
                }
            }
        }
        for (Order order : solution.getPostponedOrders()) {
            if (isMax) {
                order.setPostponementPenalty(Double.POSITIVE_INFINITY);
            } else {
                order.setPostponementPenalty(0.0);
            }
        }
    }

    private Solution createInitialSolution() {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, 3);
        List<Order> charterVoyage = solution.getOrderSequence(2);
        solution.getPostponedOrders().addAll(charterVoyage.subList(2, charterVoyage.size()));
        charterVoyage.removeIf(solution.getPostponedOrders()::contains);
        return solution;
    }

    private Solution createExpectedSolutionHighPenalty() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6),
                Problem.getOrder(7)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolutionLowPenalty() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6),
                Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
