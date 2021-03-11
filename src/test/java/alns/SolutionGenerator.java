package alns;

import data.Problem;
import objects.Order;

import java.util.*;

public class SolutionGenerator {

    public static Solution createSolutionBasicTestData(int sep1, int sep2) {
        List<List<Order>> orderSequences = new ArrayList<>();
        Set<Order> postponedOrders = new HashSet<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());
        for (int i = 0; i < sep1; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = sep1; i < sep2; i++) orderSequences.get(1).add(Problem.getOrder(i));
        for (int i = sep2; i < Problem.getNumberOfOrders(); i++) orderSequences.get(2).add(Problem.getOrder(i));
        return new Solution(orderSequences, postponedOrders, false);
    }
}
