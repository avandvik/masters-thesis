package utils;

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

    public static <T> List<T> deepCopyList(List<T> original) {
        return new ArrayList<>(original);
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

    public static int getStartTimeOfRoughWeather() {
        for (int timePoint = 0; timePoint < Problem.weatherForecastDisc.size(); timePoint++) {
            int ws = Problem.weatherForecastDisc.get(timePoint);
            if (ws == Problem.worstWeatherState) {
                return timePoint;
            }
        }
        return -1;
    }
}
