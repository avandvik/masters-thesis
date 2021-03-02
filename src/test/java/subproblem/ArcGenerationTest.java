package subproblem;

import data.Problem;
import objects.Installation;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import utils.Helpers;

import java.util.*;

import static org.junit.Assert.*;

public class ArcGenerationTest {

    @Test
    @DisplayName("Test getSpeeds")
    public void testGetSpeeds() {
        Problem.setUpProblem("basicTestData.json", true);
        List<Double> expectedSpeeds = new ArrayList<>(Arrays.asList(7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0));
        double distance = 30.0;
        int startTime = 145;
        assertEquals(expectedSpeeds, ArcGeneration.getSpeeds(distance, startTime));
        Problem.setUpProblem("weather/criticalWeather.json", true);
        double avgMaxSpeed = ArcGeneration.calculateAverageMaxSpeed(startTime, distance);
        List<Double> expectedSpeedsTwo = new ArrayList<>(Arrays.asList(7.0, 8.0, 9.0, 10.0, 11.0, avgMaxSpeed));
        assertEquals(expectedSpeedsTwo, ArcGeneration.getSpeeds(distance, startTime));
    }

    @Test
    @DisplayName("Test mapSpeedsToArrTimes")
    public void testMapSpeedsToArrTimes() {
        Problem.setUpProblem("basicTestData.json", true);
        double distance = 30.0;
        int startTime = 100;
        List<Double> speeds = new ArrayList<>(Arrays.asList(7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0));
        Map<Double, Integer> speedsToExpectedArrTime = new HashMap<>();
        speedsToExpectedArrTime.put(7.0, 117);
        speedsToExpectedArrTime.put(8.0, 115);
        speedsToExpectedArrTime.put(9.0, 113);
        speedsToExpectedArrTime.put(10.0, 112);
        speedsToExpectedArrTime.put(11.0, 110);
        speedsToExpectedArrTime.put(13.0, 109);
        speedsToExpectedArrTime.put(14.0, 108);
        Map<Double, Integer> speedsToArrTimes = ArcGeneration.mapSpeedsToArrTimes(distance, startTime, speeds);
        assertNull(speedsToArrTimes.get(12.0));
        for (double speed : speedsToArrTimes.keySet()) {
            assertEquals(speedsToExpectedArrTime.get(speed), speedsToArrTimes.get(speed));
        }
    }

    @Test
    @DisplayName("Test mapSpeedsToTimePoints")
    public void testMapSpeedsToTimePoints() {
        Problem.setUpProblem("basicTestData.json", true);
        double distance = 30.0;
        int serviceDuration = 10;
        Installation depot = Problem.getDepot();
        Installation instAlwaysOpen = Problem.getInstallation(Problem.orders.get(2));
        Installation instSometimesClosed = Problem.getInstallation(Problem.orders.get(0));
        Map<Double, Integer> speedsToArrTimes = new HashMap<>();
        speedsToArrTimes.put(9.0, 113);
        speedsToArrTimes.put(11.0, 110);

        Map<Double, Integer> speedsToExpectedServiceEndTimes = new HashMap<>();
        speedsToExpectedServiceEndTimes.put(9.0, 123);
        speedsToExpectedServiceEndTimes.put(11.0, 120);

        Map<Double, Integer> speedsToExpectedIdlingEndTimes = new HashMap<>();
        speedsToExpectedIdlingEndTimes.put(9.0, 123);
        speedsToExpectedIdlingEndTimes.put(11.0, 123);

        // To depot
        Map<Double, List<Integer>> speedsToTPDepot = ArcGeneration.mapSpeedsToTimePoints(speedsToArrTimes, distance,
                serviceDuration, depot);
        for (double speed : speedsToTPDepot.keySet()) {
            List<Integer> timePoints = speedsToTPDepot.get(speed);
            for (int timePoint : timePoints) assertEquals((int) speedsToArrTimes.get(speed), timePoint);
        }

        // No idling
        Map<Double, List<Integer>> speedsToTPNI = ArcGeneration.mapSpeedsToTimePoints(speedsToArrTimes, distance,
                serviceDuration, instAlwaysOpen);
        for (Map.Entry<Double, List<Integer>> entry : speedsToTPNI.entrySet()) {
            double speed = entry.getKey();
            List<Integer> timePoints = entry.getValue();
            int arrTime = timePoints.get(0);
            int serviceStartTime = timePoints.get(1);
            int serviceEndTime = timePoints.get(2);
            assertEquals((int) speedsToArrTimes.get(speed), arrTime);
            assertEquals(arrTime, serviceStartTime);
            assertEquals((int) speedsToExpectedServiceEndTimes.get(speed), serviceEndTime);
        }

        // Idling
        Map<Double, List<Integer>> speedsToTPI = ArcGeneration.mapSpeedsToTimePoints(speedsToArrTimes, distance,
                serviceDuration, instSometimesClosed);
        for (Map.Entry<Double, List<Integer>> entry : speedsToTPI.entrySet()) {
            double speed = entry.getKey();
            List<Integer> timePoints = entry.getValue();
            int arrTime = timePoints.get(0);
            int serviceStartTime = timePoints.get(1);
            int serviceEndTime = timePoints.get(2);
            assertEquals((int) speedsToArrTimes.get(speed), arrTime);
            assertEquals((int) speedsToExpectedIdlingEndTimes.get(speed), serviceStartTime);
            assertEquals(speedsToExpectedIdlingEndTimes.get(speed) + serviceDuration, serviceEndTime);
        }
    }

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
        Installation instAlwaysOpen = Problem.getInstallation(Problem.orders.get(2));
        // TESTING
        assertFalse(ArcGeneration.isServicingPossible(serviceStartTimeOne, serviceEndTimeOne, instAlwaysOpen));
        // END TESTING
        assertTrue(ArcGeneration.isServicingPossible(serviceStartTimeTwo, startTimeWS3 - 1, instAlwaysOpen));
        assertFalse(ArcGeneration.isServicingPossible(serviceStartTimeTwo, startTimeWS3, instAlwaysOpen));
        assertFalse(ArcGeneration.isServicingPossible(startTimeWS3, startTimeWS3 + 10, instAlwaysOpen));
        Installation instSometimesClosed = Problem.getInstallation(Problem.orders.get(0));
        assertFalse(ArcGeneration.isServicingPossible(80, 100, instSometimesClosed));
        assertFalse(ArcGeneration.isServicingPossible(122, 130, instSometimesClosed));
        assertTrue(ArcGeneration.isServicingPossible(123, 131, instSometimesClosed));
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
    @DisplayName("Test calculateServiceDuration")
    public void testCalculateServiceDuration() {
        Problem.setUpProblem("basicTestData.json", true);
        Order MDOrder = Problem.orders.get(0);
        Order OPOrder = Problem.orders.get(1);
        Order ODOrder = Problem.orders.get(2);
        assertEquals(10, ArcGeneration.calculateServiceDuration(MDOrder));
        assertEquals(1, ArcGeneration.calculateServiceDuration(OPOrder));
        assertEquals(13, ArcGeneration.calculateServiceDuration(ODOrder));
    }

    @Test
    @DisplayName("Test calculateArcCost")
    public void testCalculateArcCost() {
        Problem.setUpProblem("basicTestData.json", true);
        int startTime = 100;
        int arrTime = 120;
        int serviceStartTime = 130;
        int serviceEndTime = 140;
        double speed = 10.0;
        double distance = speed * Problem.discTimeToHour(arrTime - startTime);
        double delta = 0.5;
        assertEquals(479.0, ArcGeneration.calculateFuelCostSailing(startTime, arrTime, speed, distance), delta);
        assertEquals(0.0, ArcGeneration.calculateFuelCostSailing(startTime, startTime, speed, 0.0), delta);
        assertEquals(83.0, ArcGeneration.calculateFuelCostIdling(arrTime, serviceStartTime), delta);
        assertEquals(117.0, ArcGeneration.calculateFuelCostServicing(serviceStartTime, serviceEndTime), delta);
        assertEquals(6080.0, ArcGeneration.calculateCharterCost(startTime, serviceEndTime, true), delta);
        assertEquals(0.0, ArcGeneration.calculateCharterCost(startTime, serviceEndTime, false), delta);
        assertEquals(679.0, ArcGeneration.calculateArcCost(startTime, arrTime, serviceStartTime, serviceEndTime,
                speed, distance, false), delta);
        assertEquals(6759.0, ArcGeneration.calculateArcCost(startTime, arrTime, serviceStartTime, serviceEndTime,
                speed, distance, true), delta);

        // TODO: Add test for calculateArcCost with weather impact

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

        Map<Integer, Double> wsToTimeSpent = ArcGeneration.mapWSToTimeSpent(startTime, endTime);
        double sumTimeSpent = wsToTimeSpent.values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(Problem.discTimeToHour(endTime - startTime), sumTimeSpent, 0.0);
    }
}


