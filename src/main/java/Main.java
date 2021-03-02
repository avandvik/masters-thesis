import alns.Solution;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        Problem.setUpProblem("example.json");
        Solution solution = new Solution(5);

        runSubProblem(solution);

        // Testing for GitHub Actions
    }

    private static void runSubProblem(Solution solution) {
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            try {
                SubProblem subproblem = new SubProblem(solution.getOrderSequence(vesselNumber), vesselNumber);
                subproblem.solve();
                System.out.println(subproblem.getShortestPath() + ": " + subproblem.getShortestPathCost());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private static void runSubProblem(List<Order> orderSequence, int vesselNumber) {
        try {
            SubProblem subproblem = new SubProblem(orderSequence, vesselNumber);
            subproblem.solve();
            System.out.println(subproblem.getShortestPath() + ": " + subproblem.getShortestPathCost());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
