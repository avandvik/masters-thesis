package localsearch;

import alns.Objective;
import alns.Solution;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.List;

public class OperatorTwoRelocate extends OperatorTwo {

    public static Solution twoRelocate(Solution solution) {
        initialize(solution);
        for (int vIdxOne = 0; vIdxOne < Problem.getNumberOfVessels() - 1; vIdxOne++) {
            for (int vIdxTwo = vIdxOne + 1; vIdxTwo < Problem.getNumberOfVessels(); vIdxTwo++) {
                double ogCost = calculateOriginalCost(vIdxOne, vIdxTwo);
                List<Installation> ogInstSeqOne = Helpers.getInstSequence(newSolution.getOrderSequence(vIdxOne));
                List<Installation> ogInstSeqTwo = Helpers.getInstSequence(newSolution.getOrderSequence(vIdxTwo));
                if (ogInstSeqOne.isEmpty() || ogInstSeqTwo.isEmpty()) continue;
                for (Installation inst : ogInstSeqOne) {
                    List<Installation> newInstSeqOne = rmInstFromSequence(ogInstSeqOne, inst);
                    List<Order> newOrderSeqOne = createNewOrderSequence(newInstSeqOne);
                    for (int insertionIdx = 0; insertionIdx <= ogInstSeqTwo.size(); insertionIdx++) {
                        List<Installation> newInstSeqTwo = addInstToPosition(ogInstSeqTwo, inst, insertionIdx);
                        List<Order> newOrderSeqTwo = createNewOrderSequence(newInstSeqTwo);
                        List<List<Order>> orderSequences = Helpers.wrapListsInList(newOrderSeqOne, newOrderSeqTwo);
                        updateFields(orderSequences, vIdxOne, vIdxTwo, ogCost);
                    }
                }
                greatestDecrease = 0.0;
            }
        }
        Objective.setObjValAndSchedule(newSolution);
        return newSolution;
    }
}
