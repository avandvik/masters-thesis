package alns.heuristics;

import alns.Objective;
import alns.Solution;
import alns.heuristics.protocols.Repairer;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class InsertionRegret extends Heuristic implements Repairer {

    public InsertionRegret(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    public Solution getRegretInsertion(Solution partialSolution, int regretParameter) {
        Solution solution = Helpers.copySolution(partialSolution);
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        List<Double> increases = new ArrayList<>();
        List<Double> sumOfIncreases = new ArrayList<>();
        List<Order> ordersToPlaceList = new ArrayList<>(unplacedOrders);

        while (!ordersToPlaceList.isEmpty()) {
            List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
            for (Order order : ordersToPlaceList) {
                Map<Integer, List<Integer>> insertions =
                        Construction.getAllFeasibleInsertions(orderSequences, order);
                for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
                    List<Order> orderSequence = orderSequences.get(vesselNumber);
                    double currentObj = Objective.runSubProblemLean(orderSequence, vesselNumber);
                    for (int insertionIdx : insertions.get(vesselNumber)) {
                        List<Order> orderSequenceCopy = Helpers.deepCopyList(orderSequence, true);
                        orderSequenceCopy.add(insertionIdx, order);
                        double increase = Objective.runSubProblemLean(orderSequenceCopy, vesselNumber) - currentObj;
                        increases.add(0, increase);
                    }
                }

                double sum = 0.0;
                Collections.sort(increases);
                for (int i = 0; i < Math.min(regretParameter, increases.size()); i++)
                    sum += increases.get(i) - increases.get(0);
                sumOfIncreases.add(sum);
            }

            double greatestRegret = 0.0;
            int greatestRegretIndex = 0;

            for (int orderIdx = 0; orderIdx < ordersToPlaceList.size(); orderIdx++) {
                if (sumOfIncreases.get(orderIdx) > greatestRegret) {
                    greatestRegret = sumOfIncreases.get(orderIdx);
                    greatestRegretIndex = orderIdx;
                }
            }

            solution = InsertionGreedy.getGreedyInsertion(solution, ordersToPlaceList.remove(greatestRegretIndex));
            sumOfIncreases = new ArrayList<>();
        }

        return solution;
    }

    @Override
    public Solution repair(Solution partialSolution) {
        Solution solution = partialSolution;
        int regretParameter = 2;
        solution = getRegretInsertion(solution, regretParameter);
        return solution;
    }
}
