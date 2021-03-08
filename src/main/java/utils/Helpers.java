package utils;

import alns.Solution;
import data.Problem;
import objects.Order;

import java.util.*;
import java.util.stream.IntStream;

public class Helpers {

    public static List<Order> createDummyOrderSequence(int length, int seedValue) {
        Integer[] indicesArray = IntStream.range(0, Problem.orders.size()).boxed().toArray(Integer[]::new);
        List<Integer> indices = Arrays.asList(indicesArray);
        Collections.shuffle(indices, new Random(seedValue));
        List<Order> orderSequence = new LinkedList<>();
        for (int i = 0; i < length; i++) orderSequence.add(Problem.orders.get(indices.get(i)));
        return orderSequence;
    }

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

    public static List<List<Integer>> getInstSequences(List<List<Order>> orderSequences) {
        List<List<Integer>> instSequences = new ArrayList<>();
        for (List<Order> orderSequence : orderSequences) instSequences.add(getInstSequence(orderSequence));
        return instSequences;
    }

    public static List<Integer> getInstSequence(List<Order> orderSequence) {
        List<Integer> instSequence = new ArrayList<>();
        for (int i = 0; i < orderSequence.size(); i++) instSequence.add(orderSequence.get(i).getInstallationId());
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

    public static Solution copySolution(Solution solution) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> posponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        return new Solution(orderSequences, posponedOrders, unplacedOrders);
    }
}
