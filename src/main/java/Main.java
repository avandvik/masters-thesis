import alns.Solution;
import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false);
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < 3; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 3; i < Problem.getNumberOfOrders(); i++) orderSequences.get(1).add(Problem.getOrder(i));
        Solution solution = new Solution(orderSequences);
        runSubProblem(solution);
    }

    private static void runSubProblem(Solution solution) {
        List<List<Node>> shortestPaths = new ArrayList<>();
        double objectiveValue = 0.0;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            SubProblem subProblem = runSingleSubProblem(solution.getOrderSequence(vesselNumber), vesselNumber);
            shortestPaths.add(subProblem != null ? subProblem.getShortestPath() : new ArrayList<>());
            objectiveValue += subProblem != null ? subProblem.getShortestPathCost() : 0.0;
        }
        solution.setShortestPaths(shortestPaths);
        solution.setObjectiveValue(objectiveValue);

        solution.printSchedules();
    }

    private static SubProblem runSingleSubProblem(List<Order> orderSequence, int vesselNumber) {
        try {
            SubProblem subProblem = new SubProblem(orderSequence, vesselNumber);
            subProblem.solve();
            System.out.println(subProblem.getShortestPath() + ": " + subProblem.getShortestPathCost());
            return subProblem;
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }
}
