package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Problem;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class RemovalRandom extends Heuristic implements Destroyer {

    // TODO: Test extensively

    public RemovalRandom(String name, boolean destroy, boolean repair) {
        super(name, destroy, repair);
    }

    @Override
    public Solution destroy(Solution solution, int numberOfOrders) {
        List<List<Order>> orderSequences = Helpers.deepCopy2DList(solution.getOrderSequences());
        Set<Order> postponedOrders = Helpers.deepCopySet(solution.getPostponedOrders());
        Set<Order> unplacedOrders = Helpers.deepCopySet(solution.getUnplacedOrders());
        while (numberOfOrders > 0) {
            int rnSequenceIdx = Problem.random.nextInt(orderSequences.size() + 1);

            if (rnSequenceIdx == orderSequences.size() && postponedOrders.size() > 0) {
                unplacedOrders.add(Helpers.removeRandomElementFromSet(postponedOrders));
                numberOfOrders--;
                continue;
            }

            if (rnSequenceIdx == orderSequences.size() || orderSequences.get(rnSequenceIdx).size() == 0) continue;

            int randomOrderNumber = Problem.random.nextInt(orderSequences.get(rnSequenceIdx).size());
            unplacedOrders.add(orderSequences.get(rnSequenceIdx).remove(randomOrderNumber));
            numberOfOrders--;
        }

        // Remove unplaced orders from orderSequences and postponedOrders
        for (List<Order> orderSequence : orderSequences) orderSequence.removeIf(unplacedOrders::contains);
        postponedOrders.removeIf(unplacedOrders::contains);

        // Return a partial solution
        return new Solution(orderSequences, postponedOrders, unplacedOrders);
    }
}
