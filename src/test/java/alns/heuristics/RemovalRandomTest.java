package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RemovalRandomTest {

    @Test
    @DisplayName("test RemovalRandom")
    public void removalRandomTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        RemovalRandom removalRandom = new RemovalRandom("random removal", true, false);
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, 5, Problem.getNumberOfOrders());
        testNumberOfRemovals(removalRandom, solution);
        testNoRemovals(removalRandom, solution);
        testRemovalsAsExpected(removalRandom);
    }

    private void testNumberOfRemovals(RemovalRandom removalRandom, Solution solution) {
        int ordersToRemove = 2;
        int ordersBefore = 0;
        int ordersAfter = 0;
        for (List<Order> orderSequence : solution.getOrderSequences()) ordersBefore += orderSequence.size();
        Solution partialSolution = removalRandom.destroy(solution, ordersToRemove);
        for (List<Order> orderSequence : partialSolution.getOrderSequences()) ordersAfter += orderSequence.size();
        assertEquals(ordersBefore, ordersAfter + ordersToRemove);
    }

    private void testNoRemovals(RemovalRandom removalRandom, Solution solution) {
        Solution partialSolution = removalRandom.destroy(solution, 0);
        assertEquals(solution, partialSolution);
    }

    private void testRemovalsAsExpected(RemovalRandom removalRandom) {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, 3, 5);
        Solution partialSolution = removalRandom.destroy(solution, 3);
        Solution expectedSolution = createExpectedSolutionThree();
        assertEquals(expectedSolution, partialSolution);
    }

    private Solution createExpectedSolutionThree() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        Set<Order> postponedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(6)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
