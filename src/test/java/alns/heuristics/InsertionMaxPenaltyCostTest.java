package alns.heuristics;

import alns.Objective;
import alns.Solution;
import data.Constants;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class InsertionMaxPenaltyCostTest {

    @Test
    @DisplayName("test InsertionMaxPenaltyCost")
    public void insertionMaxPenaltyCostTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Objective.initializeCache();
        Parameters.parallelHeuristics = false;
        InsertionMaxPenaltyCost insertionMaxPenaltyCost =
                new InsertionMaxPenaltyCost(Constants.INSERTION_MAX_PENALTY_COST_NAME);
        System.out.println(insertionMaxPenaltyCost.repair(createInitialSolution()));
        assertEquals(createExpectedSolution(), insertionMaxPenaltyCost.repair(createInitialSolution()));
    }

    private Solution createInitialSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(0))));
        orderSequences.add(new LinkedList<>(Collections.singletonList(Problem.getOrder(3))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(1),
                Problem.getOrder(2), Problem.getOrder(4), Problem.getOrder(5),
                Problem.getOrder(6), Problem.getOrder(7)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6), Problem.getOrder(0),
                Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(5),
                Problem.getOrder(7), Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
