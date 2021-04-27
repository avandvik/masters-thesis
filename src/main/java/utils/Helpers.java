package utils;

import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import subproblem.Node;

import java.util.*;

public class Helpers {

    public static <T> List<T> deepCopyList(List<T> original, boolean linkedList) {
        return linkedList ? new LinkedList<>(original) : new ArrayList<>(original);
    }

    public static <T> List<List<T>> deepCopy2DList(List<List<T>> original) {
        List<List<T>> copy = new ArrayList<>();
        for (List<T> row : original) copy.add(new LinkedList<>(row));
        return copy;
    }

    public static <T> Set<T> deepCopySet(Set<T> original) {
        return new HashSet<>(original);
    }

    public static Solution deepCopySolution(Solution solution) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    public static List<List<Order>> createEmptyOrderSequences() {
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());
        return orderSequences;
    }

    public static Double getKeyWithMinValue(Map<Double, Double> doubleToDouble) {
        Map.Entry<Double, Double> min = null;
        for (Map.Entry<Double, Double> entry : doubleToDouble.entrySet()) {
            if (min == null || entry.getValue() < min.getValue()) {
                min = entry;
            }
        }
        return min.getKey();
    }

    public static <T> T getNextElement(LinkedList<T> linkedList, T element) {
        if (!element.equals(linkedList.getLast())) {
            return linkedList.get(linkedList.indexOf(element) + 1);
        }
        return null;
    }

    public static int getStartTimeOfWeatherState(int weatherState) {
        /* Only to be used when weather is continuously rising or falling with periods of same weather */
        for (int timePoint = 0; timePoint < Problem.weatherForecastDisc.size(); timePoint++) {
            int ws = Problem.weatherForecastDisc.get(timePoint);
            if (ws == weatherState) {
                return timePoint;
            }
        }
        return -1;
    }

    public static List<List<Integer>> getInstIdSequences(List<List<Order>> orderSequences) {
        List<List<Integer>> instSequences = new ArrayList<>();
        for (List<Order> orderSequence : orderSequences) instSequences.add(getInstIdSequence(orderSequence));
        return instSequences;
    }

    public static List<Integer> getInstIdSequence(List<Order> orderSequence) {
        List<Integer> instSequence = new ArrayList<>();
        for (Order order : orderSequence) instSequence.add(order.getInstallationId());
        return instSequence;
    }

    public static List<Installation> getInstSequence(List<Order> orderSequence) {
        List<Installation> instSequence = new ArrayList<>();
        for (Order order : orderSequence) {
            Installation inst = Problem.getInstallation(order);
            if (instSequence.contains(inst)) continue;
            instSequence.add(inst);
        }
        return instSequence;
    }

    public static double getRandomDouble(double min, double max) {
        return Problem.random.nextDouble() * (max - min) + min;
    }

    public static <T extends Comparable<T>> T getRandomElementFromSet(Set<T> set) {
        List<T> list = new ArrayList<>(set);
        Collections.sort(list);  // Sort for predictability in tests
        int rnIdx = Problem.random.nextInt(list.size());
        return list.get(rnIdx);
    }

    public static List<List<Double>> convertOrdersToCoordinates(List<Order> orders) {
        List<List<Double>> coordinateCentroids = new ArrayList<>();
        for (int centroidIdx = 0; centroidIdx < orders.size(); centroidIdx++) {
            coordinateCentroids.add(new LinkedList<>());
            double latitude = Problem.getInstallation(orders.get(centroidIdx)).getLatitude();
            double longitude = Problem.getInstallation(orders.get(centroidIdx)).getLongitude();
            coordinateCentroids.get(centroidIdx).add(latitude);
            coordinateCentroids.get(centroidIdx).add(longitude);
        }
        return coordinateCentroids;
    }

    public static Installation getInstallationFromNode(Node node) {
        if (node.getOrder() == null) {
            return Problem.getDepot();
        } else {
            return Problem.getInstallation(node.getOrder());
        }
    }
}
