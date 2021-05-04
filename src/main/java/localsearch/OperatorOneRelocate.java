package localsearch;

import alns.Evaluator;
import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class OperatorOneRelocate extends OperatorOne {

    public static Solution oneRelocate(Solution solution) {
        initialize(solution);
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            List<Order> orderSequence = originalSolution.getOrderSequence(vesselIdx);
            if (orderSequence.isEmpty()) continue;
            List<Installation> instSequence = Helpers.getInstSequence(orderSequence);
            seenInstSequences.add(instSequence);
            for (Installation installation : instSequence) {
                List<Installation> rmInstSequence = rmInstFromSequence(instSequence, installation);
                for (int insertionIdx = 0; insertionIdx <= rmInstSequence.size(); insertionIdx++) {
                    List<Installation> newInstSequence = addInstToPosition(rmInstSequence, installation, insertionIdx);
                    if (seenInstSequences.contains(newInstSequence)) continue;
                    seenInstSequences.add(newInstSequence);
                    List<Order> newOrderSequence = createNewOrderSequence(newInstSequence);
                    if (Evaluator.isOrderSequenceFeasible(newOrderSequence, Problem.getVessel(vesselIdx))) {
                        updateFields(newOrderSequence, vesselIdx);
                    }
                }
            }
        }
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }
}
