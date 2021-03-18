package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RemovalRelatedTest {

    @Test
    @DisplayName("test RemovalRelated")
    public void removalRelatedTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        RemovalRelated removalRelated = new RemovalRelated("related removal", true, false);

        Solution solution = createInitialSolution();
        testNoRandomness(removalRelated, solution);
        testRandomness(removalRelated, solution);
        testNoRemovals(removalRelated, solution);
    }

    private Solution createInitialSolution() {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, 5);
        solution.addPostponedOrder(solution.getOrderSequence(2).remove(2));
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(0));
        return solution;
    }

    private void testNoRandomness(RemovalRelated removalRelated, Solution solution) {
        int ordersToRemove = 3;
        Parameters.rnRelated = 100;

        Solution partialSolution = removalRelated.destroy(solution, ordersToRemove);
        assertEquals(createExpectedSolutionNoRandomness(), partialSolution);
    }

    private void testRandomness(RemovalRelated removalRelated, Solution solution) {
        int ordersToRemove = 3;
        Parameters.rnRelated = 1;

        Solution partialSolution = removalRelated.destroy(solution, ordersToRemove);
        assertEquals(createExpectedSolutionRandomness(), partialSolution);
    }

    private void testNoRemovals(RemovalRelated removalRelated, Solution solution) {
        int ordersToRemove = 0;

        Solution partialSolution = removalRelated.destroy(solution, ordersToRemove);
        assertEquals(solution, partialSolution);
    }

    private Solution createExpectedSolutionNoRandomness() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(6))));
        Set<Order> postponedOrders = new HashSet<>(Collections.singletonList(Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(2), Problem.getOrder(5)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolutionRandomness() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6))));
        Set<Order> postponedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(7), Problem.getOrder(2)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(3), Problem.getOrder(4)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
