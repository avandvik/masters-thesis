import data.Problem;
import objects.Installation;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import subproblem.ArcGeneration;
import utils.Helpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ArcGenerationTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    @DisplayName("Test isReturnPossible")
    public void testIsReturnPossible() {
        Problem.setUpProblem("testOne.json");
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

    @Test
    @DisplayName("Test isServicingPossible")
    public void testIsServicingPossible() {
        Problem.setUpProblem("testTwo.json");
        int startRoughWeather = Helpers.getStartTimeOfRoughWeather();
        int serviceStartTimeOne = 100;
        int serviceEndTimeOne = 120;
        int serviceStartTimeTwo = 130;
        Installation toInstOne = Problem.getInstallation(0);
        assertTrue(ArcGeneration.isServicingPossible(serviceStartTimeOne, serviceEndTimeOne, toInstOne));
        assertTrue(ArcGeneration.isServicingPossible(serviceStartTimeTwo, startRoughWeather - 1, toInstOne));
        assertFalse(ArcGeneration.isServicingPossible(serviceStartTimeTwo, startRoughWeather, toInstOne));
        assertFalse(ArcGeneration.isServicingPossible(startRoughWeather, startRoughWeather + 10, toInstOne));
    }

    @Test
    @DisplayName("Test calculateAverageMaxSpeed")
    public void testCalculateAverageMaxSpeed() {
        Problem.setUpProblem("testTwo.json");
        double distance = 40.0;

        int startSailingTimeOne = 100;

        assertEquals(12.0, ArcGeneration.calculateAverageMaxSpeed(startSailingTimeOne, distance));

    }
}


