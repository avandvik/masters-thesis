package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Constants;
import data.Parameters;
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
        RemovalRandom removalRandom = new RemovalRandom(Constants.REMOVAL_RANDOM_NAME);
        Solution solution = SolutionGenerator.createSolutionBasicTestData(3, Problem.getNumberOfOrders());
        testNumberOfRemovals(removalRandom, solution);
        testNoRemovals(removalRandom, solution);
        testRemovalsAsExpected(removalRandom, solution);
    }

    private void testNumberOfRemovals(RemovalRandom removalRandom, Solution solution) {
        Parameters.minPercentage = 0.2;
        Parameters.maxPercentage = 0.2;
        int ordersToRemove = 2;
        int ordersBefore = 0;
        int ordersAfter = 0;
        for (List<Order> orderSequence : solution.getOrderSequences()) ordersBefore += orderSequence.size();
        Solution partialSolution = removalRandom.destroy(solution);
        for (List<Order> orderSequence : partialSolution.getOrderSequences()) ordersAfter += orderSequence.size();
        assertEquals(ordersBefore, ordersAfter + ordersToRemove);  // One more is removed
    }

    private void testNoRemovals(RemovalRandom removalRandom, Solution solution) {
        Parameters.minPercentage = 0.0;
        Parameters.maxPercentage = 0.0;
        Parameters.minOrdersRemove = 0;
        Solution partialSolution = removalRandom.destroy(solution);
        assertEquals(solution, partialSolution);
    }

    private void testRemovalsAsExpected(RemovalRandom removalRandom, Solution solution) {
        Parameters.minPercentage = 0.3;
        Parameters.maxPercentage = 0.3;
        Solution partialSolution = removalRandom.destroy(solution);
        Solution expectedSolution = createExpectedSolutionThree();
        assertEquals(expectedSolution, partialSolution);
    }

    private Solution createExpectedSolutionThree() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(7))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(4), Problem.getOrder(6),
                Problem.getOrder(3)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
