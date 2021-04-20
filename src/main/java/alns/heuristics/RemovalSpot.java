package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class RemovalSpot extends Heuristic implements Destroyer {

    public RemovalSpot(String name) {
        super(name);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        return removeSpotVesselOrders(solution);
    }

    private Solution removeSpotVesselOrders(Solution solution) {
        Solution newSolution = Helpers.deepCopySolution(solution);
        List<List<Order>> orderSequences = newSolution.getOrderSequences();
        Set<Order> postponedOrders = newSolution.getPostponedOrders();
        Set<Order> unplacedOrders = newSolution.getUnplacedOrders();
        int spotVesselId = Problem.vessels.size() - 1;
        unplacedOrders.addAll(orderSequences.get(spotVesselId));
        orderSequences.get(spotVesselId).clear();
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
