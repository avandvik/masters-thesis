package localsearch;

import alns.Solution;

public class LocalSearch {

    public static Solution localSearch(Solution solution) {
        Solution improvedSolution = intraVoyageImprovement(solution);
        improvedSolution = interVoyageImprovement(improvedSolution);  // TODO: Use solution or improvedSolution?
        return improvedSolution;
    }

    public static Solution intraVoyageImprovement(Solution solution) {
        Solution improvedSolution = OperatorOneRelocate.oneRelocate(solution);
        return improvedSolution;
    }

    public static Solution interVoyageImprovement(Solution solution) {
        Solution improvedSolution;

        return solution;
    }
}
