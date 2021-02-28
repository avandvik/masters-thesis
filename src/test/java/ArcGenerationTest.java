import data.Problem;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import subproblem.ArcGeneration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArcGenerationTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    @DisplayName("Test isReturnPossible")
    public void testIsReturnPossible() {
        Problem.setUpProblem("example.json");
        double distanceOne = 40.0;
        double distanceTwo = 0.0;
        int endTimeOne = 100;
        int endTimeTwo = 310;
        int endTimeThree = 320;
        assertTrue(ArcGeneration.isReturnPossible(distanceOne, endTimeOne));
        assertFalse(ArcGeneration.isReturnPossible(distanceOne, endTimeTwo));
        assertFalse(ArcGeneration.isReturnPossible(distanceOne, endTimeThree));
        assertTrue(ArcGeneration.isReturnPossible(distanceTwo, endTimeOne));
    }
}


