package localsearch;

import alns.Objective;
import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import setpartitioning.VoyagePool;
import subproblem.Cache;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class OperatorOneRelocateTest {

    @Test
    @DisplayName("test oneRelocate")
    public void oneRelocateTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        VoyagePool.initializeSequenceSaving();
        Solution solution = SolutionGenerator.createSolutionBasicTestData(3, 8);
        Objective.setObjValAndSchedule(solution);
        Solution newSolution = OperatorOneRelocate.oneRelocate(solution);
        Solution expectedSolution = createExpectedSolution();
        assertEquals(expectedSolution, newSolution);
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(0),
                Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(6), Problem.getOrder(7), Problem.getOrder(5))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
