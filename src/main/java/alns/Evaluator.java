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

    public static boolean isFeasibleLoad(Solution solution) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (int vesselNumber = 0; vesselNumber < orderSequences.size(); vesselNumber++) {
            if (!isFeasibleLoad(orderSequences.get(vesselNumber), Problem.getVessel(vesselNumber))) return false;
        }
        return true;
    }

    public static boolean isFeasibleLoad(List<Order> orderSequence, Vessel vessel) {
        double currentLoad = findTotalStartLoad(orderSequence);
        if (currentLoad > vessel.getCapacity()) return false;
        for (Order order : orderSequence) {
            if (order.isDelivery()) {
                currentLoad -= order.getSize();
            } else {
                currentLoad += order.getSize();
            }
            if (currentLoad > vessel.getCapacity()) {
                return false;
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

    public static boolean isFeasibleDuration(Solution solution) {
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = solution.getOrderSequences().get(vesselNumber);
            if (!isFeasibleDuration(orderSequence)) return false;
        }
        return true;
    }

    public static boolean isFeasibleDuration(List<Order> orderSequence) {
        if (orderSequence.size() == 0) return true;
        int currentTime = Problem.preparationEndTime;
        currentTime = findTimeAtFirstOrder(currentTime, orderSequence);
        if (orderSequence.size() > 1) currentTime = findTimeAtLastOrder(currentTime, orderSequence);
        currentTime = findEndTime(currentTime, orderSequence);
        return currentTime <= Problem.planningPeriodDisc;
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

    public static boolean isFeasibleVisitOrder(Solution solution) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        List<List<Integer>> instSequences = solution.getInstSequences();
        if (instInMoreThanOneSequence(instSequences)) return false;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Integer> instSequence = instSequences.get(vesselNumber);
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            if (isIllegalPattern(orderSequence, instSequence)) return false;
        }
        return true;
    }

    public static boolean instInMoreThanOneSequence(List<List<Integer>> instSequences) {
        for (int i = 0; i < instSequences.size(); i++) {
            int j = i + 1;
            while (j < instSequences.size()) {
                List<Integer> firstSequence = instSequences.get(i);
                List<Integer> secondSequence = instSequences.get(j);
                boolean differentInst = Collections.disjoint(firstSequence, secondSequence);
                if (!differentInst) return true;
                j++;
            }
        }
        return false;
    }

    public static boolean isIllegalPattern(List<Order> orderSequence, List<Integer> instSequence) {
        return isSpread(instSequence) || isIllegalVisitOrder(orderSequence);
    }

    private static boolean isSpread(List<Integer> instSequence) {
        List<Integer> seen = new ArrayList<>();
        seen.add(instSequence.get(0));
        for (int i = 1; i < instSequence.size(); i++) {
            int elem = instSequence.get(i);
            if (seen.contains(elem) && seen.get(seen.size() - 1) != elem) return true;
            seen.add(elem);
        }
        return false;
    }

    private static boolean isIllegalVisitOrder(List<Order> orderSequence) {
        for (Order currentOrder : orderSequence) {
            Order nextOrder = Helpers.getNextElement((LinkedList<Order>) orderSequence, currentOrder);
            if (nextOrder == null) break;
            if (Problem.getInstallation(currentOrder).equals(Problem.getInstallation(nextOrder))) {
                if (!currentOrder.isDelivery()) return true;
                if (nextOrder.isMandatory()) return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        int randomSeed = 17;
        Solution solution = new Solution(randomSeed);
        System.out.println(solution);
        System.out.println(isFeasibleLoad(solution));
        System.out.println(isFeasibleDuration(solution));
        System.out.println(isFeasibleVisitOrder(solution));
    }
}
