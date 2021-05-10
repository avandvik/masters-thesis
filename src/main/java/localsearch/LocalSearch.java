package localsearch;

import alns.Evaluator;
import alns.Solution;
import data.Messages;

public class LocalSearch {

    public static Solution localSearch(Solution solution) {
        Solution newSolution = intraVoyageImprovement(solution);
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
}