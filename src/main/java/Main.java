import alns.Solution;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        Problem.setUpProblem("example.json", false);
        Solution solution = new Solution(5);

        runSubProblem(solution);
    }

    private static void runSubProblem(Solution solution) {
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            runSingleSubProblem(solution.getOrderSequence(vesselNumber), vesselNumber);
        }
    }

    private static void runSingleSubProblem(List<Order> orderSequence, int vesselNumber) {
        try {
            SubProblem subproblem = new SubProblem(orderSequence, vesselNumber);
            subproblem.solve();
            System.out.println(subproblem.getShortestPath() + ": " + subproblem.getShortestPathCost());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
