package alns;

import data.Problem;
import objects.Installation;
import objects.Order;
import subproblem.ArcGeneration;
import utils.DistanceCalculator;

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
        System.out.println(orderSequences);
        // We want to do the same calculations for all vessels
        for (int vesselNumber = 0; vesselNumber < orderSequences.size()-1; vesselNumber++) {

            // If a vessel has no orders, it does not leave
            if(orderSequences.size() == 0) {
                continue;
            }

            // Handling from depot to first order
            Order firstOrder = orderSequences.get(vesselNumber).get(0);
            int currentTime = Problem.preparationEndTime;
            Installation depot = Problem.getDepot();
            Installation firstInstallation = Problem.getInstallation(firstOrder);
            int firstSailingDuration = findSailingDuration(currentTime, depot, firstInstallation);
            currentTime += firstSailingDuration;
            int firstServiceDuration = ArcGeneration.calculateServiceDuration(firstOrder);

            while(!ArcGeneration.isServicingPossible(currentTime,currentTime + firstServiceDuration,firstInstallation)) {
               currentTime++;
            }
            currentTime += firstServiceDuration;


           // Handling between all orders in a sequence
            if(orderSequences.size() > 1) {
                for (int orderNum = 0; orderNum < orderSequences.size()-1; orderNum++) {
                    Order fromOrder = orderSequences.get(vesselNumber).get(orderNum);
                    Order toOrder = orderSequences.get(vesselNumber).get(orderNum+1);
                    Installation fromInstallation = Problem.getInstallation(fromOrder);
                    Installation toInstallation = Problem.getInstallation(toOrder);
                    int sailingDuration = findSailingDuration(currentTime, fromInstallation, toInstallation);
                    currentTime += firstServiceDuration;

                    int serviceDuration = ArcGeneration.calculateServiceDuration(toOrder);
                    while(!ArcGeneration.isServicingPossible(currentTime,(currentTime + serviceDuration),toInstallation)) {
                        currentTime++;
                    }
                    currentTime += serviceDuration;
                }
            }

            // Handling from last order back to depot
            Order lastOrder = orderSequences.get(vesselNumber).get(orderSequences.size()-1);
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
            int ws = Problem.weatherForecastDisc.get(time/Problem.discretizationParam);
            sailedDistance += (Problem.maxSpeed-Problem.wsToSpeedImpact.get(ws))*(1/Problem.discretizationParam);
            time++;
            sailingDuration++;
        }

        return sailingDuration;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        Solution solution = new Solution();
        evaluateLoad(solution);
        evaluateTime(solution);
    }
}
