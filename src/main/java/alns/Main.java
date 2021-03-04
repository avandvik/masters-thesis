package alns;

import data.Problem;
import objects.Order;
import subproblem.SubProblem;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static List<Heuristic> destroyHeuristics;
    private static List<Heuristic> repairHeuristics;
    private static Solution currentSolution;
    private static Solution bestSolution;
    private static int iterationsCurrentSolution;

    public static void runALNS() {

        // TODO: Create initial solution by calling construction heuristic
        

        int segmentIterations = 0;

        for (int iteration = 0; iteration < 100; iteration++) {
            segmentIterations++;

            List<Heuristic> heuristics = chooseHeuristics();

            // TODO: Apply heuristics to generate new candidate solution

            Solution candidateSolution = createFeasibleSolution();

            List<Double> rewards = acceptSolution(candidateSolution);

            if (segmentIterations == 10) {
                segmentIterations = 0;
                resetScores();
            } else {
                updateScores(rewards, heuristics);
            }
        }
    }

    private static List<Heuristic> chooseHeuristics() {
        Heuristic chosenDestroy = rouletteWheelSelection(destroyHeuristics);
        Heuristic chosenRepair = rouletteWheelSelection(repairHeuristics);
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

    // TODO: Parameterize rewards and research whether we need to remember all previously accepted solutions
    private static List<Double> acceptSolution(Solution candidateSolution) {
        List<Double> rewards = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0));
        if (candidateSolution.getFitness() < bestSolution.getFitness()) {
            bestSolution = candidateSolution;
            currentSolution = candidateSolution;
            rewards.add(0, 1.0);  // Candidate is better than global best (global improvement)
        } else if (candidateSolution.getFitness() < currentSolution.getFitness()) {
            rewards.add(1, 1.0);  // Candidate is better than current (local improvement)
            if (simulatedAnnealing(currentSolution.getFitness(), candidateSolution.getFitness())) {
                currentSolution = candidateSolution;
                if (candidateSolution.getFitness() > currentSolution.getFitness()) {
                    rewards.add(2, 1.0);  // Candidate is accepted when worse than current
                }
            }
        } else if (iterationsCurrentSolution > 50) {  // TODO: Parameterize max number of iterations with current sol
            currentSolution = candidateSolution;
        }
        return rewards;
    }

    // TODO: Parameterize temperature and implement cooling
    private static boolean simulatedAnnealing(double currentFitness, double candidateFitness) {
        return Math.random() < Math.exp(-(currentFitness - candidateFitness) / 10);
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
        for (Heuristic heuristic: repairHeuristics) heuristic.resetScoreAndUpdateWeight();
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
        Solution solution = createFeasibleSolution();
        SubProblem.runSubProblem(solution);
    }
}
