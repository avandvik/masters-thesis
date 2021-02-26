package alns;

import data.Problem;
import objects.Order;

import java.util.*;

public class Solution {

    private List<List<Order>> orderSequences = new ArrayList<>();
    private Random random = new Random();

    public Solution() {
        List<Order> orderList = new ArrayList<>(Problem.orders);
        Collections.shuffle(orderList);

        for(int vesselId = 0; vesselId < Problem.vessels.size(); vesselId++) orderSequences.add(new LinkedList<>());

        while(!orderList.isEmpty()) {
            Order order = orderList.remove(this.getRandomIndex(orderList.size()));
            orderSequences.get(this.getRandomVessel(Problem.vessels.size())).add(order);
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

    public List<Order> getOrderSequence(int vesselNumber) {
        return this.orderSequences.get(vesselNumber);
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
