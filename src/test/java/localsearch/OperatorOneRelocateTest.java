package localsearch;

import alns.Objective;
import alns.Solution;
import alns.SolutionGenerator;
import data.Problem;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class OperatorOneRelocateTest {

    @Test
    @DisplayName("test oneRelocate")
    public void oneRelocateTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Objective.initializeCache();
        Solution solution = SolutionGenerator.createSolutionBasicTestData(3, 8);
        Objective.setObjValAndSchedule(solution);
        System.out.println(solution);
        Solution newSolution = OperatorOneRelocate.oneRelocate(solution);
        System.out.println(newSolution);
    }
}
