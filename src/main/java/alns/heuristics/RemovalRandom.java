package alns.heuristics;

import alns.Evaluator;
import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class RemovalRandom extends Heuristic implements Destroyer {

    public RemovalRandom(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        Solution newSolution = solution;
        while (newSolution.getUnplacedOrders().size() < numberOfOrders) newSolution = getRandomRemoval(newSolution);
        if (!Evaluator.isPartFeasible(newSolution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return newSolution;
    }

    public Solution getRandomRemoval(Solution solution) {
        Solution newSolution = Helpers.deepCopySolution(solution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> postponedOrders = newSolution.getPostponedOrders();
        Set<Order> unplacedOrders = newSolution.getUnplacedOrders();

        List<Order> ordersToRemove = findRandomOrdersToRemove(orderSequences, postponedOrders);

        unplacedOrders.addAll(ordersToRemove);
        postponedOrders.removeAll(ordersToRemove);
        for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);
        return newSolution;
    }

    public static List<Order> findRandomOrdersToRemove(List<List<Order>> orderSequences, Set<Order> postponedOrders) {
        List<Order> ordersToRemove = new ArrayList<>();
        while (ordersToRemove.isEmpty()) {
            int rnSequenceIdx = Problem.random.nextInt(orderSequences.size() + 1);
            if (rnSequenceIdx == orderSequences.size()) {
                if (postponedOrders.size() > 0) {
                    ordersToRemove = getOrdersToRemove(Helpers.getRandomElementFromSet(postponedOrders));
                }
            } else {
                if (orderSequences.get(rnSequenceIdx).size() > 0) {
                    int rnOrderIdx = Problem.random.nextInt(orderSequences.get(rnSequenceIdx).size());
                    ordersToRemove = getOrdersToRemove(orderSequences.get(rnSequenceIdx).get(rnOrderIdx));
                }
            }
        }
        return ordersToRemove;
    }
}
