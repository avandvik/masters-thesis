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
}
