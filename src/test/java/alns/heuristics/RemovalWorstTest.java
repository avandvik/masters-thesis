package alns.heuristics;

import alns.Main;
import alns.Solution;
import alns.SolutionGenerator;
import data.Constants;
import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import subproblem.Cache;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RemovalWorstTest {

    @Test
    @DisplayName("test RemovalWorst")
    public void removalWorstTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        Main.initializeSequenceSaving();
        RemovalWorst removalWorst = new RemovalWorst(Constants.REMOVAL_WORST_NAME);
        Parameters.parallelHeuristics = false;
        Solution solution = createInitialSolution();
        testRemovalsWithRandomness(removalWorst, solution);
        setPostponementPenaltyMaxOrMin(solution, true);
        testNoRemovals(removalWorst, solution);
        testRemovalsAsExpectedHighPenalty(removalWorst, solution);
        setPostponementPenaltyMaxOrMin(solution, false);
        testRemovalsAsExpectedLowPenalty(removalWorst, solution);
    }

    private void testRemovalsWithRandomness(RemovalWorst removalWorst, Solution solution) {
        Parameters.maxPercentage = 0.5;
        Parameters.minPercentage = 0.5;
        Parameters.p = 1;
        Parameters.parallelHeuristics = false;
        Solution seqSolution = removalWorst.destroy(solution);
        assertEquals(createExpectedSolutionRandomness(), seqSolution);
    }

    private void testNoRemovals(RemovalWorst removalWorst, Solution solution) {
        Parameters.maxPercentage = 0.0;
        Parameters.minPercentage = 0.0;
        Parameters.minOrdersRemove = 0;
        Solution partialSolution = removalWorst.destroy(solution);
        assertEquals(solution, partialSolution);
    }

    private void testRemovalsAsExpectedHighPenalty(RemovalWorst removalWorst, Solution solution) {
        Parameters.p = 100;
        Parameters.maxPercentage = 0.5;
        Parameters.minPercentage = 0.5;
        Solution expectedSolution = createExpectedSolutionHighPenalty();
        Parameters.parallelHeuristics = false;
        Solution seqSolution = removalWorst.destroy(solution);
        Parameters.parallelHeuristics = true;
        Solution parSolution = removalWorst.destroy(solution);
        assertEquals(expectedSolution, seqSolution);
        assertEquals(expectedSolution, parSolution);
    }

    private void testRemovalsAsExpectedLowPenalty(RemovalWorst removalWorst, Solution solution) {
        Parameters.p = 100;
        Parameters.maxPercentage = 0.5;
        Parameters.minPercentage = 0.5;
        Solution expectedSolution = createExpectedSolutionLowPenalty();
        Parameters.parallelHeuristics = false;
        Solution seqSolution = removalWorst.destroy(solution);
        Parameters.parallelHeuristics = true;
        Solution parSolution = removalWorst.destroy(solution);
        assertEquals(expectedSolution, seqSolution);
        assertEquals(expectedSolution, parSolution);
    }

    private void setPostponementPenaltyMaxOrMin(Solution solution, boolean isMax) {
        for (List<Order> orderSequence : solution.getOrderSequences()) {
            for (Order order : orderSequence) {
                if (isMax) {
                    order.setPostponementPenalty(10000000.0 + Problem.random.nextDouble());
                } else {
                    order.setPostponementPenalty(Problem.random.nextDouble());
                }
            }
        }
        List<Order> postponedOrderList = new ArrayList<>(solution.getAllPostponed());
        Collections.sort(postponedOrderList);  // For predictability
        for (Order order : postponedOrderList) {
            if (isMax) {
                order.setPostponementPenalty(10000000.0 + Problem.random.nextDouble());
            } else {
                order.setPostponementPenalty(Problem.random.nextDouble());
            }
        }
    }

    private Solution createInitialSolution() {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(2, Problem.getNumberOfOrders());
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(0));
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(1));
        solution.addPostponedOrder(solution.getOrderSequence(1).remove(3));
        return solution;
    }

    private Solution createExpectedSolutionRandomness() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>(Collections.singletonList(Problem.getOrder(2)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(7), Problem.getOrder(3),
                Problem.getOrder(4)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolutionHighPenalty() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(5), Problem.getOrder(6))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>(Collections.singletonList(Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(4), Problem.getOrder(3),
                Problem.getOrder(2)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createExpectedSolutionLowPenalty() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(7)));
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(5), Problem.getOrder(6)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
