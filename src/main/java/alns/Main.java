package alns;

import alns.heuristics.*;
import alns.heuristics.protocols.Destroyer;
import alns.heuristics.protocols.Repairer;
import data.*;
import localsearch.LocalSearch;
import objects.Order;
import setpartitioning.Data;
import setpartitioning.Model;
import subproblem.Cache;
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

    public static Map<Integer, Map<List<Order>, Double>> vesselToSequenceToCost;

    public static void alns(double startTime) {
        initialize();
        for (int iter = 0; iter < Parameters.totalIter; iter++) {
            iterationsCurrentSolution++;
            List<Heuristic> heuristics = chooseHeuristics();
            Solution candidateSolution = generateCandidate(heuristics);
            if (candidateSolution == null) continue;
            if (Parameters.setPartitioning) saveOrderSequences(candidateSolution);
            printIterationInfo(iter, candidateSolution);
            double reward = acceptSolution(candidateSolution, iter);
            if (Parameters.setPartitioning && (iter + 1) % Parameters.setPartIter == 0) runSetPartitioning(iter);
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
        initializeHeuristics();
        initializeSolutionFields();
        initializeSimulatedAnnealing();
        initializeSequenceSaving();
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

    private static void initializeSequenceSaving() {
        vesselToSequenceToCost = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            Map<List<Order>, Double> emptySequence = new HashMap<>();
            emptySequence.put(new LinkedList<>(), 0.0);
            vesselToSequenceToCost.put(vesselIdx, emptySequence);
        }
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

    public static Solution generateCandidate(List<Heuristic> heuristics) {
        Solution candidateSolution = null;
        try {
            candidateSolution = applyHeuristics(currentSolution, heuristics);
            if (Parameters.localSearch) candidateSolution = LocalSearch.localSearch(candidateSolution, bestSolution);
        } catch (IllegalStateException e) {
            System.out.println("\n" + e.getMessage());
        }
        return candidateSolution;
    }

    private static Solution applyHeuristics(Solution solution, List<Heuristic> heuristics) {
        Destroyer destroyer = (Destroyer) heuristics.get(0);
        Solution partialSolution = destroyer.destroy(solution);
        Repairer repairer = (Repairer) heuristics.get(1);
        Solution candidateSolution = repairer.repair(partialSolution);
        if (!Evaluator.isSolutionFeasible(candidateSolution)) throw new IllegalStateException(Messages.solInfeasible);
        return candidateSolution;
    }

    private static void saveOrderSequences(Solution candidateSolution) {
        /* Save order sequences in candidateSolution while not exceeding storage limits */
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            if (vesselToSequenceToCost.get(vIdx).keySet().size() > Parameters.poolSize) {
                List<List<Order>> sequences = new ArrayList<>(vesselToSequenceToCost.get(vIdx).keySet());
                Collections.shuffle(sequences, Problem.random);
                int mappingsToDelete = Problem.getNumberOfVessels();
                sequences.subList(0, mappingsToDelete).forEach(vesselToSequenceToCost.get(vIdx).keySet()::remove);
            }
            List<Order> orderSequence = candidateSolution.getOrderSequence(vIdx);
            double cost = Objective.getOrderSequenceCost(orderSequence, vIdx);
            vesselToSequenceToCost.get(vIdx).put(orderSequence, cost);  // Okay if overwrite
        }
    }

    public static Double acceptSolution(Solution candidateSolution, int iter) {
        if (candidateSolution.equals(currentSolution)) {
            if (iterationsCurrentSolution > Parameters.maxIterSolution) {
                currentSolution = Construction.constructRandomInitialSolution();
                iterationsCurrentSolution = 0;
            }
        } else if (candidateSolution.getObjective(false) < bestSolution.getObjective(false)) {
            return doGlobalBestUpdates(candidateSolution, iter);
        } else if (simulatedAnnealing(currentSolution.getObjective(false), candidateSolution.getObjective(false))) {
            return doLocalUpdates(candidateSolution);
        }
        return 0.0;
    }

    private static double doGlobalBestUpdates(Solution candidateSolution, int iter) {
        bestSolution = candidateSolution;
        currentSolution = candidateSolution;
        visitedSolutions.add(candidateSolution.hashCode());
        iterationsCurrentSolution = 0;
        SearchHistory.setIterationBestSolutionFound(iter);
        SearchHistory.setBestSolutionFound(candidateSolution);
        return Parameters.newGlobalBest;
    }

    private static double doLocalUpdates(Solution candidateSolution) {
        currentSolution = candidateSolution;
        iterationsCurrentSolution = 0;
        if (!visitedSolutions.contains(candidateSolution.hashCode())) {
            visitedSolutions.add(candidateSolution.hashCode());
            if (candidateSolution.getObjective(false) < currentSolution.getObjective(false)) {
                return Parameters.newLocalImprovement;
            } else {
                return Parameters.newLocal;
            }
        }
        return 0.0;  // No reward if solution has been visited before, but current solution is updated
    }

    private static void runSetPartitioning(int iter) {
        Model model = new Model();
        model.run();
        Solution candidateSolution;
        try {
            candidateSolution = model.getNewSolution();
        } catch (NullPointerException e) {
            System.out.println(Messages.errorInSetPartitioning);
            return;
        }
        if (candidateSolution.getObjective(false) < bestSolution.getObjective(false)) {
            SearchHistory.incrementNbrImprovementsBySetPartitioning();
        }
        acceptSolution(candidateSolution, iter);  // Reward is ignored
    }

    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Problem.random.nextDouble() < Math.exp(-(candidateFitness - currentFitness) / currentTemperature);
    }

    private static void maintenance(double reward, List<Heuristic> heuristics, int iteration) {
        currentTemperature *= Parameters.coolingRate;
        for (Heuristic heuristic : heuristics) heuristic.addToScore(reward);
        if ((iteration + 1) % Parameters.segmentIter == 0) resetHeuristicScores();
        Cache.cacheLongTerm();
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

    public static int getNumberOfSavedSequences() {
        int nbrSequences = 0;
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            nbrSequences += vesselToSequenceToCost.get(vIdx).size();
        }
        return nbrSequences;
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
                + "  |  " + Cache.getCacheSize()
                + "  |  " + getNumberOfSavedSequences()
                + "  |  " + heapUtilization + "/" + Constants.MAX_HEAP_SIZE
                + "\r");
    }

    private static void printSolutionInfo(double startTime) {
        if (Parameters.semiVerbose) {
            double timeElapsed = (System.nanoTime() - startTime) / 1e9;
            System.out.println("\nBest objective: " + Main.getBestSolution().getObjective(false));
            System.out.println("\tFuel costs: " + Main.getBestSolution().getFuelCosts());
            System.out.println("\tPenalty costs: " + Main.getBestSolution().getPenaltyCosts());
            System.out.println("Time elapsed: " + timeElapsed);
        }
        if (Parameters.printSolution) {
            System.out.println("Postponed orders: " + Main.getBestSolution().getAllPostponed());
            Main.getBestSolution().printSchedules();
        }
        System.out.println();
    }

    private static void run() {
        Random rn = new Random();
        int seed = rn.nextInt(Parameters.seedBound);
        Problem.setUpProblem(Constants.FILE_NAME, false, seed);
        if (Constants.SOLSTORM) Parameters.setSolstormParameters();
        Problem.setRandom(seed);
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
        Constants.FILE_NAME = "11-12-2-1.json";  // If running locally (will be overwritten on Solstorm)
        if (args.length > 0) Constants.setSolstormConstants(args[0], args[1]);
        Main.run();
    }
}
