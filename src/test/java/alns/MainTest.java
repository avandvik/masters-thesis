package alns;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import subproblem.SubProblem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class MainTest {


    @Test
    @DisplayName("test acceptSolution")
    public void testAcceptSolution() {
        Problem.setUpProblem("basicTestData.json", true);

        Solution solutionOne = createFeasibleSolution(2, Problem.getNumberOfOrders());
        Solution solutionTwo = createFeasibleSolution(3, Problem.getNumberOfOrders());
        Solution solutionThree = createFeasibleSolution(5, Problem.getNumberOfOrders());

        assertNotNull(solutionOne);
        assertNotNull(solutionTwo);
        assertNotNull(solutionThree);

        SubProblem.runSubProblem(solutionOne);  // Fitness 2726
        SubProblem.runSubProblem(solutionTwo);  // Fitness 2561
        SubProblem.runSubProblem(solutionThree);  // Fitness 2594

        Main.setCurrentSolution(solutionOne);
        Main.setBestSolution(solutionOne);

        List<Double> rewardsOne = Main.acceptSolution(solutionTwo);

        assertEquals(solutionTwo, Main.getBestSolution());
        assertEquals(solutionTwo, Main.getCurrentSolution());
        assertEquals(33.0, rewardsOne.get(0), 0.0);

        List<Double> rewardsTwo = new ArrayList<>();
        while (Main.getCurrentSolution() != solutionThree) {
            rewardsTwo = Main.acceptSolution(solutionThree);
        }

        assertEquals(solutionThree, Main.getCurrentSolution());
        assertEquals(9.0, rewardsTwo.get(2), 0.0);

    }

    private Solution createFeasibleSolution(int dividerOne, int dividerTwo) {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < dividerOne; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = dividerOne; i < dividerTwo; i++) orderSequences.get(1).add(Problem.getOrder(i));
        for (int i = dividerTwo; i < Problem.getNumberOfOrders(); i++) orderSequences.get(2).add(Problem.getOrder(i));
        return new Solution(orderSequences);
    }
}
