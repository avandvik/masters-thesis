package subproblem;

import data.ProblemInstance;
import objects.Installation;
import objects.Order;
import utils.DistanceCalculator;

public class ArcGeneration {

    public static void generateArcsFromDepotToOrder(Order firstOrder) {
        Installation depot = ProblemInstance.getDepot();
        Installation firstInstallation = ProblemInstance.getInstallation(firstOrder);

        // Get possible arrival times
        double distance = DistanceCalculator.distance(depot.getLatitude(), depot.getLongitude(),
                firstInstallation.getLatitude(), firstInstallation.getLongitude(), "N");

        int maxDuration = (int) Math.ceil(hourToDisc(distance / ProblemInstance.minSpeed));

        double serviceDuration = calculateServiceDuration(firstOrder);
    }

    public static int calculateServiceDuration(Order order) {
        return (int) Math.ceil(order.getSize() * ProblemInstance.discServiceTimeUnit);
    }


    public static double hourToDisc(double timeHour) {
        return timeHour * ProblemInstance.discretizationParam;
    }
}
