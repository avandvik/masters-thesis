package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Parameters;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;
import subproblem.SubProblemInsertion;
import utils.Helpers;

import java.util.*;

public class InsertionRegret extends Heuristic implements Repairer {

    public InsertionRegret(String name) {
        super(name);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        while (!solution.getUnplacedOrders().isEmpty()) solution = getRegretSolution(solution);
        Objective.setObjValAndSchedule(solution);
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
        Order orderMaxRegret = getMaxRegretOrder(orderToRegret, unplacedOrders);
        return InsertionGreedy.insertGreedilyInSolution(newSolution, orderMaxRegret);
    }

    private Map<Order, Double> calculateRegretsPar(Set<Order> unplacedOrders) {
        Map<Order, Double> orderToRegret = new HashMap<>();
        for (Order order : unplacedOrders) {
            List<Double> increases = new ArrayList<>();
            if (!order.isMandatory()) increases.add(order.getPostponementPenalty());
            Map<List<Integer>, Double> insertionToObj = SubProblemInsertion.orderToInsertionToObjective.get(order);
            if (insertionToObj == null) {
                orderToRegret.put(order, calculateRegret(increases));
                continue;
            }
            for (Map.Entry<List<Integer>, Double> entry : insertionToObj.entrySet()) {
                List<Integer> insertion = entry.getKey();
                double obj = entry.getValue();
                obj += Helpers.getRandomDouble(-Parameters.maxNoise, Parameters.maxNoise);
                double increase = obj - SubProblem.vesselToObjective.get(insertion.get(0));
                increases.add(increase);
            }
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
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            List<Order> orderSequence = orderSequences.get(vIdx);
            double currentObj = Objective.runSP(orderSequence, vIdx);
            for (int insertionIdx : insertions.get(vIdx)) {
                double increase = calculateIncrease(orderSequence, order, vIdx, insertionIdx, currentObj);
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

    private Order getMaxRegretOrder(Map<Order, Double> orderToRegret, Set<Order> unplacedOrders) {
        Order orderMaxRegret = Collections.max(orderToRegret.entrySet(), Map.Entry.comparingByValue()).getKey();
        orderToRegret.remove(orderMaxRegret);
        while (instHasMandUnplacedOrder(orderMaxRegret, unplacedOrders)) {
            orderMaxRegret = Collections.max(orderToRegret.entrySet(), Map.Entry.comparingByValue()).getKey();
            orderToRegret.remove(orderMaxRegret);
        }
        return orderMaxRegret;
    }
}
