package alns;

import data.Problem;
import objects.Installation;
import objects.Order;
import objects.Vessel;
import subproblem.ArcGeneration;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

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
            List<Order> orderSequence = solution.getOrderSequences().get(vesselNumber);
            int currentTime = Problem.preparationEndTime;

            if(orderSequence.size() == 0) {
                continue;
            }

            currentTime = findTimeAtFirstOrder(currentTime, orderSequence);
            if(orderSequence.size() > 1) {
                currentTime = findTimeAtLastOrder(currentTime,orderSequence);
            }
            currentTime = findEndTime(currentTime,orderSequence);

            if (currentTime > Problem.planningPeriodDisc) {
                return false;
            }
        }

        return true;
    }

    private static int findTimeAtFirstOrder(int currentTime, List<Order> orderSequence) {
        Order firstOrder = orderSequence.get(0);
        Installation depot = Problem.getDepot();
        Installation firstInstallation = Problem.getInstallation(firstOrder);
        int firstSailingDuration = findSailingDuration(currentTime, depot, firstInstallation);
        currentTime += firstSailingDuration;
        int firstServiceDuration = findServiceDuration(currentTime,firstInstallation,firstOrder);
        return (currentTime + firstServiceDuration);
    }

    private static int findTimeAtLastOrder(int currentTime, List<Order> orderSequence) {
        for (Order fromOrder : orderSequence.subList(0, orderSequence.size() - 1)) {
            Order toOrder = Helpers.getNextElement((LinkedList<Order>) orderSequence, fromOrder);
            Installation fromInstallation = Problem.getInstallation(fromOrder);
            Installation toInstallation = Problem.getInstallation(toOrder);
            int sailingDuration = findSailingDuration(currentTime, fromInstallation, toInstallation);
            currentTime += sailingDuration;
            int serviceDuration = findServiceDuration(currentTime,toInstallation,toOrder);
            currentTime += serviceDuration;
        }
        return currentTime;
    }

    private static int findEndTime(int currentTime, List<Order> orderSequence) {
        Order lastOrder = ((LinkedList<Order>) orderSequence).getLast();
        Installation lastInstallation = Problem.getInstallation(lastOrder);
        Installation depot = Problem.getDepot();
        int lastSailingDuration = findSailingDuration(currentTime, lastInstallation, depot);
        return (currentTime + lastSailingDuration);
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

    private static int findServiceDuration(int currentTime, Installation toInstallation, Order toOrder) {
        int serviceDuration = ArcGeneration.calculateServiceDuration(toOrder);
        while(!ArcGeneration.isServicingPossible(currentTime,(currentTime + serviceDuration),toInstallation)) {
            currentTime++;
        }
        return serviceDuration;
    }

    public static boolean evaluateVisitOrder(Solution solution) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        List<List<Integer>> installationSequences = getInstallationSequences(orderSequences);

        if (installationInMultipleSequences(installationSequences)) {
            return false;
        }
        
        List<List<Order>> copyOfOrderSequences = Helpers.deepCopyList(orderSequences);
        if (wrongVisitOrder(copyOfOrderSequences)) {
            return false;
        }

        return true;
    }

    private static List<List<Integer>> getInstallationSequences(List<List<Order>> orderSequences) {
        List<List<Integer>> installationSequences = new ArrayList<>();

        for (int vesselNumber = 0; vesselNumber < orderSequences.size(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            List<Integer> installationSequence = new ArrayList<>();
            for (Order order : orderSequence) {
                int installationID = order.getInstallationId();
                installationSequence.add(installationID);
            }

            installationSequences.add(installationSequence);
        }

        return installationSequences;
    }

    private static boolean installationInMultipleSequences(List<List<Integer>> installationSequences) {
        while (installationSequences.size() > 1) {
            for (int i = 1; i < installationSequences.size(); i++) {
                List<Integer> firstSequence = installationSequences.get(0);
                List<Integer> secondSequence = installationSequences.get(i);
                boolean differentInstallation = Collections.disjoint(firstSequence,secondSequence);
                if(!differentInstallation) {
                    return true;
                }
            }

            installationSequences.remove(0);
        }

        return false;
    }

    private static boolean wrongVisitOrder(List<List<Order>> orderSequences) {
        for (List<Order> orderSequence : orderSequences) {
            while (orderSequence.size() > 1) {
                for (int j = 1; j < orderSequence.size(); j++) {
                    Order firstOrder = orderSequence.get(0);
                    Order secondOrder = orderSequence.get(j);
                    if (firstOrder.getInstallationId() == secondOrder.getInstallationId()) {
                        if (!firstOrder.isDelivery()) {
                            return true;
                        }
                        if (!firstOrder.isMandatory()) {
                            return true;
                        }
                    }
                }

                orderSequence.remove(0);
            }
        }

        return false;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        int randomSeed = 17;
        Solution solution = new Solution(randomSeed);
        evaluateLoad(solution);
        evaluateTime(solution);
        evaluateVisitOrder(solution);
    }
}
