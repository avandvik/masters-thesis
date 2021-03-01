package alns;

import data.Problem;
import objects.Installation;
import objects.Order;
import objects.Vessel;
import subproblem.ArcGeneration;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.LinkedList;
import java.util.List;

public class Evaluator {

    public static boolean evaluateLoad(Solution solution) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (int vesselNumber = 0; vesselNumber < orderSequences.size(); vesselNumber++) {
            Vessel vessel = Problem.vessels.get(vesselNumber);
            double currentLoad = findTotalStartLoad(orderSequences.get(vesselNumber));
            if (currentLoad > vessel.getCapacity()) return false;
            for (Order order : orderSequences.get(vesselNumber)) {
                if (order.isDelivery()) {
                    currentLoad -= order.getSize();
                } else {
                    currentLoad += order.getSize();
                }
                if (currentLoad > vessel.getCapacity()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double findTotalStartLoad(List<Order> orderSequence) {
        double totalStartLoad = 0.0;
        for (Order order : orderSequence) {
            if (order.isDelivery()) totalStartLoad += order.getSize();
        }
        return totalStartLoad;
    }

    public static boolean evaluateTime(Solution solution) {
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            int currentTime = Problem.preparationEndTime;
            List<Order> orderSequence = solution.getOrderSequences().get(vesselNumber);
            if (orderSequence.size() == 0) continue;
            currentTime = findTimeAtFirstOrder(currentTime, orderSequence);
            if (orderSequence.size() > 1) currentTime = findTimeAtLastOrder(currentTime, orderSequence);
            currentTime = findEndTime(currentTime, orderSequence);
            if (currentTime > Problem.planningPeriodDisc) return false;
        }
        return true;
    }

    private static int findTimeAtFirstOrder(int startTime, List<Order> orderSequence) {
        Order firstOrder = orderSequence.get(0);
        Installation depot = Problem.getDepot();
        Installation firstInst = Problem.getInstallation(firstOrder);
        int sailingDuration = findSailingDuration(startTime, depot, firstInst);
        int arrTime = startTime + sailingDuration;
        int serviceDuration = ArcGeneration.calculateServiceDuration(firstOrder);
        int serviceStartTime = ArcGeneration.getServiceStartTimeAfterIdling(arrTime, serviceDuration, firstInst);
        return serviceStartTime + serviceDuration;
    }

    private static int findTimeAtLastOrder(int endTimeFirstOrder, List<Order> orderSequence) {
        int startTimeFromOrder = endTimeFirstOrder;
        for (Order fromOrder : orderSequence.subList(0, orderSequence.size() - 1)) {
            Order toOrder = Helpers.getNextElement((LinkedList<Order>) orderSequence, fromOrder);
            Installation fromInst = Problem.getInstallation(fromOrder);
            Installation toInst = Problem.getInstallation(toOrder);
            int sailingDuration = findSailingDuration(startTimeFromOrder, fromInst, toInst);
            int arrTime = startTimeFromOrder + sailingDuration;
            int serviceDuration = ArcGeneration.calculateServiceDuration(toOrder);
            int serviceStartTime = ArcGeneration.getServiceStartTimeAfterIdling(arrTime, serviceDuration, toInst);
            startTimeFromOrder = serviceStartTime + serviceDuration;
        }
        return startTimeFromOrder;
    }

    private static int findEndTime(int endTimeLastOrder, List<Order> orderSequence) {
        Order lastOrder = ((LinkedList<Order>) orderSequence).getLast();
        Installation lastInst = Problem.getInstallation(lastOrder);
        Installation depot = Problem.getDepot();
        int sailingDuration = findSailingDuration(endTimeLastOrder, lastInst, depot);
        return endTimeLastOrder + sailingDuration;
    }

    private static int findSailingDuration(int startTime, Installation fromInstallation, Installation toInstallation) {
        double distance = DistanceCalculator.distance(fromInstallation, toInstallation, "N");
        double averageMaxSpeed = ArcGeneration.calculateAverageMaxSpeed(startTime, distance);
        return Problem.hourToDiscTimePoint(distance / averageMaxSpeed);
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        int randomSeed = 5;
        Solution solution = new Solution(randomSeed);
        System.out.println(solution);
        evaluateTime(solution);

    }
}
