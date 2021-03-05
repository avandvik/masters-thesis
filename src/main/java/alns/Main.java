package alns;

import data.Problem;
import objects.Order;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static List<Heuristic> destroyHeuristics;
    private static List<Heuristic> repairHeuristics;
    private static Solution currentSolution;
    private static Solution bestSolution;
    private static int iterationsCurrentSolution;
    private final static List<Integer> visitedSolutions = new ArrayList<>();

    public static void runALNS() {

        currentSolution = Construction.constructRandomInitialSolution();
        bestSolution = currentSolution;
        visitedSolutions.add(currentSolution.hashCode());

        int segmentIterations = 0;
        for (int iteration = 0; iteration < 100; iteration++) {
            segmentIterations++;

            List<Heuristic> heuristics = chooseHeuristics();

            // TODO: Apply heuristics to generate new candidate solution

            Solution candidateSolution = Construction.constructRandomInitialSolution();

            List<Double> rewards = acceptSolution(candidateSolution);

            updateScores(rewards, heuristics);

            if (segmentIterations < 10) continue;

            segmentIterations = 0;
            resetScores();
        }
    }

    private static List<Heuristic> chooseHeuristics() {
        Heuristic chosenDestroy = rouletteWheelSelection(destroyHeuristics);
        Heuristic chosenRepair = rouletteWheelSelection(repairHeuristics);
        chosenDestroy.incrementSelections();
        chosenRepair.incrementSelections();
        return new ArrayList<>(Arrays.asList(chosenDestroy, chosenRepair));
    }

    // TODO: Must be verified
    private static Heuristic rouletteWheelSelection(List<Heuristic> heuristics) {
        double weights = heuristics.stream().mapToDouble(Heuristic::getWeight).sum();
        List<Double> probabilities = heuristics.stream().map(o -> o.getWeight() / weights).collect(Collectors.toList());

        TreeMap<Double, Heuristic> rouletteWheel = new TreeMap<>();
        double aggregatedProbability = 0.0;
        for (int idx = 0; idx < heuristics.size(); idx++) {
            aggregatedProbability += probabilities.get(idx);
            rouletteWheel.put(aggregatedProbability, heuristics.get(idx));
        }

        return rouletteWheel.higherEntry(Math.random()).getValue();
    }

    // TODO: Parameterize rewards and max number of iterations for a currentSolution
    public static List<Double> acceptSolution(Solution candidateSolution) {
        List<Double> rewards = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0));
        boolean unacceptedSolution = !visitedSolutions.contains(candidateSolution.hashCode());
        if (candidateSolution.getFitness(false) < bestSolution.getFitness(false)) {
            bestSolution = candidateSolution;
            currentSolution = candidateSolution;
            visitedSolutions.add(candidateSolution.hashCode());
            iterationsCurrentSolution = 0;
            rewards.add(0, 33.0);
        } else if (simulatedAnnealing(currentSolution.getFitness(false), candidateSolution.getFitness(false))) {
            currentSolution = candidateSolution;
            visitedSolutions.add(candidateSolution.hashCode());
            iterationsCurrentSolution = 0;
            if (unacceptedSolution) {
                if (candidateSolution.getFitness(false) < currentSolution.getFitness(false)) {
                    rewards.add(1, 9.0);
                } else {
                    rewards.add(2, 9.0);
                }
            }
        } else if (iterationsCurrentSolution > 50) {
            currentSolution = candidateSolution;
        }
        return rewards;
    }

    // TODO: Parameterize temperature and implement cooling
    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Math.random() < Math.exp(-(candidateFitness - currentFitness) / 100);
    }

    private static void updateScores(List<Double> rewards, List<Heuristic> heuristics) {
        for (Heuristic heuristic : heuristics) {
            for (double reward : rewards) {
                heuristic.addToScore(reward);
            }
        }
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

    private static Solution createFeasibleSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < 3; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 3; i < Problem.getNumberOfOrders(); i++) orderSequences.get(1).add(Problem.getOrder(i));
        return new Solution(orderSequences);
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false);
    }
}
