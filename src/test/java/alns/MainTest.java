package alns;

import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import setpartitioning.Pool;

import java.util.*;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    @DisplayName("test run")
    public void runTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Parameters.verbose = false;
        Parameters.semiVerbose = false;
        Parameters.totalIter = 65;
        Parameters.maxIterSolution = 20;
        Parameters.noiseRate = 0.5;
        Parameters.saveSolution = false;
        double startTime = System.nanoTime() / 1e9;
        Main.alns(startTime);
    }


    @Test
    @DisplayName("test acceptSolution")
    public void acceptSolutionTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        Main.initialize();

        Solution solutionOne = createFeasibleSolution(2, Problem.getNumberOfOrders());
        Solution solutionTwo = createFeasibleSolution(3, Problem.getNumberOfOrders());
        Solution solutionThree = createFeasibleSolution(5, Problem.getNumberOfOrders());

        assertNotNull(solutionOne);
        assertNotNull(solutionTwo);
        assertNotNull(solutionThree);

        Objective.setObjValAndSchedule(solutionOne);  // Fitness 2726
        Objective.setObjValAndSchedule(solutionTwo);  // Fitness 2561
        Objective.setObjValAndSchedule(solutionThree);  // Fitness 2594

        Parameters.setTemperatureAndCooling(solutionOne.getObjective(false));
        Main.setCurrentTemperature(Parameters.startTemperature);

        Main.setCurrentSolution(solutionOne);
        Main.setBestSolution(solutionOne);

        // Initialize route saving
        Cache.initialize();
        Pool.initialize();

        double rewardOne = Main.acceptSolution(solutionTwo, null, 0);

        assertEquals(solutionTwo, Main.getBestSolution());
        assertEquals(solutionTwo, Main.getCurrentSolution());
        // assertEquals(33.0, rewardOne, 0.0);

        double rewardTwo = 0.0;
        while (Main.getCurrentSolution() != solutionThree) rewardTwo = Main.acceptSolution(solutionThree, null, 0);

        assertEquals(solutionThree, Main.getCurrentSolution());
        // assertEquals(13.0, rewardTwo, 0.0);

    }

    private Solution createFeasibleSolution(int dividerOne, int dividerTwo) {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < dividerOne; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = dividerOne; i < dividerTwo; i++) orderSequences.get(1).add(Problem.getOrder(i));
        for (int i = dividerTwo; i < Problem.getNumberOfOrders(); i++) orderSequences.get(2).add(Problem.getOrder(i));
        return new Solution(orderSequences, new HashSet<>(), false);
    }
}
