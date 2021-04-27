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
        for (int firVesselIdx = 0; firVesselIdx < Problem.getNumberOfVessels() - 1; firVesselIdx++) {
            for (int secVesselIdx = firVesselIdx + 1; secVesselIdx < Problem.getNumberOfVessels(); secVesselIdx++) {
                double originalCost = calculateOriginalCost(firVesselIdx, secVesselIdx);
                List<Order> orderSequence = originalSolution.getOrderSequence(firVesselIdx);
                List<Order> nextOrderSequence = originalSolution.getOrderSequence(secVesselIdx);
                if (orderSequence.isEmpty() || nextOrderSequence.isEmpty()) continue;
                List<Installation> firstInstSequence = Helpers.getInstSequence(orderSequence);
                List<Installation> secondInstSequence = Helpers.getInstSequence(nextOrderSequence);
                List<List<Installation>> instSequences = new ArrayList<>(Arrays.asList(firstInstSequence, secondInstSequence));
                List<List<Integer>> instExchanges = getInstExchangesInter(instSequences);
                performInstExchangesInter(instSequences, instExchanges, firVesselIdx, secVesselIdx, originalCost);

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

    private static double calculateOriginalCost(Integer firstVesselIdx, Integer secondVesselIdx) {
        return (vesselToCost.get(firstVesselIdx) + vesselToCost.get(secondVesselIdx));
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

    private static void performInstExchangesInter(List<List<Installation>> instSequences, List<List<Integer>> instExchanges,
                                                  int firVesselIdx, int secVesselIdx, Double originalCost) {
        List<Installation> firInstSeq = instSequences.get(0);
        List<Installation> secInstSeq = instSequences.get(1);
        for (List<Integer> instExchangeList : instExchanges) {
            int firstInstIdx = instExchangeList.get(0);
            int secondInstIdx = instExchangeList.get(1);
            Installation firstInst = firInstSeq.get(firstInstIdx);
            Installation secondInst = secInstSeq.get(secondInstIdx);
            List<Installation> firstInstSequence = Helpers.deepCopyList(firInstSeq, false);
            List<Installation> secondInstSequence = Helpers.deepCopyList(secInstSeq, false);
            firstInstSequence.set(firstInstIdx, secondInst);
            secondInstSequence.set(secondInstIdx, firstInst);
            List<Order> firstOrderSequence = createNewOrderSequence(firstInstSequence);
            List<Order> secondOrderSequence = createNewOrderSequence(secondInstSequence);
            List<List<Order>> orderSequences = new ArrayList<>(Arrays.asList(firstOrderSequence, secondOrderSequence));
            if (orderSequencesFeasible(orderSequences, firVesselIdx, secVesselIdx)) {
                updateFields(orderSequences, firVesselIdx, secVesselIdx, originalCost);
            }
        }
    }

    private static boolean orderSequencesFeasible(List<List<Order>> orderSequences, Integer firVesselIdx, Integer secVesselIdx) {
        List<Order> firstOrderSequence = orderSequences.get(0);
        List<Order> secondOrderSequence = orderSequences.get(1);
        boolean firstCheck = Evaluator.isOrderSequenceFeasible(firstOrderSequence, Problem.getVessel(firVesselIdx));
        boolean secondCheck = Evaluator.isOrderSequenceFeasible(secondOrderSequence, Problem.getVessel(secVesselIdx));
        return firstCheck && secondCheck;
    }

    private static double calculateAggregatedCost(List<List<Order>> orderSequences, Integer firVesselIdx, Integer secVesselIdx) {
        List<Order> firOrderSeq = orderSequences.get(0);
        List<Order> secOrderSeq = orderSequences.get(1);
        return (Objective.runSPLean(firOrderSeq, firVesselIdx) + Objective.runSPLean(secOrderSeq, secVesselIdx));
    }

    private static void updateFields(List<List<Order>> orderSequences, Integer firVesselIdx, Integer secVesselIdx, Double originalCost) {
        List<Order> firOrdSeq = orderSequences.get(0);
        List<Order> secOrdSeq = orderSequences.get(1);
        double aggregatedCost = calculateAggregatedCost(orderSequences, firVesselIdx, secVesselIdx);
        double decrease = aggregatedCost - originalCost; // Negative number
        if (decrease < 0 && decrease < greatestDecrease) {
            greatestDecrease = decrease;
            newSolution.replaceOrderSequence(firVesselIdx, firOrdSeq);
            newSolution.replaceOrderSequence(secVesselIdx, secOrdSeq);
        }
    }
}
