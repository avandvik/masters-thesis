package localsearch;

import alns.Objective;
import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class OperatorTwoExchangeTest {

    @Test
    @DisplayName("test twoExchange")
    public void twoExchangeTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Objective.initializeCache();
        Solution solution = SolutionGenerator.createSolutionBasicTestData(5, 8);
        Objective.setObjValAndSchedule(solution);
        Solution newSolution = OperatorTwoExchange.twoExchange(solution);
        Solution expectedSolution = createExpectedSolution();
        assertEquals(newSolution, expectedSolution);
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(2),
                Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(6), Problem.getOrder(7))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

}
