package localsearch;

import alns.Evaluator;
import alns.Solution;
import data.Messages;
import data.Parameters;
import utils.Helpers;

public class LocalSearch {

    public static Solution localSearch(Solution candidateSolution, Solution bestSolution) {
        double objDeviation = candidateSolution.getObjective(false) / bestSolution.getObjective(false);
        if (objDeviation > Parameters.lsThresh) return candidateSolution;
        Solution newSolution = Helpers.deepCopySolution(candidateSolution);
        newSolution = intraVoyageImprovement(newSolution);
        newSolution = interVoyageImprovement(newSolution);
        newSolution = schedulePostponeImprovement(newSolution);
        if (!Evaluator.isSolutionFeasible(newSolution)) throw new IllegalStateException(Messages.infSolCreated);
        return newSolution;
    }

    private static Solution intraVoyageImprovement(Solution solution) {
        Solution newSolution = OperatorOneRelocate.oneRelocate(solution);
        newSolution = OperatorOneExchange.oneExchange(newSolution);
        return newSolution;
    }

    private static Solution interVoyageImprovement(Solution solution) {
        Solution newSolution = OperatorTwoRelocate.twoRelocate(solution);
        newSolution = OperatorTwoExchange.twoExchange(newSolution);
        return newSolution;
    }

    private static Solution schedulePostponeImprovement(Solution solution) {
        Solution newSolution = OperatorPostponeScheduled.postponeScheduled(solution);
        newSolution = OperatorSchedulePostponed.schedulePostponed(newSolution);
        return newSolution;
    }

    private static Solution applyOperator(int operatorNo, Solution solution) {
        switch (operatorNo) {
            case 1:
                return OperatorOneExchange.oneExchange(solution);
            case 2:
                return OperatorTwoExchange.twoExchange(solution);
            case 3:
                return OperatorOneRelocate.oneRelocate(solution);
            case 4:
                return OperatorTwoRelocate.twoRelocate(solution);
            case 5:
                return OperatorSchedulePostponed.schedulePostponed(solution);
            case 6:
                return OperatorPostponeScheduled.postponeScheduled(solution);
            default:
                System.out.println("Unrecognized operator.");
        }
        return solution;
    }
}