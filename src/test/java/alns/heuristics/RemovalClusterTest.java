package alns.heuristics;

import alns.Cache;
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

public class RemovalClusterTest {

    @Test
    @DisplayName("test Removal Cluster")
    public void removalClusterTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        RemovalCluster removalCluster = new RemovalCluster(Constants.REMOVAL_CLUSTER_NAME);
        Parameters.parallelHeuristics = false;
        Parameters.minPercentage = 0.5;
        Parameters.maxPercentage = 0.5;
        assertEquals(createExpectedSolution(), removalCluster.destroy(createInitialSolution()));
    }

    @Test
    @DisplayName("test Removal Cluster Specific Data")
    public void removalClusterTestSpecificData() {
        Problem.setUpProblem("clusterOrders.json", true, 10);
        Cache.initialize();
        RemovalCluster removalCluster = new RemovalCluster(Constants.REMOVAL_CLUSTER_NAME);
        Parameters.parallelHeuristics = false;
        Parameters.minPercentage = 0.5;
        Parameters.maxPercentage = 0.5;
        assertEquals(createClusteredExpectedSolution(), removalCluster.destroy(createClusteredInitialSolution()));
    }

    private Solution createClusteredInitialSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        Set<Order> postponedOrders = new HashSet<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());
        for (int i = 0; i < 6; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 6; i < 10; i++) orderSequences.get(1).add(Problem.getOrder(i));
        return new Solution(orderSequences, postponedOrders, false);
    }

    private Solution createInitialSolution() {
        return SolutionGenerator.createSolutionBasicTestData(2, Problem.getNumberOfOrders());
    }

    private Solution createExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(6), Problem.getOrder(7))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(1), Problem.getOrder(5),
                Problem.getOrder(2), Problem.getOrder(0)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    private Solution createClusteredExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        Set<Order> postponedOrders = new HashSet<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(5))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6), Problem.getOrder(7))));
        orderSequences.add(new LinkedList<>());
        Set<Order> unplacedOrders = new HashSet<>(Arrays.asList(Problem.getOrder(9), Problem.getOrder(8),
                Problem.getOrder(1), Problem.getOrder(2), Problem.getOrder(0)));
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
