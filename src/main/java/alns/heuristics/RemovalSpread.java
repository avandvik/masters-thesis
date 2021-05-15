package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
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
    public Solution destroy(Solution solution) {
        int nbrOrders = getNbrOrdersToRemove(solution);
        Solution newSolution = getSpreadRemoval(solution, nbrOrders);
        // if (!Evaluator.isPartFeasible(newSolution)) throw new IllegalStateException(Messages.solutionInfeasible);
        newSolution.clearSubProblemResults();
        return newSolution;
    }

    private Solution getSpreadRemoval(Solution solution, int numberOfOrders) {
        /*  */

        Solution newSolution = Helpers.deepCopySolution(solution);
        List<Order> orders = Helpers.deepCopyList(Problem.orders, true);
        orders.removeIf(candidateOrder -> newSolution.getAllPostponed().contains(candidateOrder));

        while (newSolution.getUnplacedOrders().size() < numberOfOrders) {
            Order removalOrder = removeSpread(orders);
            List<Order> removalOrders = getOrdersToRemove(removalOrder);
            for (Order order : removalOrders) {
                newSolution.removeOrderFromSequences(order);
                newSolution.getAllPostponed().remove(order);
                newSolution.getUnplacedOrders().add(order);
                orders.remove(removalOrder);
            }
        }
        return newSolution;
    }

    private Order removeSpread(List<Order> orders) {
        Map<Order, Double> orderToMinTravelDistance = new LinkedHashMap<>();
        for (Order currentOrder : orders) {
            double minDistanceToOrder = Double.POSITIVE_INFINITY;
            for (Order nextOrder : orders) {
                if (currentOrder.getInstallationId() == nextOrder.getInstallationId()) continue;
                double distance = DistanceCalculator.distance(currentOrder, nextOrder, "N");
                if (distance < minDistanceToOrder) {
                    minDistanceToOrder = distance;
                }
            }
            orderToMinTravelDistance.put(currentOrder, minDistanceToOrder);
        }
        List<Map.Entry<Order, Double>> orderToDistance = new ArrayList<>(orderToMinTravelDistance.entrySet());
        orderToDistance.sort(Comparator.comparing(Map.Entry<Order, Double>::getValue));
        Collections.reverse(orderToDistance);
        return orderToDistance.get(0).getKey();
    }
}
