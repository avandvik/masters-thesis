package alns;

import alns.heuristics.*;
import alns.heuristics.protocols.Destroyer;
import alns.heuristics.protocols.Repairer;
import data.*;
import localsearch.LocalSearch;
import setpartitioning.Data;
import setpartitioning.Model;
import setpartitioning.Pool;
import utils.IO;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final char[] animationChars = new char[]{'|', '/', '-', '\\'};

    private static List<Heuristic> allHeuristics;
    private static List<Heuristic> dHeuristics;
    private static List<Heuristic> rHeuristics;

    private static Solution currentSolution;
    private static Solution bestSolution;
    private static List<Integer> visitedSolutions;

    private static int iterationsCurrentSolution;
    private static double currentTemperature;

    public static void alns(double startTime) {
        initialize();
        for (int iter = 0; iter < Parameters.totalIter; iter++) {
            iterationsCurrentSolution++;
            List<Heuristic> heuristics = chooseHeuristics();
            List<Solution> newSolutions = generateNewSolutions(heuristics);
            Solution candidateSolution = newSolutions.get(0);
            Solution lsSolution = newSolutions.get(1);
            if (candidateSolution == null) continue;
            printIterationInfo(iter, candidateSolution);
            double reward = acceptSolution(candidateSolution, lsSolution, iter);
            if (Parameters.setPartitioning && Parameters.setPartIterations.contains(iter + 1)) runSetPartitioning(iter);
            maintenance(reward, heuristics, iter);
            if ((System.nanoTime() - startTime) / 1e9 > Parameters.maxRunTime || iter + 1 == Parameters.totalIter) {
                SearchHistory.setNbrIterations(iter + 1);
                break;
            }
        }
    }

    public static void initialize() {
        if (Parameters.setPartitioning) Data.initializeGurobiEnv();
        Cache.initialize();
        Pool.initialize();
        initializeHeuristics();
        initializeSolutionFields();
        initializeSimulatedAnnealing();
        SearchHistory.initialize(allHeuristics);
    }

    private static void initializeHeuristics() {
        dHeuristics = new ArrayList<>();
        rHeuristics = new ArrayList<>();
        dHeuristics.add(new RemovalRandom(Constants.REMOVAL_RANDOM_NAME));
        dHeuristics.add(new RemovalRelated(Constants.REMOVAL_RELATED_NAME));
        dHeuristics.add(new RemovalWorst(Constants.REMOVAL_WORST_NAME));
        dHeuristics.add(new RemovalCluster(Constants.REMOVAL_CLUSTER_NAME));
        dHeuristics.add(new RemovalSpread(Constants.REMOVAL_SPREAD_NAME));
        dHeuristics.add(new RemovalSpot(Constants.REMOVAL_SPOT_NAME));
        rHeuristics.add(new InsertionGreedy(Constants.INSERTION_GREEDY_NAME));
        rHeuristics.add(new InsertionRegret(Constants.INSERTION_REGRET_NAME));
        rHeuristics.add(new InsertionMaxPenaltyCost(Constants.INSERTION_MAX_PENALTY_NAME));
        rHeuristics.add(new InsertionMaxOrderSize(Constants.INSERTION_MAX_ORDER_SIZE_NAME));
        for (Heuristic heuristic : dHeuristics) heuristic.setWeight(Parameters.initialWeight);
        for (Heuristic heuristic : rHeuristics) heuristic.setWeight(Parameters.initialWeight);
        allHeuristics = new ArrayList<>(dHeuristics);
        allHeuristics.addAll(rHeuristics);
    }

    private static void initializeSolutionFields() {
        currentSolution = Construction.constructGreedyInitialSolution();
        bestSolution = currentSolution;
        SearchHistory.setBestSolutionFound(currentSolution);
        visitedSolutions = new ArrayList<>();
        visitedSolutions.add(currentSolution.hashCode());
        iterationsCurrentSolution = 0;
    }

    private static void initializeSimulatedAnnealing() {
        Parameters.setTemperatureAndCooling(currentSolution.getObjective(false));
        currentTemperature = Parameters.startTemperature;
    }

    private static List<Heuristic> chooseHeuristics() {
        Heuristic chosenDestroy = Parameters.roulette ? rouletteSelection(dHeuristics) : randomSelection(dHeuristics);
        Heuristic chosenRepair = Parameters.roulette ? rouletteSelection(rHeuristics) : randomSelection(rHeuristics);
        chosenDestroy.incrementSelections();
        chosenRepair.incrementSelections();
        return new ArrayList<>(Arrays.asList(chosenDestroy, chosenRepair));
    }

    private static Heuristic rouletteSelection(List<Heuristic> heuristics) {
        double weights = heuristics.stream().mapToDouble(Heuristic::getWeight).sum();
        List<Double> probabilities = heuristics.stream().map(o -> o.getWeight() / weights).collect(Collectors.toList());
        TreeMap<Double, Heuristic> rouletteWheel = new TreeMap<>();
        double aggregatedProbability = 0.0;
        for (int idx = 0; idx < heuristics.size(); idx++) {
            aggregatedProbability += probabilities.get(idx);
            rouletteWheel.put(aggregatedProbability, heuristics.get(idx));
        }
        return rouletteWheel.higherEntry(Problem.random.nextDouble()).getValue();
    }

    private static Heuristic randomSelection(List<Heuristic> heuristics) {
        return heuristics.get(Problem.random.nextInt(heuristics.size()));
    }

    public static List<Solution> generateNewSolutions(List<Heuristic> heuristics) {
        Solution candidateSolution = null;
        Solution lsSolution = null;
        try {
            candidateSolution = applyHeuristics(currentSolution, heuristics);
            if (Parameters.localSearch) lsSolution = LocalSearch.localSearch(candidateSolution, bestSolution);
        } catch (IllegalStateException e) {
            System.out.println("\n" + e.getMessage() + "(" + heuristics + ")");
            candidateSolution = Construction.constructRandomInitialSolution();
            lsSolution = LocalSearch.localSearch(candidateSolution, bestSolution);
        }
        return new ArrayList<>(Arrays.asList(candidateSolution, lsSolution));
    }

    private static Solution applyHeuristics(Solution solution, List<Heuristic> heuristics) {
        Destroyer destroyer = (Destroyer) heuristics.get(0);
        Solution partialSolution = destroyer.destroy(solution);
        Repairer repairer = (Repairer) heuristics.get(1);
        Solution candidateSolution = repairer.repair(partialSolution);
        if (!Evaluator.isSolutionFeasible(candidateSolution)) throw new IllegalStateException(Messages.solInfeasible);
        return candidateSolution;
    }

    public static Double acceptSolution(Solution candidateSolution, Solution lsSolution, int iter) {
        Solution iterSolution = getIterationBestSolution(candidateSolution, lsSolution);
        if (iterSolution.getObjective(false) < bestSolution.getObjective(false)) {
            return doGlobalBestUpdates(candidateSolution, iterSolution, iter);
        } else if (simulatedAnnealing(currentSolution.getObjective(false), iterSolution.getObjective(false))) {
            return doLocalUpdates(candidateSolution, iterSolution);
        } else {
            if (iterationsCurrentSolution > Parameters.maxIterSolution) {
                currentSolution = Construction.constructRandomInitialSolution();
                iterationsCurrentSolution = 0;
            }
        }
        return 0.0;
    }

    private static Solution getIterationBestSolution(Solution candidateSolution, Solution lsSolution) {
        Solution iterSolution = candidateSolution;
        if (lsSolution != null && lsSolution.getObjective(false) < candidateSolution.getObjective(false)) {
            iterSolution = lsSolution;
        }
        return iterSolution;
    }

    private static double doGlobalBestUpdates(Solution candidateSolution, Solution iterSolution, int iter) {
        bestSolution = iterSolution;
        currentSolution = candidateSolution;
        visitedSolutions.add(candidateSolution.hashCode());
        iterationsCurrentSolution = 0;
        SearchHistory.setIterationBestSolutionFound(iter);
        SearchHistory.setBestSolutionFound(iterSolution);
        SearchHistory.updateLSOrDRImprovements(candidateSolution, iterSolution);
        return Parameters.newGlobalBest;
    }

    private static double doLocalUpdates(Solution candidateSolution, Solution iterSolution) {
        currentSolution = candidateSolution;
        iterationsCurrentSolution = 0;
        if (!visitedSolutions.contains(candidateSolution.hashCode())) {
            visitedSolutions.add(candidateSolution.hashCode());
            if (iterSolution.getObjective(false) < currentSolution.getObjective(false)) {
                return Parameters.newLocalImprovement;
            } else {
                return Parameters.newLocal;
            }
        }
        return 0.0;  // No reward if solution has been visited before, but current solution is updated
    }

    private static void runSetPartitioning(int iter) {
        Model model = new Model();
        Solution setPartSolution;
        try {
            model.run();
            setPartSolution = model.getNewSolution();
        } catch (IllegalStateException | NullPointerException e) {
            System.out.println(Messages.errorInSetPartitioning);
            return;
        }
        if (setPartSolution.getObjective(false) < bestSolution.getObjective(false)) {
            SearchHistory.incrementNbrImprovementsBySetPartitioning();
            SearchHistory.setBestSolFoundBy(Constants.SET_PARTITIONING_NAME);
        }
        acceptSolution(setPartSolution, null, iter);  // Reward is ignored
    }

    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Problem.random.nextDouble() < Math.exp(-(candidateFitness - currentFitness) / currentTemperature);
    }

    private static void maintenance(double reward, List<Heuristic> heuristics, int iteration) {
        currentTemperature *= Parameters.coolingRate;
        for (Heuristic heuristic : heuristics) heuristic.addToScore(reward);
        if ((iteration + 1) % Parameters.segmentIter == 0) resetHeuristicScores();
        if (iteration % Parameters.searchHistoryIter == 0) {
            SearchHistory.setIterationToObjective(iteration, bestSolution.getObjective(false));
            for (Heuristic heuristic : allHeuristics) SearchHistory.setIterationToWeight(heuristic, iteration);
        }
    }

    private static void resetHeuristicScores() {
        for (Heuristic heuristic : dHeuristics) heuristic.resetScoreAndUpdateWeight();
        for (Heuristic heuristic : rHeuristics) heuristic.resetScoreAndUpdateWeight();
    }

    public static Solution getCurrentSolution() {
        return currentSolution;
    }

    public static Solution getBestSolution() {
        return bestSolution;
    }

    public static void setCurrentSolution(Solution solution) {
        currentSolution = solution;
    }

    public static void setBestSolution(Solution solution) {
        bestSolution = solution;
    }

    public static void setCurrentTemperature(double temperature) {
        currentTemperature = temperature;
    }

    private static void printIterationInfo(int iteration, Solution candidateSolution) {
        if (Parameters.verbose) {
            printHeavy(iteration, candidateSolution);
        } else if (Parameters.semiVerbose) {
            printSubtle(iteration);
        }
    }

    private static void printHeavy(int iteration, Solution candidateSolution) {
        System.out.println("_".repeat(400));
        System.out.println("Iteration: " + iteration + "\n");
        System.out.println(Constants.ANSI_GREEN + "Best solution \n" + bestSolution + "\n"
                + "Objective: " + bestSolution.getObjective(false)
                + "\nHash: " + bestSolution.hashCode() + Constants.ANSI_RESET);
        System.out.println();
        System.out.println(Constants.ANSI_BLUE + "Current solution \n" + currentSolution
                + "\nObjective: " + currentSolution.getObjective(false)
                + "\nHash: " + currentSolution.hashCode());
        System.out.println("Iterations with current solution: " + iterationsCurrentSolution + Constants.ANSI_RESET);
        System.out.println();
        System.out.println(Constants.ANSI_YELLOW + "Candidate solution \n" + candidateSolution
                + "\nObjective: " + candidateSolution.getObjective(false)
                + "\nHash: " + candidateSolution.hashCode());
        System.out.println("Equal solutions: " + candidateSolution.equals(currentSolution) + Constants.ANSI_RESET);
        System.out.println();
    }

    private static void printSubtle(int iteration) {
        double heapFreeSize = Math.round(Runtime.getRuntime().freeMemory() / 1e9 * 100.0) / 100.0;
        double heapUtilization = Math.round((Constants.MAX_HEAP_SIZE - heapFreeSize) * 100.0) / 100.0;
        int percentage = (int) (((iteration + 1) / (double) Parameters.totalIter) * 100);
        System.out.print("Processing: " + percentage + "% " + animationChars[iteration % 4]
                + "     " + Math.round(bestSolution.getObjective(false))
                + "  |  " + Cache.getTotalCacheSize()
                + "  |  " + Pool.getTotalPoolSize()
                + "  |  " + heapUtilization + "/" + Constants.MAX_HEAP_SIZE
                + "\r");
    }

    private static void printSolutionInfo(double startTime) {
        if (Parameters.semiVerbose) {
            double timeElapsed = (System.nanoTime() - startTime) / 1e9;
            System.out.println("\nBest objective: " + bestSolution.getObjective(false));
            System.out.println("\tFuel costs: " + bestSolution.getFuelCosts());
            System.out.println("\tPenalty costs: " + bestSolution.getPenaltyCosts());
            System.out.println("Time elapsed: " + timeElapsed);
        }
        if (Parameters.printSolution) {
            System.out.println("Postponed orders: " + bestSolution.getAllPostponed());
            bestSolution.printSchedules();
        }
        System.out.println();
    }

    private static void run() {
        Random rn = new Random();
        int seed = rn.nextInt(Parameters.seedBound);
        Problem.setUpProblem(Constants.FILE_NAME, false, seed);
        if (Constants.SOLSTORM) Parameters.setSolstormParameters();
        Problem.setRandom(seed);
        Problem.setVesselPoolAndCacheSize();
        System.out.println("Running with seed: " + seed);
        double startTime = System.nanoTime();
        try {
            Main.alns(startTime);
            SearchHistory.setRuntime(startTime);
            if (Parameters.saveSolution) IO.saveSolution(bestSolution);
            if (Parameters.saveHistory) IO.saveSearchHistory();
            printSolutionInfo(startTime);
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println("Caught RuntimeException, exiting...\n");
        }
    }

    public static void main(String[] args) {
        Constants.FILE_NAME = "19-21-3-2_critical.json";  // If running locally (will be overwritten on Solstorm)
        if (args.length > 0) Constants.setSolstormConstants(args[0], args[1]);
        Main.run();
    }
}
