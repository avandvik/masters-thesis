package alns.heuristics;

import alns.Evaluator;
import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Problem;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class RemovalSpread extends Heuristic implements Destroyer {

    public RemovalSpread(String name) {
        super(name);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        Solution newSolution = getSpreadRemoval(solution, numberOfOrders);
        if (!Evaluator.isPartFeasible(newSolution)) throw new IllegalStateException(Messages.solutionInfeasible);
        return newSolution;
    }

    private Solution getSpreadRemoval(Solution solution, int numberOfOrders) {
        Solution newSolution = Helpers.deepCopySolution(solution);
        List<Order> orders = Helpers.deepCopyList(Problem.orders, true);
        orders.removeIf(candidateOrder -> newSolution.getPostponedOrders().contains(candidateOrder));

        while (newSolution.getUnplacedOrders().size() < numberOfOrders) {
            Order removalOrder = removeSpread(orders);
            List<Order> removalOrders = getOrdersToRemove(removalOrder);
            for (Order order : removalOrders) {
                newSolution.removeOrderFromSequences(order);
                newSolution.getPostponedOrders().remove(order);
                newSolution.getUnplacedOrders().add(order);
                orders.remove(removalOrder);
            }
        }

        newSolution.clearSubProblemResults();

        return newSolution;
    }

    private Order removeSpread(List<Order> orders) {
        Map<Order, Double> orderToMinTravelDistance = new LinkedHashMap<>();
        for (Order currentOrder : orders) {
            double MIN_DISTANCE_TO_ORDER = Double.POSITIVE_INFINITY;
            for (Order nextOrder : orders) {
                if (currentOrder.getInstallationId() == nextOrder.getInstallationId()) continue;
                double distance = DistanceCalculator.distance(currentOrder, nextOrder, "N");
                if (distance < MIN_DISTANCE_TO_ORDER) {
                    MIN_DISTANCE_TO_ORDER = distance;
                }
            }
            orderToMinTravelDistance.put(currentOrder, MIN_DISTANCE_TO_ORDER);
        }

        List<Map.Entry<Order, Double>> orderToDistance = new ArrayList<>(orderToMinTravelDistance.entrySet());
        orderToDistance.sort(Comparator.comparing(Map.Entry<Order, Double>::getValue));
        Collections.reverse(orderToDistance);
        return orderToDistance.get(0).getKey();
    }
}
