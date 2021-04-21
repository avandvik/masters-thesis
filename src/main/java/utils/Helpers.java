package utils;

import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;

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

    public static <T extends Comparable<T>> T removeRandomElementFromSet(Set<T> set) {
        T element = getRandomElementFromSet(set);
        set.remove(element);
        return element;
    }

    public static Solution deepCopySolution(Solution solution) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    public static List<Order> sortUnplacedOrders(List<Order> unplacedOrders) {
        List<Order> sortedUnplacedOrders = new ArrayList<>();
        int numberOfMand = 0;
        for (Order order : unplacedOrders) {
            if (order.isMandatory()) {
                sortedUnplacedOrders.add(0, order);
                numberOfMand++;
            } else {
                sortedUnplacedOrders.add(order);
            }
        }
        Collections.shuffle(sortedUnplacedOrders.subList(0, numberOfMand), Problem.random);
        Collections.shuffle(sortedUnplacedOrders.subList(numberOfMand, sortedUnplacedOrders.size()), Problem.random);
        return sortedUnplacedOrders;
    }

    public static List<Order> sortOrdersByPenalty(Solution partialSolution) {
        List<Order> sortedOrders = new ArrayList<>();
        List<Order> optionalOrders = new ArrayList<>();
        for (Order order : partialSolution.getUnplacedOrders()) {
            if (order.isMandatory()) {
                sortedOrders.add(order);
            } else {
                optionalOrders.add(order);
            }
        }
        Collections.sort(sortedOrders);  // Sort by id for predictability
        optionalOrders.sort(Comparator.comparing((Order::getPostponementPenalty)).reversed());
        sortedOrders.addAll(optionalOrders);
        return sortedOrders;
    }

    public static List<Order> sortOrdersBySize(Solution partialSolution) {
        List<Order> sortedOrders = new ArrayList<>();
        List<Order> optionalOrders = new ArrayList<>();
        for (Order order : partialSolution.getUnplacedOrders()) {
            if (order.isMandatory()) {
                sortedOrders.add(order);
            } else {
                optionalOrders.add(order);
            }
        }
        Collections.sort(sortedOrders);  // Sort by id for predictability
        optionalOrders.sort(Comparator.comparing((Order::getSize)).reversed());
        sortedOrders.addAll(optionalOrders);
        return sortedOrders;
    }
}
