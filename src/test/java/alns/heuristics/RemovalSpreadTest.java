package alns.heuristics;

import alns.Objective;
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

public class RemovalSpreadTest {

    @Test
    @DisplayName("test Removal Spread")
    public void removalSpreadTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Objective.initializeCache();
        RemovalSpread removalSpread = new RemovalSpread(Constants.REMOVAL_SPREAD_NAME);
        Parameters.parallelHeuristics = false;
        assertEquals(createExpectedSolution(), removalSpread.destroy(createInitialSolution(), 3));
    }

    private Solution createInitialSolution() {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, Problem.getNumberOfOrders());
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(0));
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(1));
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(3));
        return solution;
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(6))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(5)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

}
