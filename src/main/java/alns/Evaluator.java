package alns;

import data.Problem;
import objects.Installation;
import objects.Order;
import subproblem.ArcGeneration;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.LinkedList;
import java.util.List;

public class Evaluator {

    public static boolean evaluateLoad(Solution solution) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (int vesselNumber = 0; vesselNumber < orderSequences.size(); vesselNumber++) {
            double currentLoad = findTotalStartLoad(orderSequences.get(vesselNumber));
            if (currentLoad > Problem.vessels.get(vesselNumber).getCapacity()) return false;
            for (Order order : orderSequences.get(vesselNumber)) {
                if (order.isDelivery()) {
                    currentLoad -= order.getSize();
                } else {
                    currentLoad += order.getSize();
                }
                if (currentLoad > Problem.vessels.get(vesselNumber).getCapacity()) {
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
        List<List<Order>> orderSequences = solution.getOrderSequences();

        for (int vesselNumber = 0; vesselNumber < orderSequences.size(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);

            if(orderSequence.size() == 0) {
                continue;
            }

            Order firstOrder = orderSequence.get(0);
            int currentTime = Problem.preparationEndTime;
            Installation depot = Problem.getDepot();
            Installation firstInstallation = Problem.getInstallation(firstOrder);
            int firstSailingDuration = findSailingDuration(currentTime, depot, firstInstallation);
            currentTime += firstSailingDuration;

            int firstServiceDuration = ArcGeneration.calculateServiceDuration(firstOrder);
            while(!ArcGeneration.isServicingPossible(currentTime,(currentTime + firstServiceDuration),firstInstallation)) {
               currentTime++;
            }
            currentTime += firstServiceDuration;

            if(orderSequence.size() > 1) {
                for (Order fromOrder : orderSequence.subList(0, orderSequence.size() - 1)) {
                    Order toOrder = Helpers.getNextElement((LinkedList<Order>) orderSequence, fromOrder);
                    Installation fromInstallation = Problem.getInstallation(fromOrder);
                    Installation toInstallation = Problem.getInstallation(toOrder);
                    int sailingDuration = findSailingDuration(currentTime, fromInstallation, toInstallation);
                    currentTime += sailingDuration;

                    int serviceDuration = ArcGeneration.calculateServiceDuration(toOrder);
                    while(!ArcGeneration.isServicingPossible(currentTime,(currentTime + serviceDuration),toInstallation)) {
                        currentTime++;
                    }

                    currentTime += serviceDuration;
                }

            }

            Order lastOrder = ((LinkedList<Order>) orderSequence).getLast();
            Installation lastInstallation = Problem.getInstallation(lastOrder);
            int lastSailingDuration = findSailingDuration(currentTime, lastInstallation, depot);
            currentTime += lastSailingDuration;

            if (currentTime > Problem.planningPeriodDisc) {
                return false;
            }
        }

        return true;
    }

    private static int findSailingDuration(int startTime, Installation fromInstallation, Installation toInstallation) {
        int sailingDuration = 0;
        double sailedDistance = 0.0;
        int time = startTime;
        double distance = DistanceCalculator.distance(fromInstallation, toInstallation, "N");
        while (sailedDistance < distance) {
            int ws = Problem.weatherForecastDisc.get(time);
            sailedDistance += (Problem.maxSpeed - Problem.wsToSpeedImpact.get(ws)) * ((double) 1 / Problem.discretizationParam);
            time++;
            sailingDuration++;
        }
        return sailingDuration;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        int randomSeed = 5;
        Solution solution = new Solution(randomSeed);
        evaluateLoad(solution);
        evaluateTime(solution);
    }
}
