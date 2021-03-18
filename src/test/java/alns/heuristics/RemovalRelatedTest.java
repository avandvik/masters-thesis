package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import utils.Helpers;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RemovalRelatedTest {

    @Test
    @DisplayName("test RemovalRelated")
    public void removalRelatedTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Parameters.rnRelated = 0;
        RemovalRelated removalRelated = new RemovalRelated("related removal", true, false);

        Solution solution = createInitialSolution();
        testNumberOfRemovals(removalRelated, solution);
        testNoRemovals(removalRelated, solution);
        testRemovalsAsExpected(removalRelated, solution);
    }

    private Solution createInitialSolution() {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, 3);
        List<Order> charterVoyage = solution.getOrderSequence(2);
        solution.getPostponedOrders().addAll(charterVoyage.subList(2, charterVoyage.size()));
        charterVoyage.removeIf(solution.getPostponedOrders()::contains);
        return solution;
    }

    private void testNumberOfRemovals(RemovalRelated removalRelated, Solution solution) {
        int ordersToRemove = 3;
        int ordersBefore = 0;
        int ordersAfter = 0;
        Solution copyOfSolution = Helpers.deepCopySolution(solution);
        for (List<Order> orderSequence : copyOfSolution.getOrderSequences()) ordersBefore += orderSequence.size();
        ordersBefore += copyOfSolution.getPostponedOrders().size();
        Solution partialSolution = removalRelated.destroy(solution, ordersToRemove);
        for (List<Order> orderSequence : partialSolution.getOrderSequences()) ordersAfter += orderSequence.size();
        ordersToRemove += partialSolution.getPostponedOrders().size();
        assertEquals(ordersBefore, ordersAfter + ordersToRemove);
    }

    private void testNoRemovals(RemovalRelated removalRelated, Solution solution) {
        Solution partialSolution = removalRelated.destroy(solution, 0);
        assertEquals(solution, partialSolution);
    }

    private void testRemovalsAsExpected(RemovalRelated removalRelated, Solution solution) {
        Solution partialSolution = removalRelated.destroy(solution, 3);
        Solution expectedSolution = createdExpectedSolutionThree();
        assertEquals(partialSolution, expectedSolution);
    }

    private Solution createdExpectedSolutionThree() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        Set<Order> postponedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(6),
                Problem.getOrder(1)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

}
