package localsearch;

import alns.Solution;

public class LocalSearch {

    public static Solution localSearch(Solution solution) {
        Solution newSolution = intraVoyageImprovement(solution);
        newSolution = interVoyageImprovement(newSolution);
        newSolution = schedulePostponeImprovement(newSolution);
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