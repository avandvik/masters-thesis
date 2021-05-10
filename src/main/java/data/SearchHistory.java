package data;

import alns.heuristics.Heuristic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHistory {

    private static final Map<Integer, Double> iterationToObjective = new HashMap<>();
    private static final Map<Heuristic, Map<Integer, Double>> heuristicToIterationToWeight = new HashMap<>();
    private static int iterationBestSolutionFound;
    private static double runtime;

    public static void initialize(List<Heuristic> heuristics) {
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
