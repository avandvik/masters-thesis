package subproblem;

import data.ProblemInstance;
import objects.Installation;
import objects.Order;
import utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.List;

public class ArcGeneration {

    public static void generateArcsFromDepotToOrder(Order firstOrder) {
        Installation depot = ProblemInstance.getDepot();
        Installation firstInstallation = ProblemInstance.getInstallation(firstOrder);
        int startTime = ProblemInstance.preparationEndTime;
        double distance = DistanceCalculator.distance(depot, firstInstallation, "N");
        List<Double> speeds = getSpeeds(distance, startTime);
        int serviceDuration = calculateServiceDuration(firstOrder);
        // Speeds -> arrival times
        // Arrival times -> servicing start time (possibly with idling times)
    }

    public static List<Double> getSpeeds(double distance, int startTime) {
        int maxDuration = (int) Math.ceil(hourToDisc(distance / ProblemInstance.minSpeed));
        List<Integer> weather = ProblemInstance.weatherForecastDisc.subList(startTime, startTime + maxDuration);
        List<Double> maxSpeeds = new ArrayList<>();
        for (Integer ws : weather) maxSpeeds.add(ProblemInstance.maxSpeed - ProblemInstance.wsToSpeedImpact.get(ws));
        double averageMaxSpeed = maxSpeeds.stream().mapToDouble(a -> a).average().getAsDouble();
        List<Double> speeds = new ArrayList<>();
        for (double speed = ProblemInstance.minSpeed; speed < averageMaxSpeed; speed += 1) speeds.add(speed);
        speeds.add(averageMaxSpeed);
        return speeds;
    }

    public static int calculateServiceDuration(Order order) {
        return (int) Math.ceil(order.getSize() * ProblemInstance.discServiceTimeUnit);
    }

    public static double hourToDisc(double timeHour) {
        return timeHour * ProblemInstance.discretizationParam;
    }
}
