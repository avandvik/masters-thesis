package localsearch;

import alns.Evaluator;
import alns.Solution;
import data.*;

public class LocalSearch {

    public static Solution localSearch(Solution candidateSolution, Solution bestSolution) {
        if (notWorthRunningLS(candidateSolution, bestSolution)) return candidateSolution;
        Solution lsSolution = voyageExchangeImprovement(candidateSolution);
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
        return gapToBest > Parameters.lsMaxGap;
    }

    private static Solution voyageExchangeImprovement(Solution solution) {
        Solution newSolution = OperatorVoyageExchange.voyageExchange(solution);
        SearchHistory.incrementNbrImprovementsByOperator(Constants.VOYAGE_EXCHANGE_NAME, solution, newSolution);
        return newSolution;
    }

    private static Solution intraVoyageImprovement(Solution solution) {
        Solution firstSol = solution;
        if (Problem.random.nextDouble() < Parameters.lsOperatorRunLimit) {
            firstSol = OperatorOneRelocate.oneRelocate(solution);
            SearchHistory.incrementNbrImprovementsByOperator(Constants.ONE_RELOCATE_NAME, solution, firstSol);
        }
        Solution secondSol = firstSol;
        if (Problem.random.nextDouble() < Parameters.lsOperatorRunLimit) {
            secondSol = OperatorOneExchange.oneExchange(firstSol);
            SearchHistory.incrementNbrImprovementsByOperator(Constants.ONE_EXCHANGE_NAME, firstSol, secondSol);
        }
        return secondSol;
    }

    private static Solution interVoyageImprovement(Solution solution) {
        Solution firstSol = solution;
        if (Problem.random.nextDouble() < Parameters.lsOperatorRunLimit) {
            firstSol = OperatorTwoRelocate.twoRelocate(solution);
            SearchHistory.incrementNbrImprovementsByOperator(Constants.TWO_RELOCATE_NAME, solution, firstSol);
        }
        Solution secondSol = firstSol;
        if (Problem.random.nextDouble() < Parameters.lsOperatorRunLimit) {
            secondSol = OperatorTwoExchange.twoExchange(firstSol);
            SearchHistory.incrementNbrImprovementsByOperator(Constants.TWO_EXCHANGE_NAME, firstSol, secondSol);
        }
        return secondSol;
    }

    private static Solution schedulePostponeImprovement(Solution solution) {
        Solution firstSol = solution;
        if (Problem.random.nextDouble() < 0) {
            firstSol = OperatorPostponeScheduled.postponeScheduled(solution);
            SearchHistory.incrementNbrImprovementsByOperator(Constants.POSTPONE_SCHEDULED_NAME, solution, firstSol);
        }
        Solution secondSol = firstSol;
        if (Problem.random.nextDouble() < Parameters.lsOperatorRunLimit) {
            secondSol = OperatorSchedulePostponed.schedulePostponed(solution);
            SearchHistory.incrementNbrImprovementsByOperator(Constants.SCHEDULE_POSTPONED_NAME, solution, secondSol);
        }
        return secondSol;
    }
}