package alns.heuristics;

import alns.Objective;
import alns.Solution;
import data.Constants;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import subproblem.Cache;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class InsertionMaxOrderSizeTest {

    @Test
    @DisplayName("test InsertionMaxOrderSize")
    public void insertionMaxOrderSize() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        Parameters.parallelHeuristics = false;
        InsertionMaxOrderSize insertionMaxOrderSize = new InsertionMaxOrderSize(Constants.INSERTION_MAX_ORDER_SIZE_NAME);
        // assertEquals(createExpectedSolution(), insertionMaxOrderSize.repair(createInitialSolution()));
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
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(7), Problem.getOrder(6),
                Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(5),
                Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
