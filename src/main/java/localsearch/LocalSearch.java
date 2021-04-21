package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Messages;

public class LocalSearch {

    public static Solution localSearch(Solution solution) {
        Solution improvedSolution = intraVoyageImprovement(solution);
        improvedSolution = interVoyageImprovement(improvedSolution);  // TODO: Use solution or improvedSolution?

        if (!Evaluator.isSolutionFeasible(improvedSolution)) throw new IllegalStateException(Messages.infeasibleSolutionCreated);
        Objective.setObjValAndSchedule(improvedSolution);
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
