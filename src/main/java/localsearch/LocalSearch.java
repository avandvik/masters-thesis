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
        Solution newSolution = Helpers.deepCopySolution(candidateSolution);
        newSolution = intraVoyageImprovement(newSolution);
        newSolution = interVoyageImprovement(newSolution);
        newSolution = schedulePostponeImprovement(newSolution);
        if (!Evaluator.isSolutionFeasible(newSolution)) throw new IllegalStateException(Messages.infSolCreated);
        SearchHistory.incrementLocalSearchRuns();
        SearchHistory.addToAccLocalSearchImprovement(candidateSolution, bestSolution);
        return newSolution;
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