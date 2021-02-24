package alns;

import data.Problem;
import objects.Order;
import objects.Vessel;

import java.util.*;

public class Solution {

    private List<List<Order>> orderSequences = new ArrayList<>();
    private Random random = new Random();

    public Solution() {

        List<Vessel> vesselList = new ArrayList<>();
        List<Order> orderList = new ArrayList<>(Problem.orders);
        Collections.shuffle(orderList);

        for(int vesselId = 0; vesselId < Problem.vessels.size(); vesselId++) orderSequences.add(new LinkedList<>());

        while(!orderList.isEmpty()) {
            Order order = orderList.remove(this.getRandomIndex(orderList.size()));
            orderSequences.get(this.getRandomVessel(Problem.vessels.size())).add(order);
        }
    }

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

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        Solution solution = new Solution();
    }
}
