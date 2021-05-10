package localsearch;

import alns.Objective;
import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import subproblem.Cache;

import static org.junit.Assert.assertEquals;

public class OperatorPostponeScheduledTest {

    @Test
    @DisplayName("test postponeScheduled")
    public void postponeScheduledTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Cache.initialize();
        // No postponement due to high penalty cost
        Solution solution = SolutionGenerator.createSolutionBasicTestData(5, 8);
        Objective.setObjValAndSchedule(solution);
        Solution newSolutionOne = OperatorPostponeScheduled.postponeScheduled(solution);
        assertEquals(solution, newSolutionOne);
        // Postponement due to penalty cost of O1-OP-I21 being set to 0.0
        Solution postponementSolution = createPostponementSolution();
        Solution newSolutionTwo = OperatorPostponeScheduled.postponeScheduled(postponementSolution);
        Solution expectedSolution = createExpectedSolution();
        assertEquals(newSolutionTwo, expectedSolution);

    }

    private Solution createPostponementSolution() {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(5, 8);
        Objective.setObjValAndSchedule(solution);
        Order order = solution.getOrderSequence(0).get(1);
        order.setPostponementPenalty(0.0);
        return solution;

    }

    private Solution createExpectedSolution () {
        Solution solution = SolutionGenerator.createSolutionBasicTestData(5, 8);
        Order rmOrder = solution.getOrderSequence(0).remove(1);
        solution.getAllPostponed().add(rmOrder);
        return solution;
    }
}
