package alns;

import data.Problem;
import objects.Order;

import java.util.List;

public class Evaluator {

    public static boolean evaluateLoad(Solution solution) {
        List<List<Order>> orderSequences = solution.getOrderSequences();
        for(int vesselNumber = 0; vesselNumber < orderSequences.size(); vesselNumber++) {
            double currentLoad = findTotalStartLoad(orderSequences.get(vesselNumber));
            if(currentLoad > Problem.vessels.get(vesselNumber).getCapacity()) return false;
            for(Order order : orderSequences.get(vesselNumber)) {
                if(order.isDelivery()) {
                    currentLoad -= order.getSize();
                } else {
                    currentLoad += order.getSize();
                }
                if(currentLoad > Problem.vessels.get(vesselNumber).getCapacity()) {
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
        evaluateLoad(solution);
    }
}
