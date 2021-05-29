package localsearch;

import alns.Evaluator;
import alns.Solution;
import data.Messages;
import data.Parameters;
import data.SearchHistory;
import utils.Helpers;

public class LocalSearch {

    public static Solution localSearch(Solution candidateSolution, Solution bestSolution) {
        if (notWorthRunningLS(candidateSolution, bestSolution)) return candidateSolution;
        Solution lsSolution = Helpers.deepCopySolution(candidateSolution);
        lsSolution = intraVoyageImprovement(lsSolution);
        lsSolution = interVoyageImprovement(lsSolution);
        lsSolution = schedulePostponeImprovement(lsSolution);
        if (!Evaluator.isSolutionFeasible(lsSolution)) throw new IllegalStateException(Messages.infSolCreated);
        SearchHistory.incrementLocalSearchRuns();
        SearchHistory.updateLocalSearchImprovementData(lsSolution, candidateSolution);
        return lsSolution;
    }

    private static boolean notWorthRunningLS(Solution candidateSolution, Solution bestSolution) {
        double candidateObj = candidateSolution.getObjective(false);
        double bestObj = bestSolution.getObjective(false);
        double gapToBest = (candidateObj - bestObj) / bestObj;
        return gapToBest > Parameters.lsThresh;
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