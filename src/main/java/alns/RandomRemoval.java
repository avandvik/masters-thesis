package alns;

import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomRemoval {

    public static Solution removeRandomOrders(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopyList(solution.getOrderSequences());

        while (numberOfOrders > 0) {
            int randomSequenceNumber = ThreadLocalRandom.current().nextInt(0, orderSequences.size());
            if (orderSequences.get(randomSequenceNumber).size() == 0) continue;
            int randomOrderNumber = ThreadLocalRandom.current().nextInt(0, orderSequences.get(randomSequenceNumber).size());
            orderSequences.get(randomSequenceNumber).remove(randomOrderNumber);
            numberOfOrders--;
        }

        return new Solution(orderSequences);
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false);
        List<Order> orders = Problem.orders;
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());

        for (int i = 0; i < 2; i++) {
            orderSequences.get(0).add(orders.get(i));
        }
        for (int j = 2; j < 5; j++) {
            orderSequences.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 7; k++) {
            orderSequences.get(2).add(orders.get(k));
        }

        Solution solution = new Solution(orderSequences);
        removeRandomOrders(solution,2);
    }
}
