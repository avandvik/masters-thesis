import java.util.*;

public class Solution {

    private List<List<Order>> orderSequences = new ArrayList<>();
    private Random random = new Random();

    public Solution(int numberOfVessels, int numberOfOrders) {
        List<Order> orders = new ArrayList<>();
        for (int orderId = 0; orderId < numberOfOrders; orderId++) orders.add(new Order(orderId));
        for (int vesselId = 0; vesselId < numberOfVessels; vesselId++) orderSequences.add(new LinkedList<>());

        while (!orders.isEmpty()) {
            Order order = orders.remove(this.getRandomIndex(orders.size()));
            orderSequences.get(this.getRandomVessel(numberOfVessels)).add(order);
        }
    }

    public int getRandomIndex(int listLength) {
        return random.nextInt(listLength);
    }

    public int getRandomVessel(int numberOfVessels) {
        return random.nextInt(numberOfVessels);
    }

    public List<List<Order>> getOrderSequences() {
        return orderSequences;
    }

    @Override
    public String toString() {
        return this.orderSequences.toString();
    }
}
