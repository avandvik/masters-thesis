import data.Problem;
import objects.Installation;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import subproblem.ArcGeneration;
import utils.Helpers;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ArcGenerationTest {

    @Test
    @DisplayName("Test isReturnPossible")
    public void testIsReturnPossible() {
        Problem.setUpProblem("basicTestData.json", true);
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
        Problem.setUpProblem("weather/criticalWeather.json", true);
        int startTimeWS3 = Helpers.getStartTimeOfWeatherState(3);
        int serviceStartTimeOne = 100;
        int serviceEndTimeOne = 120;
        int serviceStartTimeTwo = 130;
        Installation toInstOne = Problem.getInstallation(0);
        assertTrue(ArcGeneration.isServicingPossible(serviceStartTimeOne, serviceEndTimeOne, toInstOne));
        assertTrue(ArcGeneration.isServicingPossible(serviceStartTimeTwo, startTimeWS3 - 1, toInstOne));
        assertFalse(ArcGeneration.isServicingPossible(serviceStartTimeTwo, startTimeWS3, toInstOne));
        assertFalse(ArcGeneration.isServicingPossible(startTimeWS3, startTimeWS3 + 10, toInstOne));
    }

    @Test
    @DisplayName("Test calculateAverageMaxSpeed")
    public void testCalculateAverageMaxSpeed() {
        Problem.setUpProblem("weather/criticalWeather.json", true);
        double distance = 40.0;
        double delta = 0.1;
        int startTimeWS2 = Helpers.getStartTimeOfWeatherState(2);
        int startTimeWS3 = Helpers.getStartTimeOfWeatherState(3);
        int startTimeWS2And3 = startTimeWS3 - 5;
        assertEquals(12.0, ArcGeneration.calculateAverageMaxSpeed(startTimeWS2, distance), delta);
        assertEquals(11.0, ArcGeneration.calculateAverageMaxSpeed(startTimeWS3, distance), delta);
        assertEquals(11.3, ArcGeneration.calculateAverageMaxSpeed(startTimeWS2And3, distance), delta);
    }

    @Test
    @DisplayName("Test mapWSToTimeSpent and getTimeInWS")
    public void testWSFunctions() {
        Problem.setUpProblem("weather/criticalWeather.json", true);
        int startTime = 50;
        int endTime = 160;
        assertEquals(0, ArcGeneration.getTimeInWS(startTime, endTime, 0));
        assertEquals(26, ArcGeneration.getTimeInWS(startTime, endTime, 1));
        assertEquals(76, ArcGeneration.getTimeInWS(startTime, endTime, 2));
        assertEquals(8, ArcGeneration.getTimeInWS(startTime, endTime, 3));

        Map<Integer, Integer> wsToTimeSpent = ArcGeneration.mapWSToTimeSpent(startTime, endTime);
        int sumTimeSpent = wsToTimeSpent.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(endTime - startTime, sumTimeSpent);
    }
}


