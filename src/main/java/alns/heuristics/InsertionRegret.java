package alns.heuristics;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;
import subproblem.SubProblemInsertion;
import utils.Helpers;

import java.util.*;

public class InsertionRegret extends Heuristic implements Repairer {

    public InsertionRegret(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        while (!solution.getUnplacedOrders().isEmpty()) solution = getRegretSolution(solution);
        Objective.setObjValAndSchedule(solution);
        if (!Evaluator.isSolutionFeasible(solution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return solution;
    }

    public Solution getRegretSolution(Solution partialSolution) {
        /* Finds and inserts the order in partialSolution's unplaced orders with the highest regret value */

        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> unplacedOrders = newSolution.getUnplacedOrders();
        Map<Order, Double> orderToRegret;
        if (Parameters.parallelHeuristics) {
            Objective.runMultipleSPInsertion(orderSequences, unplacedOrders);
            Objective.runMultipleSPEvaluate(orderSequences);
            orderToRegret = calculateRegretsPar(unplacedOrders);
        } else {
            orderToRegret = calculateRegretsSeq(orderSequences, unplacedOrders);
        }

        Order orderMaxRegret = Collections.max(orderToRegret.entrySet(), Map.Entry.comparingByValue()).getKey();
        return InsertionGreedy.insertGreedilyInSolution(newSolution, orderMaxRegret);
    }

    private Map<Order, Double> calculateRegretsPar(Set<Order> unplacedOrders) {
        Map<Order, Double> orderToRegret = new HashMap<>();
        for (Order order : unplacedOrders) {
            List<Double> increases = new ArrayList<>();
            Map<List<Integer>, Double> insertionToObj = SubProblemInsertion.orderToInsertionToObjective.get(order);
            for (List<Integer> insertion : insertionToObj.keySet()) {
                int vesselIdx = insertion.get(0);
                double increase = insertionToObj.get(insertion) - SubProblem.vesselToObjective.get(vesselIdx);
                increases.add(increase);
            }
            if (!order.isMandatory()) increases.add(order.getPostponementPenalty());
            orderToRegret.put(order, calculateRegret(increases));
        }
        return orderToRegret;
    }

    private Map<Order, Double> calculateRegretsSeq(List<List<Order>> orderSequences, Set<Order> unplacedOrders) {
        Map<Order, Double> orderToRegret = new HashMap<>();
        for (Order order : unplacedOrders) {
            List<Double> increases = findIncreasesOrderSequences(orderSequences, order);
            if (!order.isMandatory()) increases.add(order.getPostponementPenalty());
            orderToRegret.put(order, calculateRegret(increases));
        }
        return orderToRegret;
    }

    private List<Double> findIncreasesOrderSequences(List<List<Order>> orderSequences, Order order) {
        List<Double> increases = new ArrayList<>();
        Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = orderSequences.get(vesselIdx);
            double currentObjective = Objective.runSPLean(orderSequence, vesselIdx);
            for (int insertionIdx : insertions.get(vesselIdx)) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.add(insertionIdx, order);
                double increase = Objective.runSPLean(orderSequenceCopy, vesselIdx) - currentObjective;
                increases.add(increase);
            }
        }
        return increases;
    }

    private double calculateRegret(List<Double> increases) {
        double regret = 0.0;
        Collections.sort(increases);
        for (int i = 0; i < Math.min(Parameters.regretParameter, increases.size()); i++)
            regret += increases.get(i) - increases.get(0);
        return regret;
    }
}
