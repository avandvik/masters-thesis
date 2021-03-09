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

public class InsertionGreedyTest {

    @Test
    @DisplayName("test InsertionGreedy")
    public void insertionGreedyTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        InsertionGreedy insertionGreedy = new InsertionGreedy("greedy insertion", false, true);
        testSingleInsertion(insertionGreedy);
        testDoubleInsertion(insertionGreedy);
        testPostponementInsertion(insertionGreedy);
    }

    private void testSingleInsertion(InsertionGreedy insertionGreedy) {
        Solution expectedSolution = SolutionGenerator.createSolutionBasicTestData(3, 5, Problem.getNumberOfOrders());
        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(expectedSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedSolution.getPostponedOrders());
        Set<Order> unplacedOrders = new HashSet<>(Collections.singletonList(partialOrderSequences.get(0).remove(0)));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);
        assertEquals(expectedSolution, insertionGreedy.repair(partialSolution));
    }

    private void testDoubleInsertion(InsertionGreedy insertionGreedy) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, 5, Problem.getNumberOfOrders());
        Solution expectedSolution = Helpers.copySolution(originalSolution);
        expectedSolution.getOrderSequences().get(0).add(0, expectedSolution.getOrderSequences().get(2).remove(1));
        expectedSolution.getOrderSequences().get(0).add(expectedSolution.getOrderSequences().get(0).remove(3));

        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(originalSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedSolution.getPostponedOrders());
        Order removedOrderOne = partialOrderSequences.get(0).remove(0);
        Order removedOrderTwo = partialOrderSequences.get(2).remove(1);
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(removedOrderOne, removedOrderTwo));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);
        assertEquals(expectedSolution, insertionGreedy.repair(partialSolution));
    }

    private void testPostponementInsertion(InsertionGreedy insertionGreedy) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, 5, Problem.getNumberOfOrders());
        Solution expectedSolution = Helpers.copySolution(originalSolution);
        Order removedOrder = expectedSolution.getOrderSequences().get(0).remove(2);
        expectedSolution.getPostponedOrders().add(removedOrder);

        Solution partialSolution = Helpers.copySolution(originalSolution);
        partialSolution.getOrderSequences().get(0).remove(2);
        removedOrder.setPostponementPenalty(50.0);  // Very low penalty, will be best option
        partialSolution.getUnplacedOrders().add(removedOrder);
        assertEquals(expectedSolution, insertionGreedy.repair(partialSolution));
    }
}
