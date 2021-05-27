package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Constants;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import setpartitioning.VoyagePool;
import subproblem.Cache;
import utils.Helpers;

import java.util.*;

import static org.junit.Assert.*;

public class InsertionRegretTest {

    @Test
    @DisplayName("test InsertionRegret")
    public void insertionRegretTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        VoyagePool.initializeSequenceSaving();
        InsertionRegret insertionRegret = new InsertionRegret(Constants.INSERTION_REGRET_NAME);
        testRegretTwoOrders(insertionRegret);
        testRegretThreeOrders(insertionRegret);
    }

    private void testRegretTwoOrders(InsertionRegret insertionRegret) {
        Solution expectedSolution = createExpectedSolutionOne();

        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(expectedSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedSolution.getAllPostponed());
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(partialOrderSequences.get(0).remove(0),
                partialOrderSequences.get(1).remove(0)));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);

        Parameters.regretParameter = 3;
        Parameters.parallelHeuristics = false;
        Solution sequentialSolution = insertionRegret.repair(partialSolution);
        Parameters.parallelHeuristics = true;
        Solution parallelSolution = insertionRegret.repair(partialSolution);

        assertEquals(expectedSolution, sequentialSolution);
        assertEquals(expectedSolution, parallelSolution);
    }

    private void testRegretThreeOrders(InsertionRegret insertionRegret) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, 5);
        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(originalSolution.getOrderSequences());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(partialOrderSequences.get(0).remove(2),
                partialOrderSequences.get(1).remove(0), partialOrderSequences.get(2).remove(0)));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);

        Parameters.regretParameter = 4;
        Parameters.parallelHeuristics = false;
        Solution sequentialSolution = insertionRegret.repair(partialSolution);
        Parameters.parallelHeuristics = true;
        Solution parallelSolution = insertionRegret.repair(partialSolution);

        assertTrue(createExpectedSolutionThree().equals(sequentialSolution)
                || createExpectedSolutionTwo().equals(sequentialSolution));
        assertTrue(createExpectedSolutionThree().equals(parallelSolution)
                || createExpectedSolutionTwo().equals(parallelSolution));
    }

    private Solution createExpectedSolutionOne() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(0),
                Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6),
                Problem.getOrder(7))));
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolutionTwo() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2),
                Problem.getOrder(0), Problem.getOrder(1), Problem.getOrder(5))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6),
                Problem.getOrder(7))));
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolutionThree() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(5),
                Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6),
                Problem.getOrder(7))));
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
