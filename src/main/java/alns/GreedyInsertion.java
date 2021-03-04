package alns;

import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GreedyInsertion {

    public static Solution basicGreedyInsertion(Solution partialSolution, Order order) {

        List<List<Order>> copyOfOrderSequences = Helpers.deepCopyList(partialSolution.getOrderSequences());
        List<List<Order>> newOrderSequences = new ArrayList<>();
        //for (int i = 0; i < Problem.getNumberOfVessels(); i++) newOrderSequences.add(new LinkedList<>(o));
        for (List<Order> orderSequence : copyOfOrderSequences) newOrderSequences.add(orderSequence);
        /* System.out.println(Problem.getNumberOfVessels());
        System.out.println(newOrderSequences.size());
        System.out.println(newOrderSequences); */
        while (newOrderSequences.size() < Problem.getNumberOfVessels()) {
            newOrderSequences.add(new LinkedList<>());
            System.out.println(newOrderSequences);
        }

        List<List<Integer>> allFeasibleInsertions = Construction.getAllFeasibleInsertions(newOrderSequences,order);

        double bestObjective = Double.POSITIVE_INFINITY;
        List<Integer> bestObjetiveIndices = new ArrayList<>();

        for (int i = 0; i < allFeasibleInsertions.size(); i++) {
            if (!allFeasibleInsertions.get(i).isEmpty()) break;
            if (i == allFeasibleInsertions.size()-1) return null;
        }

        for (int j = 0; j < allFeasibleInsertions.size(); j++) {
            for (int k = 0; k < allFeasibleInsertions.get(j).size(); k++) {
                newOrderSequences.get(j).add(k,order);
                Solution tempSolution = new Solution(newOrderSequences);
                double tempObjective = tempSolution.getObjectiveValue();
                if (tempObjective < bestObjective) {
                    bestObjective = tempObjective;
                    bestObjetiveIndices.clear();
                    bestObjetiveIndices.add(j);
                    bestObjetiveIndices.add(k);
                }
                newOrderSequences.get(j).remove(order);
            }
        }

        newOrderSequences.get(bestObjetiveIndices.get(0)).add(bestObjetiveIndices.get(1),order);
        return new Solution(newOrderSequences);
    }

    public static void main(String[] args) {
        // Solution basicGreedySolution = basicGreedyInsertion(solution,orderToBePlaced);
        // System.out.println(basicGreedySolution);
    }
}
