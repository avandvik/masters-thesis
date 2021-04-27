package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorTwoExchange extends OperatorTwo {

    private static Map<Integer, Double> vesselToCost;
    private static double greatestDecrease; // Lowest negative number

    public static Solution twoExchange(Solution solution) {
        initialize(solution);
        for (int vIdxOne = 0; vIdxOne < Problem.getNumberOfVessels() - 1; vIdxOne++) {
            for (int vIdxTwo = vIdxOne + 1; vIdxTwo < Problem.getNumberOfVessels(); vIdxTwo++) {
                double originalCost = calculateOriginalCost(vIdxOne, vIdxTwo);
                List<Order> orderSequence = originalSolution.getOrderSequence(vIdxOne);
                List<Order> nextOrderSequence = originalSolution.getOrderSequence(vIdxTwo);
                if (orderSequence.isEmpty() || nextOrderSequence.isEmpty()) continue;
                List<List<Installation>> instSequences = new ArrayList<>(Arrays.asList(
                        Helpers.getInstSequence(orderSequence), Helpers.getInstSequence(nextOrderSequence)));
                List<List<Integer>> instExchanges = getInstExchangesInter(instSequences);
                performInstExchangesInter(instSequences, vIdxOne, vIdxTwo, instExchanges, originalCost);
            }
        }
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }

    public static void initialize(Solution solution) {
        greatestDecrease = 0.0;
        vesselToCost = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = solution.getOrderSequence(vesselIdx);
            double cost = orderSequence.isEmpty() ? 0.0 : Objective.runSPLean(orderSequence, vesselIdx); // Cached
            vesselToCost.put(vesselIdx, cost);
        }
        originalSolution = solution;
        newSolution = Helpers.deepCopySolution(solution);
    }

    private static double calculateOriginalCost(int vIdxOne, int vIdxTwo) {
        return (vesselToCost.get(vIdxOne) + vesselToCost.get(vIdxTwo));
    }

    private static List<List<Integer>> getInstExchangesInter(List<List<Installation>> instSequences) {
        List<Installation> firstInstSequence = instSequences.get(0);
        List<Installation> secondInstSequence = instSequences.get(1);
        List<List<Integer>> instExchanges = new ArrayList<>();
        for (int firstInstIdx = 0; firstInstIdx < firstInstSequence.size(); firstInstIdx++) {
            for (int secondInstIdx = 0; secondInstIdx < secondInstSequence.size(); secondInstIdx++) {
                instExchanges.add(new LinkedList<>(Arrays.asList(firstInstIdx, secondInstIdx)));
            }
        }
        return instExchanges;
    }

    private static void performInstExchangesInter(List<List<Installation>> instSequences, int vIdxOne, int vIdxTwo,
                                                  List<List<Integer>> instExchanges, double originalCost) {
        for (List<Integer> instExchangeList : instExchanges) {
            int firstInstIdx = instExchangeList.get(0);
            int secondInstIdx = instExchangeList.get(1);
            Installation firstInst = instSequences.get(0).get(firstInstIdx);
            Installation secondInst = instSequences.get(1).get(secondInstIdx);
            List<Installation> firstInstSequence = Helpers.deepCopyList(instSequences.get(0), false);
            List<Installation> secondInstSequence = Helpers.deepCopyList(instSequences.get(1), false);
            firstInstSequence.set(firstInstIdx, secondInst);
            secondInstSequence.set(secondInstIdx, firstInst);
            List<Order> firstOrderSequence = createNewOrderSequence(firstInstSequence);
            List<Order> secondOrderSequence = createNewOrderSequence(secondInstSequence);
            List<List<Order>> orderSequences = new ArrayList<>(Arrays.asList(firstOrderSequence, secondOrderSequence));
            if (orderSequencesFeasible(orderSequences, vIdxOne, vIdxTwo)) {
                updateFields(orderSequences, vIdxOne, vIdxTwo, originalCost);
            }
        }
    }

    private static boolean orderSequencesFeasible(List<List<Order>> orderSequences, int vIdxOne, int vIdxTwo) {
        List<Order> firstOrderSequence = orderSequences.get(0);
        List<Order> secondOrderSequence = orderSequences.get(1);
        boolean firstCheck = Evaluator.isOrderSequenceFeasible(firstOrderSequence, Problem.getVessel(vIdxOne));
        boolean secondCheck = Evaluator.isOrderSequenceFeasible(secondOrderSequence, Problem.getVessel(vIdxTwo));
        return firstCheck && secondCheck;
    }

    private static void updateFields(List<List<Order>> orderSequences, int vIdxOne, int vIdxTwo, double originalCost) {
        List<Order> firOrdSeq = orderSequences.get(0);
        List<Order> secOrdSeq = orderSequences.get(1);
        double aggregatedCost = calculateAggregatedCost(orderSequences, vIdxOne, vIdxTwo);
        double decrease = aggregatedCost - originalCost; // Negative number
        if (decrease < 0 && decrease < greatestDecrease) {
            greatestDecrease = decrease;
            newSolution.replaceOrderSequence(vIdxOne, firOrdSeq);
            newSolution.replaceOrderSequence(vIdxTwo, secOrdSeq);
        }
    }

    private static double calculateAggregatedCost(List<List<Order>> orderSequences, int vIdxOne, int vIdxTwo) {
        List<Order> firOrderSeq = orderSequences.get(0);
        List<Order> secOrderSeq = orderSequences.get(1);
        return (Objective.runSPLean(firOrderSeq, vIdxOne) + Objective.runSPLean(secOrderSeq, vIdxTwo));
    }
}
