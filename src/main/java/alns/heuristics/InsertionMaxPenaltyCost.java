package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Repairer;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class InsertionMaxPenaltyCost extends Heuristic implements Repairer {

    public InsertionMaxPenaltyCost(String name) {
        super(name);
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution newSolution = Helpers.deepCopySolution(partialSolution);
        List<Order> sortedOrders = sortOrdersByPenalty(newSolution);

        for (Order order : sortedOrders) {
            newSolution = InsertionGreedy.insertGreedilyInSolution(newSolution, order);
        }
        return newSolution;
    }

    private List<Order> sortOrdersByPenalty(Solution partialSolution) {
        Set<Order> orders = partialSolution.getUnplacedOrders();
        Map<Order, Double> mandatoryOrderToPenalty = new HashMap<>();
        Map<Order, Double> optionalOrderToPenalty = new HashMap<>();
        Map<Order, Double> sortedMandatoryOrderToPenalty = new LinkedHashMap<>();
        Map<Order, Double> sortedOptionalOrderToPenalty = new LinkedHashMap<>();
        List<Order> sortedOrders = new ArrayList<>();

        for (Order order : orders) {
            double postponementPenalty = order.getPostponementPenalty();
            if (order.isMandatory()) {
                mandatoryOrderToPenalty.put(order, postponementPenalty);
            } else {
                optionalOrderToPenalty.put(order, postponementPenalty);
            }
        }

        mandatoryOrderToPenalty.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedMandatoryOrderToPenalty.put(x.getKey(), x.getValue()));
        optionalOrderToPenalty.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(y -> sortedOptionalOrderToPenalty.put(y.getKey(), y.getValue()));

        Set<Order> sortedMandatoryOrderKeySet = sortedMandatoryOrderToPenalty.keySet();
        Set<Order> sortedOptionalOrderKeySet = sortedOptionalOrderToPenalty.keySet();
        sortedOrders.addAll(sortedMandatoryOrderKeySet);
        sortedOrders.addAll(sortedOptionalOrderKeySet);

        return sortedOrders;
    }
}
