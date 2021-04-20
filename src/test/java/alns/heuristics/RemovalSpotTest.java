package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.SolutionGenerator;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RemovalSpotTest {

    @Test
    @DisplayName("test RemovalSpot")
    public void removalSpotTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Objective.initializeCache();
        RemovalSpot removalSpot = new RemovalSpot("RemovalSpot");
        Parameters.parallelHeuristics = false;

        Solution solution = createInitialSolution();
        // numberOfOrders is set randomly as it is not relevant for the heuristic
        assertEquals(removalSpot.destroy(solution, 1), createExpectedSolution());

    }

    private Solution createInitialSolution() {
        return SolutionGenerator.createSolutionBasicTestData(3, 6);
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(5))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(6), Problem.getOrder(7)));

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
