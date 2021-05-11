package data;

import alns.Solution;
import alns.heuristics.Heuristic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHistory {

    private static Solution bestSolution;
    private static Map<Integer, Double> iterationToObjective;
    private static Map<Heuristic, Map<Integer, Double>> heuristicToIterationToWeight;
    private static int iterationBestSolutionFound;
    private static double runtime;

    public static void initialize(List<Heuristic> heuristics) {
        iterationToObjective = new HashMap<>();
        heuristicToIterationToWeight = new HashMap<>();
        for (Heuristic heuristic : heuristics) heuristicToIterationToWeight.put(heuristic, new HashMap<>());
    }

    public static void setIterationToObjective(int iter, double objective) {
        iterationToObjective.put(iter, objective);
    }

    public static Map<Integer, Double> getIterationToObjective() {
        return iterationToObjective;
    }

    public static void setIterationToWeight(Heuristic heuristic, int iter) {
        heuristicToIterationToWeight.get(heuristic).put(iter, heuristic.getWeight());
    }

    public static Map<Heuristic, Map<Integer, Double>> getHeuristicToIterationToWeight() {
        return heuristicToIterationToWeight;
    }

    public static void setIterationBestSolutionFound(int iter) {
        iterationBestSolutionFound = iter;
    }

    public static void setBestSolutionFound(Solution solution) {
        bestSolution = solution;
    }

    public static double getBestObjective() {
        return bestSolution.getObjective(false);
    }

    public static int getIterationBestSolutionFound() {
        return iterationBestSolutionFound;
    }

    public static void setRuntime(double startTime) {
        runtime = (System.nanoTime() - startTime) / 1e9;
    }

    public static double getRuntime() {
        return runtime;
    }
}
