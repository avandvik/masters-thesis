package alns;

import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Construction {

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
                if (Evaluator.isFeasible(newSolution)) solutions.add(newSolution);
            }
        }

        // Add the option of postponing the order
        Set<Order> postponed = new HashSet<>(Collections.singletonList(order));
        solutions.add(new Solution(partialSolution.getOrderSequences(), postponed));

        return solutions;
    }

    public static List<List<Integer>> getAllFeasibleInsertions(List<List<Order>> orderSequences, Order order) {
        // Copy orderSequences of original solution
        List<List<Order>> orderSequencesCopy = new ArrayList<>();
        for (List<Order> orderSequence : orderSequences) orderSequencesCopy.add(new LinkedList<>(orderSequence));

        List<List<Integer>> insertionIndices = new ArrayList<>();
        for (List<Order> orderSequence : orderSequencesCopy) {
            List<Integer> vesselIndices = new ArrayList<>();
            for (int i = 0; i <= orderSequence.size(); i++) {
                orderSequence.add(i, order);
                if (Evaluator.isFeasible(new Solution(orderSequencesCopy))) vesselIndices.add(i);
                orderSequence.remove(i);
            }
            insertionIndices.add(vesselIndices);
        }

        return insertionIndices;
    }

    public static Solution constructRandomInitialSolution() {

        List<Order> copyOfOrders = Helpers.deepCopyList(Problem.orders);
        List<List<Order>> orderSequences = new ArrayList<>();

        // Creating order sequences
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());

        while (!copyOfOrders.isEmpty()) {
            List<List<Integer>> feasibleInsertions = getAllFeasibleInsertions(orderSequences, copyOfOrders.get(0));
            List<Integer> insertionsForVessel;
            List<Integer> feasibleFleetVessels = new ArrayList<>();
            List<Integer> feasibleSpotVessels = new ArrayList<>();

            // Returning null if there are still orders to be placed and there are no feasible insertions
            for (int i = 0; i < feasibleInsertions.size(); i++) {
                if (!feasibleInsertions.get(i).isEmpty()) break;
                if (i == feasibleInsertions.size()-1) return null;
            }

            // Categorizing feasible fleet and spot vessels given feasible insertions
            for (int i = 0; i < feasibleInsertions.size(); i++) {
                if (!feasibleInsertions.get(i).isEmpty() && !Problem.isSpotVessel(Problem.getVessel(i))) {
                    feasibleFleetVessels.add(i);
                } else if (!feasibleInsertions.get(i).isEmpty()) {
                    feasibleSpotVessels.add(i);
                }
            }

            // Checking for insertions if spot vessels are needed
            if (feasibleFleetVessels.isEmpty() && !feasibleSpotVessels.isEmpty()) {
                insertionsForVessel = feasibleInsertions.get(feasibleSpotVessels.get(0));
                orderSequences.get(0).add(insertionsForVessel.get(0),copyOfOrders.get(0));
                continue;
            }

            // Generating random number for choosing feasible fleet vessel
            int vesselNum = ThreadLocalRandom.current().nextInt(0, feasibleFleetVessels.size());

            // Inserting order in chosen fleet vessel
            insertionsForVessel = feasibleInsertions.get(feasibleFleetVessels.get(vesselNum));
            orderSequences.get(feasibleFleetVessels.get(vesselNum)).add(insertionsForVessel.get(0),copyOfOrders.get(0));

            // Removing order from remaining order list
            copyOfOrders.remove(0);
        }

        Solution solution = new Solution(orderSequences);
        solution.setFitness();

        return solution;
    }
}
