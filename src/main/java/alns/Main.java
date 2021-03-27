package alns;

import alns.heuristics.*;
import alns.heuristics.protocols.Destroyer;
import alns.heuristics.protocols.Repairer;
import data.Constants;
import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import setpartitioning.Data;
import setpartitioning.Model;
import subproblem.SubProblem;
import utils.IO;

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
        for (int iteration = 0; iteration < Parameters.totalIterations; iteration++) {
            iterationsCurrentSolution++;
            List<Heuristic> heuristics = chooseHeuristics();
            Solution candidateSolution = applyHeuristics(currentSolution, heuristics);
            // TODO: Local search
            printIterationInfo(iteration, candidateSolution);
            double reward = acceptSolution(candidateSolution);
            if (iteration > 0 && iteration % Parameters.setPartitioningIterations == 0) runSetPartitioningModel();
            maintenance(reward, heuristics, iteration);
        }
        if (Parameters.saveSolution) IO.saveSolution(bestSolution);
    }

    private static void initialize() {
        Data.initializeGurobiEnv();
        SubProblem.initializeCache();
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
        repairHeuristics.add(new InsertionGreedy(Constants.INSERTION_GREEDY_NAME));
        repairHeuristics.add(new InsertionRegret(Constants.INSERTION_REGRET_NAME));
        for (Heuristic heuristic : destroyHeuristics) heuristic.setWeight(Parameters.initialWeight);
        for (Heuristic heuristic : repairHeuristics) heuristic.setWeight(Parameters.initialWeight);
    }

    private static void initializeSolutionFields() {
        currentSolution = Construction.constructRandomInitialSolution();
        bestSolution = currentSolution;
        visitedSolutions = new ArrayList<>();
        visitedSolutions.add(currentSolution.hashCode());
        iterationsCurrentSolution = 0;
    }

    private static void initializeSimulatedAnnealing() {
        Parameters.setTemperatureAndCooling(currentSolution.getFitness(false));
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
        Solution partialSolution = destroyer.destroy(solution, Parameters.nbrOrdersRemove);  // No need to evaluate

        if (!Evaluator.isPartFeasible(partialSolution)) throw new IllegalStateException(Messages.solutionInfeasible);

        Repairer repairer = (Repairer) heuristics.get(1);
        return repairer.repair(partialSolution);
    }

    public static Double acceptSolution(Solution candidateSolution) {
        if (candidateSolution.equals(currentSolution)) {
            if (iterationsCurrentSolution > Parameters.maxIterSolution) {
                currentSolution = Construction.constructRandomInitialSolution();
                iterationsCurrentSolution = 0;
            }
        } else if (candidateSolution.getFitness(false) < bestSolution.getFitness(false)) {
            return doGlobalBestUpdates(candidateSolution);
        } else if (simulatedAnnealing(currentSolution.getFitness(false), candidateSolution.getFitness(false))) {
            return doLocalUpdates(candidateSolution);
        }
        return 0.0;
    }

    private static double doGlobalBestUpdates(Solution candidateSolution) {
        saveVoyages(candidateSolution);
        bestSolution = candidateSolution;
        currentSolution = candidateSolution;
        visitedSolutions.add(candidateSolution.hashCode());
        iterationsCurrentSolution = 0;
        return Parameters.newGlobalBest;
    }

    private static double doLocalUpdates(Solution candidateSolution) {
        saveVoyages(candidateSolution);
        currentSolution = candidateSolution;
        iterationsCurrentSolution = 0;
        if (!visitedSolutions.contains(candidateSolution.hashCode())) {
            visitedSolutions.add(candidateSolution.hashCode());
            if (candidateSolution.getFitness(false) < currentSolution.getFitness(false)) {
                return Parameters.newLocalImprovement;
            } else {
                return Parameters.newLocal;
            }
        }
        return 0.0;  // No reward if solution has been visited before, but current solution is updated
    }

    private static void runSetPartitioningModel() {
        Model model = new Model();
        model.run();
        Solution candidateSolution = model.getNewSolution();
        acceptSolution(candidateSolution);  // Reward is ignored
    }

    private static void saveVoyages(Solution solution) {
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {

            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            boolean isSpotVessel = Problem.isSpotVessel(vesselIdx);
            int hash = Objects.hash(orderSequence, isSpotVessel);
            double cost = orderSequence.isEmpty() ? 0.0 : SubProblem.hashToCost.get(hash);

            vesselToSequenceToCost.get(vesselIdx).put(orderSequence, cost);  // Okay if overwrite
        }
    }

    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Problem.random.nextDouble() < Math.exp(-(candidateFitness - currentFitness) / currentTemperature);
    }

    private static void maintenance(double reward, List<Heuristic> heuristics, int iteration) {
        currentTemperature *= Parameters.coolingRate;
        for (Heuristic heuristic : heuristics) heuristic.addToScore(reward);
        if ((iteration + 1) % Parameters.segmentIterations == 0) resetHeuristicScores();
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
                + "Fitness: " + bestSolution.getFitness(false)
                + "\nHash: " + bestSolution.hashCode() + Constants.ANSI_RESET);
        System.out.println();
        System.out.println(Constants.ANSI_BLUE + "Current solution \n" + currentSolution
                + "\nFitness: " + currentSolution.getFitness(false)
                + "\nHash: " + currentSolution.hashCode());
        System.out.println("Iterations with current solution: " + iterationsCurrentSolution + Constants.ANSI_RESET);
        System.out.println();
        System.out.println(Constants.ANSI_YELLOW + "Candidate solution \n" + candidateSolution
                + "\nFitness: " + candidateSolution.getFitness(false)
                + "\nHash: " + candidateSolution.hashCode());
        System.out.println("Equal solutions: " + candidateSolution.equals(currentSolution) + Constants.ANSI_RESET);
        System.out.println();
    }

    private static void printSubtle(int iteration, Solution candidateSolution) {
        int percentage = (int) (((iteration + 1) / (double) Parameters.totalIterations) * 100);
        System.out.print("Processing: " + percentage + "% " + animationChars[iteration % 4] + "\r");
    }

    private static void runExtensively(int numberOfSeeds, int seedBound) {
        Random rn = new Random(seedBound);
        int seed = rn.nextInt(seedBound);
        Problem.setUpProblem("example_10.json", false, seed);
        for (int i = 0; i < numberOfSeeds; i++) {
            System.out.println("Running with seed: " + seed);

            double startTime = System.nanoTime();
            Main.run();
            double timeElapsed = (System.nanoTime() - startTime) / 1e9;

            System.out.println("Best objective: " + Main.getBestSolution().getFitness(false));
            System.out.println("Time elapsed: " + timeElapsed + "\n");

            seed = rn.nextInt(seedBound);
            Problem.setRandom(seed);
        }
    }

    private static void runSimple() {
        Problem.setUpProblem("example_10.json", false, 10);
        Main.run();
        System.out.println("Best fitness: " + Main.getBestSolution().getFitness(false));
        Main.getBestSolution().printSchedules();
    }

    public static void main(String[] args) {
        // runExtensively(20, 1000);
        if (args.length == 1 && args[0].equals("solstorm")) {
            Constants.OUTPUT_PATH = "dummy";
        } else {
            Constants.OUTPUT_PATH = Constants.LOCAL_OUTPUT_PATH;
        }
        runSimple();
    }
}
