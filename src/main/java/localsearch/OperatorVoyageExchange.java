package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorVoyageExchange extends Operator {

    private static Map<Solution, Double> solutionToObjective;
    private static Solution originalSolution;
    private static Solution bestSolution;
    private static double bestObjective;

    public static Solution voyageExchange(Solution solution) {
        initialize(solution);

        for (int vIdxOne = 0; vIdxOne < Problem.getNumberOfVessels(); vIdxOne++) {
            for (int vIdxTwo = 0; vIdxTwo < Problem.getNumberOfVessels(); vIdxTwo++) {
                if (vIdxOne == vIdxTwo) continue;
                List<List<Order>> osCopy = Helpers.deepCopy2DList(originalSolution.getOrderSequences());
                Collections.swap(osCopy, vIdxOne, vIdxTwo);
                Solution newSolution = new Solution(osCopy, originalSolution.getAllPostponed(), new HashSet<>());
                if (!Evaluator.isSolutionFeasible(newSolution)) continue;
                Objective.setObjValAndSchedule(newSolution);
                solutionToObjective.put(newSolution, newSolution.getObjective(false));
            }
        }
        updateBest();
        return bestSolution;
    }

    private static void initialize(Solution solution) {
        solutionToObjective = new HashMap<>();
        originalSolution = solution;
        bestSolution = solution;
        bestObjective = solution.getObjective(false);
    }

    private static void updateBest() {
        for (Map.Entry<Solution, Double> entry : solutionToObjective.entrySet()) {
            Solution newSolution = entry.getKey();
            double newObjective = entry.getValue();
            if (newObjective < bestObjective) {
                bestSolution = newSolution;
                bestObjective = newObjective;
            }
        }
    }
}
