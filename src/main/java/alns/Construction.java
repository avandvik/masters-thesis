package alns;

import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class Construction {

    private static Random random = new Random();

    public static List<Solution> getAllFeasibleInsertions(Solution partialSolution, Order order) {
        /* Returns the solutions resulting from inserting the order in any order sequence or the postponement set */
        List<Solution> solutions = new ArrayList<>();

        // Copy orderSequences of original solution
        List<List<Order>> originalOrderSequences = new ArrayList<>();
        for (List<Order> orderSequence : partialSolution.getOrderSequences()) {
            originalOrderSequences.add(new LinkedList<>(orderSequence));
        }

        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            for (int i = 0; i <= originalOrderSequences.get(vesselNumber).size(); i++) {
                List<Order> orderSequence = new LinkedList<>(originalOrderSequences.get(vesselNumber));
                orderSequence.add(i, order);
                List<List<Order>> orderSequences = new ArrayList<>();
                for (int j = 0; j < Problem.getNumberOfVessels(); j++) {
                    if (j == vesselNumber) {
                        orderSequences.add(orderSequence);
                        continue;
                    }
                    orderSequences.add(j, new LinkedList<>(originalOrderSequences.get(j)));
                }
                Solution newSolution = new Solution(orderSequences);
                if (Evaluator.isPartiallyFeasible(newSolution)) solutions.add(newSolution);
            }
        }

        // Add the option of postponing the order
        Set<Order> postponed = new HashSet<>(Collections.singletonList(order));
        solutions.add(new Solution(partialSolution.getOrderSequences(), postponed));

        return solutions;
    }

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
                if (Evaluator.isPartiallyFeasible(new Solution(solutionOrderSequences))) vesselIndices.add(orderIdx);
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
        if (!Evaluator.isFeasible(initialSolution)) System.out.println("Initial solution is infeasible!");;
        return initialSolution;
    }
}
