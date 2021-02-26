import alns.Solution;
import data.Problem;
import subproblem.SubProblem;

public class Main {

    public static void main(String[] args) {

        Problem.setUpProblem("example.json");
        Solution solution = new Solution(5);
        System.out.println(solution);
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
}
