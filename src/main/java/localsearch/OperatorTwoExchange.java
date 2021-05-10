package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorTwoExchange extends OperatorTwo {

    public static Solution twoExchange(Solution solution) {
        initialize(solution);
        for (int vIdxOne = 0; vIdxOne < Problem.getNumberOfVessels() - 1; vIdxOne++) {
            for (int vIdxTwo = vIdxOne + 1; vIdxTwo < Problem.getNumberOfVessels(); vIdxTwo++) {
                double originalCost = calculateOriginalCost(vIdxOne, vIdxTwo);
                List<Installation> instSeqOne = Helpers.getInstSequence(newSolution.getOrderSequence(vIdxOne));
                List<Installation> instSeqTwo = Helpers.getInstSequence(newSolution.getOrderSequence(vIdxTwo));
                if (instSeqOne.isEmpty() || instSeqTwo.isEmpty()) continue;
                List<List<Installation>> instSequences = Helpers.wrapListsInList(instSeqOne, instSeqTwo);
                List<List<Integer>> instExchanges = getInstExchangesInter(instSequences);
                performInstExchangesInter(instSequences, vIdxOne, vIdxTwo, instExchanges, originalCost);
                greatestDecrease = 0.0;
            }
        }
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }

    private static List<List<Integer>> getInstExchangesInter(List<List<Installation>> instSequences) {
        List<Installation> firstInstSequence = instSequences.get(0);
        List<Installation> secondInstSequence = instSequences.get(1);
        List<List<Integer>> instExchanges = new ArrayList<>();
        for (int firstInstIdx = 0; firstInstIdx < firstInstSequence.size(); firstInstIdx++) {
            for (int secondInstIdx = 0; secondInstIdx < secondInstSequence.size(); secondInstIdx++) {
                instExchanges.add(new ArrayList<>(Arrays.asList(firstInstIdx, secondInstIdx)));
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
            List<Order> orderSequenceOne = createNewOrderSequence(firstInstSequence);
            List<Order> orderSequenceTwo = createNewOrderSequence(secondInstSequence);
            List<List<Order>> orderSequences = Helpers.wrapListsInList(orderSequenceOne, orderSequenceTwo);
            updateFields(orderSequences, vIdxOne, vIdxTwo, originalCost);
        }
    }
}
