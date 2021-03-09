package alns;

import alns.heuristics.Construction;
import alns.heuristics.Heuristic;
import alns.heuristics.protocols.Destroyer;
import alns.heuristics.InsertionGreedy;
import alns.heuristics.RemovalRandom;
import alns.heuristics.protocols.Repairer;
import data.Constants;
import data.Parameters;
import data.Problem;
import objects.Order;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static List<Heuristic> destroyHeuristics;
    private static List<Heuristic> repairHeuristics;

    private static Solution currentSolution;
    private static Solution bestSolution;
    private final static List<Integer> visitedSolutions = new ArrayList<>();

    private static int iterationsCurrentSolution;
    private static double currentTemperature;

    public static void run() {

        initialize();

        int segmentIterations = 0;
        for (int iteration = 0; iteration < Parameters.totalIterations; iteration++) {
            segmentIterations++;
            iterationsCurrentSolution++;

            List<Heuristic> heuristics = chooseHeuristics();
            Solution candidateSolution = applyHeuristics(currentSolution, heuristics);

            if (Parameters.verbose) printIterationInfo(iteration, candidateSolution);

            double reward = acceptSolution(candidateSolution);

            currentTemperature *= Parameters.coolingRate;
            updateScores(reward, heuristics);
            if (segmentIterations < 10) continue;
            segmentIterations = 0;
            resetScores();
        }
    }

    private static void initialize() {
        // Initialize heuristics
        destroyHeuristics = new ArrayList<>();
        repairHeuristics = new ArrayList<>();
        destroyHeuristics.add(new RemovalRandom("random removal", true, false));
        repairHeuristics.add(new InsertionGreedy("greedy insertion", false, true));

        // Initialize solution fields
        currentSolution = Construction.constructRandomInitialSolution();
        bestSolution = currentSolution;
        visitedSolutions.add(currentSolution.hashCode());
        iterationsCurrentSolution = 0;

        // Initialize simulated annealing
        Parameters.setTemperatureAndCooling(currentSolution.getFitness(false));
        currentTemperature = Parameters.startTemperature;
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
        Solution partialSolution = destroyer.destroy(solution, 2);  // No need to evaluate

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
            if (candidateSolution.getFitness(false) < currentSolution.getFitness(false)) {
                return Parameters.newLocalImprovement;
            } else {
                return Parameters.newLocal;
            }
        }
        return 0.0;  // No reward if solution has been visited before, but current solution is updated
    }

    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Problem.random.nextDouble() < Math.exp(-(candidateFitness - currentFitness) / currentTemperature);
    }

    private static void updateScores(double reward, List<Heuristic> heuristics) {
        for (Heuristic heuristic : heuristics) heuristic.addToScore(reward);
    }

    private static void resetScores() {
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

    public static double getCurrentTemperature() {
        return currentTemperature;
    }

    public static void setCurrentTemperature(double temperature) {
        currentTemperature = temperature;
    }

    private static void printIterationInfo(int iteration, Solution candidateSolution) {
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

    private static Solution createFeasibleSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < 3; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 3; i < Problem.getNumberOfOrders(); i++) orderSequences.get(1).add(Problem.getOrder(i));
        Set<Order> postponedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, false);
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false, 10);
        run();
    }
}
