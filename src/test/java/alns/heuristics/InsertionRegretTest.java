package alns.heuristics;

import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import utils.Helpers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class InsertionRegretTest {

    @Test
    @DisplayName("test InsertionRegret")
    public void insertionRegretTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        InsertionRegret insertionRegret = new InsertionRegret("regret insertion", false, true);
        testRegretTwoOrders(insertionRegret);
        testRegretThreeOrders(insertionRegret);
    }

    private void testRegretTwoOrders(InsertionRegret insertionRegret) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, 5);
        Solution expectedSolution = Helpers.copySolution(originalSolution);
        expectedSolution.getOrderSequences().get(0).add(0, expectedSolution.getOrderSequences().get(0).remove(2));

        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(expectedSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedSolution.getPostponedOrders());
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(partialOrderSequences.get(0).remove(0),
                partialOrderSequences.get(0).remove(1)));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);
        Solution repairedSolution = insertionRegret.repair(partialSolution);
        assertEquals(expectedSolution, repairedSolution);
    }

    private void testRegretThreeOrders(InsertionRegret insertionRegret) {
        Solution originalSolution = SolutionGenerator.createSolutionBasicTestData(3, 5);
        Solution expectedSolution = Helpers.copySolution(originalSolution);
        expectedSolution.getOrderSequences().get(0).add(0, expectedSolution.getOrderSequences().get(0).remove(2));
        expectedSolution.getOrderSequences().get(0).add(1, expectedSolution.getOrderSequences().get(2).remove(0));

        List<List<Order>> partialOrderSequences = Helpers.deepCopy2DList(originalSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedSolution.getPostponedOrders());
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(partialOrderSequences.get(0).remove(2),
                partialOrderSequences.get(1).remove(0), partialOrderSequences.get(2).remove(0)));
        Solution partialSolution = new Solution(partialOrderSequences, postponedOrders, unplacedOrders);
        Solution repairedSolution = insertionRegret.getRegretInsertion(partialSolution,3);
        assertEquals(expectedSolution, repairedSolution);
    }

}
