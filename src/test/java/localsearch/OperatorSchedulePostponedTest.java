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

public class OperatorSchedulePostponedTest {

    @Test
    @DisplayName("test schedulePostponed")
    public void schedulePostponedTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Objective.initializeCache();
        Solution initialSolution = SolutionGenerator.createSolutionBasicTestData(5, 8);
        Order postponeOrderOne = initialSolution.getOrderSequence(0).get(4);
        initialSolution.addPostponedOrder(postponeOrderOne);
        initialSolution.removeOrderFromSequence(0, postponeOrderOne);
        Order postponeOrderTwo = initialSolution.getOrderSequence(1).get(2);
        initialSolution.addPostponedOrder(postponeOrderTwo);
        initialSolution.removeOrderFromSequence(1, postponeOrderTwo);
        Objective.setObjValAndSchedule(initialSolution);
        Solution newSolution = OperatorSchedulePostponed.schedulePostponed(initialSolution);
        assertEquals(createExpectedSolution(), newSolution);
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(7), Problem.getOrder(0), Problem.getOrder(1),
                Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
