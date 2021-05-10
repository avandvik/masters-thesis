package localsearch;

import alns.Evaluator;
import alns.Solution;
import data.Messages;
import data.Parameters;
import utils.Helpers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalSearch {

    public static Solution localSearch(Solution solution) {
        Solution newSolution = Helpers.deepCopySolution(solution);
        if (Parameters.exhaustiveLocalSearch) {
            newSolution = intraVoyageImprovement(newSolution);
            newSolution = interVoyageImprovement(newSolution);
            newSolution = schedulePostponeImprovement(newSolution);
        } else if (Parameters.randomLocalSearch) {
            List<Integer> operatorNumbers = IntStream.rangeClosed(1, 6).boxed().collect(Collectors.toList());
            Collections.shuffle(operatorNumbers);
            for (int i = 0; i < Parameters.numberOfOperators; i++) {
                newSolution = applyOperator(operatorNumbers.get(i), newSolution);
            }
        }
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