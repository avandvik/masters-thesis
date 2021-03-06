package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Parameters;
import data.Problem;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class RemovalRelated extends Heuristic implements Destroyer {

    private Map<Order, Double> orderToRelatedness;
    private int numberOfOrders;

    public RemovalRelated(String name) {
        super(name);
    }

    @Override
    public Solution destroy(Solution solution) {
        numberOfOrders = getNbrOrdersToRemove(solution);
        Solution newSolution = solution;
        while (newSolution.getUnplacedOrders().size() < numberOfOrders) newSolution = getRelatedRemoval(newSolution);
        newSolution.clearSubProblemResults();
        return newSolution;
    }

    private Solution getRelatedRemoval(Solution solution) {
        /* Finds the most related removal and returns the new solution */

        Solution newSolution = Helpers.deepCopySolution(solution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> postponedOrders = newSolution.getAllPostponed();
        List<Order> ordersToRemove = RemovalRandom.findRandomOrdersToRemove(orderSequences, postponedOrders);
        Order baseOrder = ordersToRemove.get(ordersToRemove.size() - 1);
        if (newSolution.getUnplacedOrders().size() + ordersToRemove.size() >= numberOfOrders) {
            updateSolution(newSolution, ordersToRemove);
            return newSolution;
        }
        this.mapOrderToRelatedness(orderSequences, postponedOrders, ordersToRemove, baseOrder);
        List<Order> relatedOrdersToRemove = this.findRelatedOrdersToRemove();
        ordersToRemove.addAll(relatedOrdersToRemove);
        updateSolution(newSolution, ordersToRemove);
        return newSolution;
    }

    private void updateSolution(Solution solution, List<Order> ordersToRemove) {
        solution.getUnplacedOrders().addAll(ordersToRemove);
        ordersToRemove.forEach(solution.getAllPostponed()::remove);
        for (List<Order> orderSequence : solution.getOrderSequences()) orderSequence.removeAll(ordersToRemove);
    }

    private void mapOrderToRelatedness(List<List<Order>> orderSequences, Set<Order> postponedOrders,
                                       List<Order> ordersToRemove, Order baseOrder) {
        this.orderToRelatedness = new HashMap<>();
        for (List<Order> orderSequence : orderSequences) {
            for (Order compareOrder : orderSequence) {
                if (ordersToRemove.contains(compareOrder)) continue;
                this.orderToRelatedness.put(compareOrder, DistanceCalculator.distance(baseOrder, compareOrder, "N"));
            }
        }
        for (Order compareOrder : postponedOrders) {
            if (ordersToRemove.contains(compareOrder)) continue;
            this.orderToRelatedness.put(compareOrder, DistanceCalculator.distance(baseOrder, compareOrder, "N"));
        }
    }

    private List<Order> findRelatedOrdersToRemove() {
        List<Map.Entry<Order, Double>> ordersRelatedness = new ArrayList<>(this.orderToRelatedness.entrySet());
        ordersRelatedness.sort(Comparator.comparing(Map.Entry<Order, Double>::getValue));
        int removeIdx = (int) (Math.pow(Problem.random.nextDouble(), Parameters.p) * ordersRelatedness.size());
        Order relatedOrder = ordersRelatedness.get(removeIdx).getKey();
        return getOrdersToRemove(relatedOrder);
    }
}
