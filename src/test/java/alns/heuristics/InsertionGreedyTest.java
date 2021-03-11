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
        testTripleInsertion(insertionGreedy);
        testPostponementInsertion(insertionGreedy);
    }

    private void testSingleInsertion(InsertionGreedy insertionGreedy) {
        Solution expectedSolution = SolutionGenerator.createSolutionBasicTestData(3, Problem.getNumberOfOrders());
        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(expectedSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedSolution.getPostponedOrders());
        Set<Order> unplacedOrders = new HashSet<>(Collections.singletonList(partialOrderSequences.get(0).remove(0)));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);
        assertEquals(expectedSolution, insertionGreedy.repair(partialSolution));
    }

    private void testTripleInsertion(InsertionGreedy insertionGreedy) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, Problem.getNumberOfOrders());
        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(originalSolution.getOrderSequences());
        Order removedOrderOne = partialOrderSequences.get(0).remove(0);
        Order removedOrderTwo = partialOrderSequences.get(0).remove(1);
        Order removedOrderThree = partialOrderSequences.get(1).remove(3);

        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(removedOrderOne, removedOrderTwo, removedOrderThree));
        Solution partialSolution = new Solution(partialOrderSequences, new HashSet<>(), unplacedOrders);
        Solution expectedSolution = createExpectedSolutionTripleInsertion();
        assertEquals(expectedSolution, insertionGreedy.repair(partialSolution));
    }

    private void testPostponementInsertion(InsertionGreedy insertionGreedy) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, Problem.getNumberOfOrders());
        Solution expectedSolution = Helpers.copySolution(originalSolution);
        Order removedOrder = expectedSolution.getOrderSequences().get(0).remove(2);
        expectedSolution.getPostponedOrders().add(removedOrder);

        Solution partialSolution = Helpers.copySolution(originalSolution);
        partialSolution.getOrderSequences().get(0).remove(2);
        removedOrder.setPostponementPenalty(50.0);  // Very low penalty, will be best option
        partialSolution.getUnplacedOrders().add(removedOrder);
        assertEquals(expectedSolution, insertionGreedy.repair(partialSolution));
    }

    private Solution createExpectedSolutionTripleInsertion() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(6),
                Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3),
                Problem.getOrder(4), Problem.getOrder(5), Problem.getOrder(7))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
