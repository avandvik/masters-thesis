package alns;

import data.Parameters;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import utils.Helpers;

import java.util.*;

import static org.junit.Assert.*;

public class MainTest {

    @Test
    @DisplayName("test run")
    public void runTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Parameters.verbose = false;
        Parameters.totalIterations = 65;
        Parameters.maxIterSolution = 20;
        Parameters.noiseRate = 0.5;

        Main.run();

        Solution expectedBestSolutionOne = createBestExpectedSolution();
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(expectedBestSolutionOne.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(expectedBestSolutionOne.getPostponedOrders());
        Collections.swap(orderSequences, 0, 1);
        Solution expectedBestSolutionTwo = new Solution(orderSequences, postponedOrders, true);

        assertTrue(Main.getBestSolution().equals(expectedBestSolutionOne) ||
                Main.getBestSolution().equals(expectedBestSolutionTwo));

        assertEquals(expectedBestSolutionOne.getFitness(false), Main.getBestSolution().getFitness(false), 0.0);
        assertEquals(expectedBestSolutionTwo.getFitness(false), Main.getBestSolution().getFitness(false), 0.0);
    }


    @Test
    @DisplayName("test acceptSolution")
    public void acceptSolutionTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);

        Solution solutionOne = createFeasibleSolution(2, Problem.getNumberOfOrders());
        Solution solutionTwo = createFeasibleSolution(3, Problem.getNumberOfOrders());
        Solution solutionThree = createFeasibleSolution(5, Problem.getNumberOfOrders());

        assertNotNull(solutionOne);
        assertNotNull(solutionTwo);
        assertNotNull(solutionThree);

        Objective.setObjValAndSchedule(solutionOne);  // Fitness 2726
        Objective.setObjValAndSchedule(solutionTwo);  // Fitness 2561
        Objective.setObjValAndSchedule(solutionThree);  // Fitness 2594

        Parameters.setTemperatureAndCooling(solutionOne.getFitness(false));
        Main.setCurrentTemperature(Parameters.startTemperature);

        Main.setCurrentSolution(solutionOne);
        Main.setBestSolution(solutionOne);

        double rewardOne = Main.acceptSolution(solutionTwo);

        assertEquals(solutionTwo, Main.getBestSolution());
        assertEquals(solutionTwo, Main.getCurrentSolution());
        assertEquals(33.0, rewardOne, 0.0);

        double rewardTwo = 0.0;
        while (Main.getCurrentSolution() != solutionThree) rewardTwo = Main.acceptSolution(solutionThree);

        assertEquals(solutionThree, Main.getCurrentSolution());
        assertEquals(9.0, rewardTwo, 0.0);

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

    private Solution createBestExpectedSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(7), Problem.getOrder(6),
                Problem.getOrder(0), Problem.getOrder(1), Problem.getOrder(5),
                Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>());
        return new Solution(orderSequences, new HashSet<>(), true);
    }
}
