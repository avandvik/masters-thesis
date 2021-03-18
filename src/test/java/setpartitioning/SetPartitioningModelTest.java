package setpartitioning;

import alns.Main;
import data.Parameters;
import data.Problem;
import org.junit.Test;

public class SetPartitioningModelTest {

    @Test
    public void testSetPartitioningModel() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        Parameters.verbose = false;
        Parameters.totalIterations = 65;
        Parameters.maxIterSolution = 20;
        Parameters.noiseRate = 0.5;
        Main.run();
        SetPartitioningModel.run();
    }
}
