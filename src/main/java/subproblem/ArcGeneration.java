package subproblem;

import data.Problem;
import objects.Installation;
import objects.Order;
import utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArcGeneration {

    // TODO: Go over public/private

    public static void generateArcsFromDepotToOrder(Order firstOrder) {
        Installation depot = Problem.getDepot();
        Installation firstInstallation = Problem.getInstallation(firstOrder);
        int startTime = Problem.preparationEndTime;
        double distance = DistanceCalculator.distance(depot, firstInstallation, "N");
        List<Double> speeds = getSpeeds(distance, startTime);
        Map<Integer, Double> arrTimesToSpeeds = mapArrTimesToSpeeds(distance, startTime, speeds);
        Map<Double, Integer> speedsToArrTimes = mapSpeedsToArrTimes(distance, startTime, speeds);
        int serviceDuration = calculateServiceDuration(firstOrder);
        // Arrival times -> servicing start time (possibly with idling times)

    }

    public static List<Double> getSpeeds(double distance, int startTime) {
        int maxDuration = (int) Math.ceil(hourToDisc(distance / Problem.minSpeed));
        List<Double> adjustedMaxSpeeds = getAdjustedMaxSpeeds(startTime, startTime + maxDuration);
        double averageMaxSpeed = getAverageDoubleList(adjustedMaxSpeeds);
        List<Double> speeds = new ArrayList<>();
        for (double speed = Problem.minSpeed; speed < averageMaxSpeed; speed += 1) speeds.add(speed);
        speeds.add(averageMaxSpeed);
        return speeds;
    }

    public static Map<Integer, Double> mapArrTimesToSpeeds(double distance, int startTime, List<Double> speeds) {
        Map<Integer, Double> arrTimesToSpeeds = new HashMap<>();
        for (Double speed : speeds) {
            int arrTime = startTime + ((int) Math.floor(hourToDisc(distance / speed)));
            if (!arrTimesToSpeeds.containsKey(arrTime) || speed < arrTimesToSpeeds.get(arrTime)) {
                arrTimesToSpeeds.put(arrTime, speed);
            }
        }
        return arrTimesToSpeeds;
    }

    public static Map<Double, Integer> mapSpeedsToArrTimes(double distance, int startTime, List<Double> speeds) {
        Map<Double, Integer> speedsToArrTimes = new HashMap<>();
        for (Double speed : speeds) {
            int arrTime = startTime + ((int) Math.floor(hourToDisc(distance / speed)));
            if (!speedsToArrTimes.containsValue(arrTime)) speedsToArrTimes.put(speed, arrTime);
        }
        return speedsToArrTimes;
    }

    public static Map<Double, List<Integer>> mapSpeedsToTimePoints(Map<Double, Integer> speedsToArrTimes) {
        Map<Double, List<Integer>> speedsToAllTimes = new HashMap<>();

        for (Map.Entry<Double, Integer> entry : speedsToArrTimes.entrySet()) {
            Double speed = entry.getKey();
            Integer arrTime = entry.getValue();
        }
        return new HashMap<>();
    }

    public static boolean isReturnPossible(double distance, int endTime) {
        if (endTime > Problem.getFinalTimePoint()) return false;
        List<Double> adjustedMaxSpeeds = getAdjustedMaxSpeeds(endTime, Problem.planningPeriodDisc);
        Double averageMaxSpeed = getAverageDoubleList(adjustedMaxSpeeds);
        int earliestArrTime = endTime + (int) Math.ceil(hourToDisc(distance / averageMaxSpeed));
        return earliestArrTime <= Problem.getGeneralReturnTime();
    }

    public static List<Double> getAdjustedMaxSpeeds(int startSailingTime, int endSailingTime) {
        List<Integer> weather = Problem.weatherForecastDisc.subList(startSailingTime, endSailingTime);
        List<Double> adjustedMaxSpeeds = new ArrayList<>();
        for (Integer ws : weather) adjustedMaxSpeeds.add(Problem.maxSpeed - Problem.wsToSpeedImpact.get(ws));
        return adjustedMaxSpeeds;
    }

    // TODO: This is a bit different from the project-thesis
    public static int calculateServiceDuration(Order order) {
        return (int) Math.ceil(order.getSize() * Problem.discServiceTimeUnit);
    }

    public static double hourToDisc(double timeHour) {
        return timeHour * Problem.discretizationParam;
    }

    public static double getAverageDoubleList(List<Double> list) {
        return list.stream().mapToDouble(a -> a).average().getAsDouble();
    }
}
