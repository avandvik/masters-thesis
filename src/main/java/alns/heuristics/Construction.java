package alns.heuristics;

import alns.Evaluator;
import alns.Solution;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class Construction {

    private final static Random random = new Random();

    public static Map<Integer, List<Integer>> getAllFeasibleInsertions(List<List<Order>> orderSequences, Order order) {
        List<List<Order>> orderSequencesCopy = Helpers.deepCopy2DList(orderSequences);
        Map<Integer, List<Integer>> insertionIndices = new HashMap<>();
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            List<Integer> vesselIndices = new ArrayList<>();
            List<Order> orderSequence = orderSequencesCopy.get(vesselNumber);
            for (int orderIdx = 0; orderIdx <= orderSequence.size(); orderIdx++) {
                List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                orderSequenceCopy.add(orderIdx, order);
                List<List<Order>> solutionOrderSequences = Helpers.deepCopy2DList(orderSequencesCopy);
                solutionOrderSequences.set(vesselNumber, orderSequenceCopy);
                if (Evaluator.isOrderSequencesFeasible(solutionOrderSequences)) vesselIndices.add(orderIdx);
            }
            insertionIndices.put(vesselNumber, vesselIndices);
        }
        return insertionIndices;
    }

    public static Solution constructRandomInitialSolution() {
        List<Order> ordersToPlace = Helpers.deepCopyList(Problem.orders, false);
        List<List<Order>> orderSequences = Helpers.createEmptyOrderSequences();
        Set<Order> postponedOrders = new HashSet<>();

        while (!ordersToPlace.isEmpty()) {
            Order orderToPlace = ordersToPlace.remove(0);
            Map<Integer, List<Integer>> feasibleInsertions = getAllFeasibleInsertions(orderSequences, orderToPlace);

            List<Integer> vesselNumbers = new ArrayList<>(feasibleInsertions.keySet());
            Collections.shuffle(vesselNumbers.subList(0, vesselNumbers.size() - 1));

            // Attempt placing order in fleet vessels, then spot vessel
            boolean orderPlaced = false;
            for (int vesselNumber : vesselNumbers) {
                List<Integer> vesselInsertions = feasibleInsertions.get(vesselNumber);
                if (vesselInsertions.isEmpty()) continue;
                int orderIdx = vesselInsertions.get(random.nextInt(vesselInsertions.size()));
                orderSequences.get(vesselNumber).add(orderIdx, orderToPlace);
                orderPlaced = true;
                break;
            }

            // If there are no feasible insertions in any vessels, place in postponed orders
            if (!orderPlaced) postponedOrders.add(orderToPlace);
        }
        Solution initialSolution = new Solution(orderSequences, postponedOrders, true);
        if (!Evaluator.isSolutionFeasible(initialSolution)) System.out.println("Initial solution is infeasible!");
        return initialSolution;
    }
}
