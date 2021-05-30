package alns.heuristics;

import alns.Evaluator;
import alns.Solution;
import data.Constants;
import data.Problem;
import data.SearchHistory;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class Construction {

    public static Map<Integer, List<Integer>> getAllFeasibleInsertions(List<List<Order>> orderSequences, Order order) {
        List<List<Order>> orderSequencesCopy = Helpers.deepCopy2DList(orderSequences);
        Map<Integer, List<Integer>> insertionIndices = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Integer> vesselIndices = new ArrayList<>();
            List<Order> orderSequence = orderSequencesCopy.get(vesselIdx);
            for (int insertionIdx = 0; insertionIdx <= orderSequence.size(); insertionIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.add(insertionIdx, order);
                List<List<Order>> solutionOrderSequences = Helpers.deepCopy2DList(orderSequencesCopy);
                solutionOrderSequences.set(vesselIdx, orderSequenceCopy);
                if (Evaluator.isOrderSequencesFeasible(solutionOrderSequences)) vesselIndices.add(insertionIdx);
            }
            insertionIndices.put(vesselIdx, vesselIndices);
        }
        return insertionIndices;
    }

    public static Solution constructGreedyInitialSolution() {
        InsertionGreedy insertionGreedy = new InsertionGreedy(Constants.INSERTION_GREEDY_NAME);
        List<List<Order>> orderSequences = Helpers.createEmptyOrderSequences();
        Set<Order> postponedOrders = new HashSet<>();
        Set<Order> unplacedOrders = new HashSet<>(Problem.orders);
        Solution emptySolution = new Solution(orderSequences, postponedOrders, unplacedOrders);
        Solution initialSolution = insertionGreedy.repair(emptySolution);
        SearchHistory.setConstructionHeuristicObjective(initialSolution.getObjective(false));
        SearchHistory.setBestSolFoundBy(Constants.CONSTRUCTION_NAME);
        return initialSolution;
    }

    public static Solution constructRandomInitialSolution() {
        List<Order> ordersToPlace = sortUnplacedOrders(Problem.orders);
        List<List<Order>> orderSequences = Helpers.createEmptyOrderSequences();
        Set<Order> postponedOrders = new HashSet<>();
        while (!ordersToPlace.isEmpty()) {
            Order orderToPlace = ordersToPlace.remove(0);
            Map<Integer, List<Integer>> feasibleInsertions = getAllFeasibleInsertions(orderSequences, orderToPlace);
            List<Integer> vesselNumbers = new ArrayList<>(feasibleInsertions.keySet());
            Collections.shuffle(vesselNumbers.subList(0, vesselNumbers.size() - 1), Problem.random);
            boolean orderPlaced = false;
            for (int vesselNumber : vesselNumbers) {
                List<Integer> vesselInsertions = feasibleInsertions.get(vesselNumber);
                if (vesselInsertions.isEmpty()) continue;
                int orderIdx = vesselInsertions.get(Problem.random.nextInt(vesselInsertions.size()));
                orderSequences.get(vesselNumber).add(orderIdx, orderToPlace);
                orderPlaced = true;
                break;
            }
            if (!orderPlaced && !orderToPlace.isMandatory()) postponedOrders.add(orderToPlace);
        }
        return new Solution(orderSequences, postponedOrders, true);
    }

    public static List<Order> sortUnplacedOrders(List<Order> unplacedOrders) {
        List<Order> sortedUnplacedOrders = new ArrayList<>();
        int numberOfMand = 0;
        for (Order order : unplacedOrders) {
            if (order.isMandatory()) {
                sortedUnplacedOrders.add(0, order);
                numberOfMand++;
            } else {
                sortedUnplacedOrders.add(order);
            }
        }
        Collections.shuffle(sortedUnplacedOrders.subList(0, numberOfMand), Problem.random);
        Collections.shuffle(sortedUnplacedOrders.subList(numberOfMand, sortedUnplacedOrders.size()), Problem.random);
        return sortedUnplacedOrders;
    }
}
