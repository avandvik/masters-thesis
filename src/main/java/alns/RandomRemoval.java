package alns;

import objects.Order;
import utils.Helpers;

import java.util.List;
import java.util.Random;

public class RandomRemoval {

    private final static Random random = new Random();

    public static Solution removeRandomOrders(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopyList(solution.getOrderSequences());

        while (numberOfOrders > 0) {
            int randomSequenceNumber = random.nextInt(orderSequences.size());
            if (orderSequences.get(randomSequenceNumber).size() == 0) continue;
            int randomOrderNumber = random.nextInt(orderSequences.get(randomSequenceNumber).size());
            orderSequences.get(randomSequenceNumber).remove(randomOrderNumber);
            numberOfOrders--;
        }

        return new Solution(orderSequences);
    }
}
