package alns;

import data.Problem;
import objects.Installation;
import objects.Order;
import objects.Vessel;
import arcs.ArcGenerator;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;
import java.util.stream.Collectors;

public class Evaluator {

    public static boolean isSolutionFeasible(Solution solution) {
        return isOrderSequencesFeasible(solution.getOrderSequences())
                && isSolutionComplete(solution)
                && noMandatoryOrdersPostponed(solution.getPostponedOrders())
                && eachOrderOccursOnce(solution)
                && hasVoyageForEachVessel(solution);
    }

    public static boolean isPartFeasible(Solution partialSolution) {
        return isOrderSequencesFeasible(partialSolution.getOrderSequences())
                && noMandatoryOrdersPostponed(partialSolution.getPostponedOrders())
                && eachOrderOccursOnce(partialSolution)
                && hasVoyageForEachVessel(partialSolution);
    }

    public static boolean isOrderSequencesFeasible(List<List<Order>> orderSequences) {
        return isFeasibleLoad(orderSequences)
                && isFeasibleDuration(orderSequences)
                && isFeasibleVisits(orderSequences);
    }

    public static boolean isFeasibleLoad(List<List<Order>> orderSequences) {
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            if (!isFeasibleLoadSequence(orderSequences.get(vesselIdx), Problem.getVessel(vesselIdx))) return false;
        }
        return true;
    }

    public static boolean isFeasibleLoadSequence(List<Order> orderSequence, Vessel vessel) {
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

    public static double findTotalStartLoad(List<Order> orderSequence) {
        double totalStartLoad = 0.0;
        for (Order order : orderSequence) {
            if (order.isDelivery()) totalStartLoad += order.getSize();
        }
        return totalStartLoad;
    }

    public static boolean isFeasibleDuration(List<List<Order>> orderSequences) {
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            if (!isFeasibleDurationSequence(orderSequence)) return false;
        }
        return true;
    }

    public static boolean isFeasibleDurationSequence(List<Order> orderSequence) {
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
        int serviceDuration = ArcGenerator.calculateServiceDuration(firstOrder);
        int serviceStartTime = ArcGenerator.getServiceStartTimeAfterIdling(arrTime, serviceDuration, firstInst);
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
            int serviceDuration = ArcGenerator.calculateServiceDuration(toOrder);
            int serviceStartTime = ArcGenerator.getServiceStartTimeAfterIdling(arrTime, serviceDuration, toInst);
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
        double averageMaxSpeed = ArcGenerator.calculateAverageMaxSpeed(startTime, distance);
        return Problem.hourToDiscTimePoint(distance / averageMaxSpeed);
    }

    public static boolean isFeasibleVisits(List<List<Order>> orderSequences) {
        List<List<Integer>> instSequences = Helpers.getInstSequences(orderSequences);
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
        if (instSequence.isEmpty()) return false;
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

    public static boolean isSolutionComplete(Solution solution) {
        if (!solution.getUnplacedOrders().isEmpty()) return false;
        Set<Order> unscheduledOrders = inferUnscheduledOrders(solution.getOrderSequences());
        return solution.getPostponedOrders().containsAll(unscheduledOrders);
    }

    private static Set<Order> inferUnscheduledOrders(List<List<Order>> orderSequences) {
        return Problem.orders.stream()
                .filter(o -> !orderSequences.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
                        .contains(o))
                .collect(Collectors.toSet());
    }

    public static boolean noMandatoryOrdersPostponed(Set<Order> postponedOrders) {
        for (Order order : postponedOrders) {
            if (order.isMandatory()) {
                return false;
            }
        }
        return true;
    }

    public static boolean eachOrderOccursOnce(Solution solution) {
        Set<Order> seenOrders = new HashSet<>();
        for (List<Order> orderSequence : solution.getOrderSequences()) {
            for (Order order : orderSequence) {
                if (seenOrders.contains(order)) return false;
                seenOrders.add(order);
            }
        }
        for (Order order : solution.getPostponedOrders()) {
            if (seenOrders.contains(order)) return false;
            seenOrders.add(order);
        }
        for (Order order : solution.getUnplacedOrders()) {
            if (seenOrders.contains(order)) return false;
            seenOrders.add(order);
        }
        return true;
    }

    public static boolean hasVoyageForEachVessel(Solution solution) {
        return solution.getOrderSequences().size() == Problem.getNumberOfVessels();
    }
}
