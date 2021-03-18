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

        int rnSequenceIdx = Problem.random.nextInt(orderSequences.size() + 1);

        if (rnSequenceIdx == orderSequences.size() && postponedOrders.size() > 0) {
            Order orderToRemove = Helpers.removeRandomElementFromSet(postponedOrders);
            List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
            unplacedOrders.addAll(ordersToRemove);
            postponedOrders.removeAll(ordersToRemove);
            for (List<Order> orderSequence : orderSequences) orderSequence.removeAll(ordersToRemove);
            return newSolution;
        }

        if (rnSequenceIdx == orderSequences.size() || orderSequences.get(rnSequenceIdx).size() == 0) return newSolution;

        int randomOrderNumber = Problem.random.nextInt(orderSequences.get(rnSequenceIdx).size());
        Order orderToRemove = orderSequences.get(rnSequenceIdx).remove(randomOrderNumber);
        List<Order> ordersToRemove = getOrdersToRemove(orderToRemove);
        unplacedOrders.addAll(ordersToRemove);
        orderSequences.get(rnSequenceIdx).removeAll(ordersToRemove);
        postponedOrders.removeAll(ordersToRemove);
        return newSolution;
    }
}
