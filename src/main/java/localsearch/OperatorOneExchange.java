package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Messages;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorOneExchange extends OperatorOne {

    public static Solution oneExchange(Solution solution) {
        initialize(solution);
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = originalSolution.getOrderSequence(vesselIdx);
            if (orderSequence.isEmpty()) continue;
            List<Installation> instSequence = Helpers.getInstSequence(orderSequence);
            Set<Set<Integer>> instExchanges = getInstExchangesIntra(instSequence);
            performInstExchangesIntra(instSequence, instExchanges, vesselIdx);
        }
        if (!Evaluator.isSolutionFeasible(newSolution)) throw new IllegalStateException(Messages.infSolCreated);
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }

    private static Set<Set<Integer>> getInstExchangesIntra(List<Installation> instSequence) {
        Set<Set<Integer>> instExchanges = new HashSet<>();
        for (int firstInstIdx = 0; firstInstIdx < instSequence.size(); firstInstIdx++) {
            for (int secondInstIdx = 0; secondInstIdx < instSequence.size(); secondInstIdx++) {
                if (firstInstIdx == secondInstIdx) continue;
                instExchanges.add(new HashSet<>(Arrays.asList(firstInstIdx, secondInstIdx)));
            }
        }
        return instExchanges;
    }

    private static void performInstExchangesIntra(List<Installation> instSequence, Set<Set<Integer>> instExchanges, Integer vesselIdx) {
        for (Set<Integer> instExchange : instExchanges) {
            List<Integer> instExchangeList = new ArrayList<>(instExchange);
            int firstInstIdx = instExchangeList.get(0);
            int secondInstIdx = instExchangeList.get(1);
            List<Installation> newInstSequence = exchangeInstallations(instSequence, firstInstIdx, secondInstIdx);
            List<Order> newOrderSequence = createNewOrderSequence(newInstSequence);
            if (Evaluator.isOrderSequenceFeasible(newOrderSequence, Problem.getVessel(vesselIdx))) {
                updateFields(newOrderSequence, vesselIdx);
            }
        }
    }

    private static List<Installation> exchangeInstallations(List<Installation> instSequence, int firstInstIdx, int secInstIdx) {
        List<Installation> newInstSequence = Helpers.deepCopyList(instSequence, true);
        Collections.swap(newInstSequence, firstInstIdx, secInstIdx);
        return newInstSequence;
    }
}
