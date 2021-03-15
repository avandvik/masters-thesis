package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Messages;
import data.Problem;
import objects.Order;
import subproblem.SubProblem;
import utils.Helpers;

import java.util.*;

public class InsertionGreedy extends Heuristic implements Repairer {

    public InsertionGreedy(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    public static void greedyVol2(Solution partialSolution, Set<Order> ordersToPlace) {

        Solution solutionCopy = Helpers.deepCopySolution(partialSolution);
        List<List<Order>> orderSequences = solutionCopy.getOrderSequences();

        SubProblem.initializeParallelRuns();
        List<Thread> threads = new ArrayList<>();

        for (Order order : ordersToPlace) {
            Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, order);
            for (int vesselIdx : insertions.keySet()) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequences.get(vesselIdx), true);
                for (int insertionIdx : insertions.get(vesselIdx)) {
                    orderSequenceCopy.add(insertionIdx, order);
                    Thread thread = new Thread(new SubProblem(orderSequenceCopy, vesselIdx, insertionIdx, order));
                    threads.add(thread);
                    thread.start();
                }
            }
        }

        Map<Integer, Double> vesselToObjective = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            vesselToObjective.put(vesselIdx, Objective.runSPLean(orderSequences.get(vesselIdx), vesselIdx));
        }

        collect(threads);

        System.out.println(SubProblem.sharedObjectiveValues);

        double leastIncrease = Double.POSITIVE_INFINITY;
        List<Integer> bestInsertion = null;
        Order bestOrder = null;
        for (Order order : ordersToPlace) {
            Map<List<Integer>, Double> insertionToObjective = SubProblem.sharedObjectiveValues.get(order);
            List<Integer> minInsertion = getMinInsertion(insertionToObjective);
            double increase = insertionToObjective.get(minInsertion) - vesselToObjective.get(minInsertion.get(0));
            if (increase < leastIncrease) {
                leastIncrease = increase;
                bestInsertion = minInsertion;
                bestOrder = order;
            }
        }

        System.out.println(leastIncrease);
        System.out.println(bestInsertion);
        System.out.println(bestOrder);
    }

    private static List<Integer> getMinInsertion(Map<List<Integer>, Double> map) {
        return Collections.min(map.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
    }

    private static void collect(List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static Solution getGreedyInsertion(Solution partialSolution, Order orderToPlace) {
        /* Inserts order in an available vessel, a spot vessel, or the set of postponed orders */

        List<List<Order>> orderSequences = Helpers.deepCopy2DList(partialSolution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(partialSolution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(partialSolution.getUnplacedOrders());
        Map<Integer, List<Integer>> insertions = Construction.getAllFeasibleInsertions(orderSequences, orderToPlace);
        double leastIncrease = Double.POSITIVE_INFINITY;
        List<Integer> bestInsertion = null;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Order> orderSequence = orderSequences.get(vesselNumber);
            double currentObj = Objective.runSPLean(orderSequence, vesselNumber);
            for (int insertionIdx : insertions.get(vesselNumber)) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.add(insertionIdx, orderToPlace);
                double increase = Objective.runSPLean(orderSequenceCopy, vesselNumber) - currentObj;
                if (increase < leastIncrease) {
                    leastIncrease = increase;
                    bestInsertion = new ArrayList<>(Arrays.asList(vesselNumber, insertionIdx));
                }
            }
        }
        double increase = orderToPlace.getPostponementPenalty();
        if (!orderToPlace.isMandatory() && (increase < leastIncrease || bestInsertion == null)) {
            postponedOrders.add(orderToPlace);
            unplacedOrders.remove(orderToPlace);
            return new Solution(orderSequences, postponedOrders, unplacedOrders);
        }

        if (bestInsertion == null) throw new IllegalStateException(Messages.cannotPlaceMDOrder);

        orderSequences.get(bestInsertion.get(0)).add(bestInsertion.get(1), orderToPlace);
        unplacedOrders.remove(orderToPlace);

        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        List<Order> sortedUnplacedOrders = Helpers.sortUnplacedOrders(partialSolution.getUnplacedOrders());
        Solution solution = partialSolution;
        for (Order order : sortedUnplacedOrders) solution = getGreedyInsertion(solution, order);
        return new Solution(solution.getOrderSequences(), solution.getPostponedOrders(), true);
    }
}
