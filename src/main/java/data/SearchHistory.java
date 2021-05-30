package data;

import alns.Solution;
import alns.heuristics.Heuristic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchHistory {

    private static Solution bestSolution;
    private static double constructionHeuristicObjective;
    private static String bestSolFoundBy;
    private static Map<Integer, Double> iterationToObjective;
    private static List<Heuristic> heuristicsUsedInSearch;
    private static Map<Heuristic, Map<Integer, Double>> heuristicToIterationToWeight;
    private static Map<String, Integer> operatorToNbrImprovements;
    private static int iterationBestSolutionFound;
    private static double runtime;
    private static int nbrIterations;
    private static int nbrLocalSearchRuns;
    private static double accLocalSearchImprovement;
    private static double bestLocalSearchImprovement;
    private static int nbrImprovementsBySetPartitioning;
    private static int nbrImprovementsByLocalSearch;
    private static int nbrImprovementsByDestroyRepair;

    public static void initialize(List<Heuristic> heuristics) {
        iterationToObjective = new HashMap<>();
        heuristicToIterationToWeight = new HashMap<>();
        heuristicsUsedInSearch = heuristics;
        for (Heuristic heuristic : heuristicsUsedInSearch) heuristicToIterationToWeight.put(heuristic, new HashMap<>());
        operatorToNbrImprovements = new HashMap<>();
        operatorToNbrImprovements.put(Constants.ONE_EXCHANGE_NAME, 0);
        operatorToNbrImprovements.put(Constants.TWO_EXCHANGE_NAME, 0);
        operatorToNbrImprovements.put(Constants.ONE_RELOCATE_NAME, 0);
        operatorToNbrImprovements.put(Constants.TWO_RELOCATE_NAME, 0);
        operatorToNbrImprovements.put(Constants.POSTPONE_SCHEDULED_NAME, 0);
        operatorToNbrImprovements.put(Constants.SCHEDULE_POSTPONED_NAME, 0);
        operatorToNbrImprovements.put(Constants.VOYAGE_EXCHANGE_NAME, 0);
        iterationBestSolutionFound = 0;
        runtime = 0.0;
        nbrIterations = 0;
        nbrLocalSearchRuns = 0;
        accLocalSearchImprovement = 0.0;
        bestLocalSearchImprovement = 0.0;
        nbrImprovementsBySetPartitioning = 0;
    }

    public static void setConstructionHeuristicObjective(double obj) {
        constructionHeuristicObjective = obj;
    }

    public static double getConstructionHeuristicObjective() {
        return constructionHeuristicObjective;
    }

    public static void setBestSolFoundBy(String method) {
        bestSolFoundBy = method;
    }

    public static String getBestSolFoundBy() {
        return bestSolFoundBy;
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

    public static Map<Heuristic, Integer> getHeuristicToSelections() {
        Map<Heuristic, Integer> heuristicToSelections = new HashMap<>();
        for (Heuristic heuristic : heuristicsUsedInSearch) {
            heuristicToSelections.put(heuristic, heuristic.getSelections());
        }
        return heuristicToSelections;
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

    public static double getBestFuelCosts() {
        return bestSolution.getFuelCosts();
    }

    public static double getBestPenaltyCosts() {
        return bestSolution.getPenaltyCosts();
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

    public static void setNbrIterations(int iter) {
        nbrIterations = iter;
    }

    public static int getNbrIterations() {
        return nbrIterations;
    }

    public static void incrementLocalSearchRuns() {
        nbrLocalSearchRuns++;
    }

    public static int getNbrLocalSearchRuns() {
        return nbrLocalSearchRuns;
    }

    public static void updateLocalSearchImprovementData(Solution newSolution, Solution candidateSolution) {
        double newObj = newSolution.getObjective(false);
        double candidateObj = candidateSolution.getObjective(false);
        double improvement = (candidateObj - newObj) / candidateObj;
        accLocalSearchImprovement += improvement;
        if (improvement > bestLocalSearchImprovement) bestLocalSearchImprovement = improvement;
    }

    public static double getAvgLocalSearchImprovement() {
        return accLocalSearchImprovement / nbrLocalSearchRuns;
    }

    public static double getBestLocalSearchImprovement() {
        return bestLocalSearchImprovement;
    }

    public static void incrementNbrImprovementsBySetPartitioning() {
        nbrImprovementsBySetPartitioning++;
    }

    public static int getNbrImprovementsBySetPartitioning() {
        return nbrImprovementsBySetPartitioning;
    }

    public static void updateLSOrDRImprovements(Solution candidateSolution, Solution iterSolution) {
        if (candidateSolution.equals(iterSolution)) {
            incrementNbrImprovementsByDestroyRepair();
            setBestSolFoundBy(Constants.DESTROY_REPAIR_NAME);
        } else {
            incrementNbrImprovementsByLocalSearch();
            setBestSolFoundBy(Constants.LOCAL_SEARCH_NAME);
        }
    }

    public static void incrementNbrImprovementsByLocalSearch() {
        nbrImprovementsByLocalSearch++;
    }

    public static int getNbrImprovementsByLocalSearch() {
        return nbrImprovementsByLocalSearch;
    }
    public static void incrementNbrImprovementsByDestroyRepair() {
        nbrImprovementsByDestroyRepair++;
    }

    public static int getNbrImprovementsByDestroyRepair() {
        return nbrImprovementsByDestroyRepair;
    }

    public static void incrementNbrImprovementsByOperator(String operatorName, Solution oldSol, Solution newSol) {
        if (newSol.getObjective(false) < oldSol.getObjective(false)) {
            int nbrImprovements = operatorToNbrImprovements.get(operatorName);
            operatorToNbrImprovements.put(operatorName, ++nbrImprovements);
        }
    }

    public static Map<String, Integer> getOperatorToNbrImprovements() {
        return operatorToNbrImprovements;
    }
}
