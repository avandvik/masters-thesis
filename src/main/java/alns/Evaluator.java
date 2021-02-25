package alns;

import data.Problem;
import objects.Order;

import java.util.List;

public class Evaluator {

    public static boolean evaluateLoad(List<List<Order>> orderSequences) {
        for(int i = 0; i < orderSequences.size(); i++) {
            double currentLoad = findTotalStartLoad(orderSequences.get(i));
            if(currentLoad > Problem.vessels.get(i).getCapacity()) return false;
            for(int j = 0; j < orderSequences.get(i).size(); j++) {
                if(orderSequences.get(i).get(j).isDelivery()) {
                    currentLoad -= orderSequences.get(i).get(j).getSize();
                } else {
                    currentLoad += orderSequences.get(i).get(j).getSize();
                }
                if(currentLoad > Problem.vessels.get(i).getCapacity()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double findTotalStartLoad(List<Order> orderSequence) {
        double totalStartLoad = 0.0;
        for (Order order : orderSequence) {
            if (order.isDelivery()) totalStartLoad += order.getSize();
        }
        return totalStartLoad;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        Solution solution = new Solution();
        List<List<Order>> orderSequences = solution.getOrderSequences();
        evaluateLoad(orderSequences);
    }
}
