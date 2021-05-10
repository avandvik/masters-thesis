package alns;

import alns.heuristics.*;
import alns.heuristics.protocols.Destroyer;
import alns.heuristics.protocols.Repairer;
import data.Constants;
import data.Messages;
import data.Parameters;
import data.Problem;
import localsearch.LocalSearch;
import objects.Order;
import setpartitioning.Data;
import setpartitioning.Model;
import subproblem.Cache;
import utils.IO;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final char[] animationChars = new char[]{'|', '/', '-', '\\'};

    private static List<Heuristic> destroyHeuristics;
    private static List<Heuristic> repairHeuristics;

    private static Solution currentSolution;
    private static Solution bestSolution;
    private static List<Integer> visitedSolutions;

    private static int iterationsCurrentSolution;
    private static double currentTemperature;

    public static Map<Integer, Map<List<Order>, Double>> vesselToSequenceToCost;

    public static void run() {
        initialize();
        for (int iter = 0; iter < Parameters.totalIter; iter++) {
            iterationsCurrentSolution++;
            List<Heuristic> heuristics = chooseHeuristics();
            Solution candidateSolution = applyHeuristics(currentSolution, heuristics);
            if (Parameters.localSearch) candidateSolution = LocalSearch.localSearch(candidateSolution);
            if (Parameters.setPartitioning) saveOrderSequences(candidateSolution);
            printIterationInfo(iter, candidateSolution);
            double reward = acceptSolution(candidateSolution);
            if (Parameters.setPartitioning && (iter + 1) % Parameters.setPartitioningIter == 0) runSetPartitioning();
            maintenance(reward, heuristics, iter);
        }
        if (Parameters.saveSolution) IO.saveSolution(bestSolution);
    }

    public static void initialize() {
        Data.initializeGurobiEnv();
        Cache.initialize();
        initializeHeuristics();
        initializeSolutionFields();
        initializeSimulatedAnnealing();
        initializeSequenceSaving();
    }

    private static void initializeHeuristics() {
        destroyHeuristics = new ArrayList<>();
        repairHeuristics = new ArrayList<>();
        destroyHeuristics.add(new RemovalRandom(Constants.REMOVAL_RANDOM_NAME));
        destroyHeuristics.add(new RemovalRelated(Constants.REMOVAL_RELATED_NAME));
        destroyHeuristics.add(new RemovalWorst(Constants.REMOVAL_WORST_NAME));
        destroyHeuristics.add(new RemovalCluster(Constants.REMOVAL_CLUSTER_NAME));
        destroyHeuristics.add(new RemovalSpread(Constants.REMOVAL_SPREAD_NAME));
        destroyHeuristics.add(new RemovalSpot(Constants.REMOVAL_SPOT_NAME));
        repairHeuristics.add(new InsertionGreedy(Constants.INSERTION_GREEDY_NAME));
        repairHeuristics.add(new InsertionRegret(Constants.INSERTION_REGRET_NAME));
        repairHeuristics.add(new InsertionMaxPenaltyCost(Constants.INSERTION_MAX_PENALTY_NAME));
        repairHeuristics.add(new InsertionMaxOrderSize(Constants.INSERTION_MAX_ORDER_SIZE_NAME));
        for (Heuristic heuristic : destroyHeuristics) heuristic.setWeight(Parameters.initialWeight);
        for (Heuristic heuristic : repairHeuristics) heuristic.setWeight(Parameters.initialWeight);
    }

    private static void initializeSolutionFields() {
        currentSolution = Construction.constructGreedyInitialSolution();
        bestSolution = currentSolution;
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
            vesselToSequenceToCost.put(vesselIdx, new HashMap<>());
        }
    }

    private static List<Heuristic> chooseHeuristics() {
        Heuristic chosenDestroyer = rouletteWheelSelection(destroyHeuristics);
        Heuristic chosenRepairer = rouletteWheelSelection(repairHeuristics);
        chosenDestroyer.incrementSelections();
        chosenRepairer.incrementSelections();
        return new ArrayList<>(Arrays.asList(chosenDestroyer, chosenRepairer));
    }

    // TODO: Must be verified when there are more heuristics to choose from
    private static Heuristic rouletteWheelSelection(List<Heuristic> heuristics) {
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

    public static Solution applyHeuristics(Solution solution, List<Heuristic> heuristics) {
        Destroyer destroyer = (Destroyer) heuristics.get(0);
        Solution partialSolution = destroyer.destroy(solution, Parameters.nbrOrdersRemove);
        Repairer repairer = (Repairer) heuristics.get(1);
        Solution candidateSolution = repairer.repair(partialSolution);
        if (!Evaluator.isSolutionFeasible(candidateSolution)) {
            /*
            System.out.println(candidateSolution);
            System.out.println("Load: " + Evaluator.isFeasibleLoad(candidateSolution.getOrderSequences()));
            System.out.println("Duration: " + Evaluator.isFeasibleDuration(candidateSolution.getOrderSequences()));
            System.out.println("Visits: " + Evaluator.isFeasibleVisits(candidateSolution.getOrderSequences()));
            System.out.println("Completeness: " + Evaluator.isSolutionComplete(candidateSolution));
            System.out.println("Each order occurs once: " + Evaluator.eachOrderOccursOnce(candidateSolution));
            System.out.println("Heuristics used: " + heuristics);
             */
            throw new IllegalStateException(Messages.solInfeasible);
        }
        return candidateSolution;
    }

    private static void saveOrderSequences(Solution candidateSolution) {
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            List<Order> orderSequence = candidateSolution.getOrderSequence(vIdx);
            double cost = Objective.getOrderSequenceCost(orderSequence, vIdx);
            vesselToSequenceToCost.get(vIdx).put(orderSequence, cost);  // Okay if overwrite
        }
    }

    public static Double acceptSolution(Solution candidateSolution) {
        if (candidateSolution.equals(currentSolution)) {
            if (iterationsCurrentSolution > Parameters.maxIterSolution) {
                currentSolution = Construction.constructGreedyInitialSolution();
                iterationsCurrentSolution = 0;
            }
        } else if (candidateSolution.getObjective(false) < bestSolution.getObjective(false)) {
            return doGlobalBestUpdates(candidateSolution);
        } else if (simulatedAnnealing(currentSolution.getObjective(false), candidateSolution.getObjective(false))) {
            return doLocalUpdates(candidateSolution);
        }
        return 0.0;
    }

    private static double doGlobalBestUpdates(Solution candidateSolution) {
        bestSolution = candidateSolution;
        currentSolution = candidateSolution;
        visitedSolutions.add(candidateSolution.hashCode());
        iterationsCurrentSolution = 0;
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

    private static void runSetPartitioning() {
        Model model = new Model();
        model.run();
        Solution candidateSolution = model.getNewSolution();
        acceptSolution(candidateSolution);  // Reward is ignored
    }

    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Problem.random.nextDouble() < Math.exp(-(candidateFitness - currentFitness) / currentTemperature);
    }

    private static void maintenance(double reward, List<Heuristic> heuristics, int iteration) {
        currentTemperature *= Parameters.coolingRate;
        for (Heuristic heuristic : heuristics) heuristic.addToScore(reward);
        if ((iteration + 1) % Parameters.segmentIter == 0) resetHeuristicScores();
        Cache.cacheLongTerm();
    }

    private static void resetHeuristicScores() {
        for (Heuristic heuristic : destroyHeuristics) heuristic.resetScoreAndUpdateWeight();
        for (Heuristic heuristic : repairHeuristics) heuristic.resetScoreAndUpdateWeight();
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
            printSubtle(iteration, candidateSolution);
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

    private static void printSubtle(int iteration, Solution candidateSolution) {
        int percentage = (int) (((iteration + 1) / (double) Parameters.totalIter) * 100);
        System.out.print("Processing: " + percentage + "% " + animationChars[iteration % 4] + "\r");
    }

    private static void runExtensively(String fileName, int numberOfSeeds, int seedBound) {
        Random rn = new Random(seedBound);
        int seed = rn.nextInt(seedBound);
        Problem.setUpProblem(fileName, false, seed);
        for (int i = 0; i < numberOfSeeds; i++) {
            System.out.println("Running with seed: " + seed);

            double startTime = System.nanoTime();
            Main.run();
            printSolutionInfo(startTime);

            seed = rn.nextInt(seedBound);
            Problem.setRandom(seed);
        }
    }

    private static void runSimple(String fileName) {
        Problem.setUpProblem(fileName, false, 4);
        double startTime = System.nanoTime();
        Main.run();
        printSolutionInfo(startTime);
    }

    private static void printSolutionInfo(double startTime) {
        if (Parameters.semiVerbose) {
            double timeElapsed = (System.nanoTime() - startTime) / 1e9;
            System.out.println("Best objective: " + Main.getBestSolution().getObjective(false));
            System.out.println("\tFuel costs: " + Main.getBestSolution().getFuelCosts());
            System.out.println("\tPenalty costs: " + Main.getBestSolution().getPenaltyCosts());
            System.out.println("Time elapsed: " + timeElapsed);
            System.out.println("Postponed orders: " + Main.getBestSolution().getAllPostponed());
            Main.getBestSolution().printSchedules();
        }
    }

    public static void main(String[] args) {
        File[] instances;
        if (args.length > 0) Constants.overwritePathsSolstorm(args[0]);
        instances = new File(Constants.PATH_TO_INSTANCES).listFiles();
        if (instances == null) throw new IllegalStateException("No instances to run!");
        for (File instance : instances) {
            String fileName = instance.getName();
            System.out.println("Running " + fileName);
            // runExtensively(fileName, 10, 1000);
            runSimple(fileName);
        }
    }
}
