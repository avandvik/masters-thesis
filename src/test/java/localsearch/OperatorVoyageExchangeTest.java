package localsearch;

import alns.Cache;
import alns.Solution;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import setpartitioning.Pool;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class OperatorVoyageExchangeTest {

    @Test
    @DisplayName("test voyageExchange")
    public void voyageExchangeTest() {
        Problem.setUpProblem("bigInstance.json", true, 10);
        Cache.initialize();
        Pool.initialize();
        Solution solution = createInitialSolution();
        Solution newSolution = OperatorVoyageExchange.voyageExchange(solution);
        Solution expectedSolution = createExpectedSolution();
        assertEquals(newSolution, expectedSolution);
    }

    private Solution createInitialSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(4), Problem.getOrder(15),
                Problem.getOrder(24), Problem.getOrder(10), Problem.getOrder(22),
                Problem.getOrder(8), Problem.getOrder(25), Problem.getOrder(7),
                Problem.getOrder(9))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(1), Problem.getOrder(23),
                Problem.getOrder(14), Problem.getOrder(27), Problem.getOrder(20),
                Problem.getOrder(21), Problem.getOrder(11), Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6), Problem.getOrder(12),
                Problem.getOrder(18), Problem.getOrder(13), Problem.getOrder(3),
                Problem.getOrder(17), Problem.getOrder(19), Problem.getOrder(26))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(0))));
        Set<Order> postponedOrders = new HashSet<>();
        postponedOrders.add(Problem.getOrder(16));
        return new Solution(orderSequences, postponedOrders, true);
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6), Problem.getOrder(12),
                Problem.getOrder(18), Problem.getOrder(13), Problem.getOrder(3),
                Problem.getOrder(17), Problem.getOrder(19), Problem.getOrder(26))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(1), Problem.getOrder(23),
                Problem.getOrder(14), Problem.getOrder(27), Problem.getOrder(20),
                Problem.getOrder(21), Problem.getOrder(11), Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(4), Problem.getOrder(15),
                Problem.getOrder(24), Problem.getOrder(10), Problem.getOrder(22),
                Problem.getOrder(8), Problem.getOrder(25), Problem.getOrder(7),
                Problem.getOrder(9))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(0))));
        Set<Order> postponedOrders = new HashSet<>();
        postponedOrders.add(Problem.getOrder(16));
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
