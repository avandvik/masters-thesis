package subproblem;

import objects.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubProblemRemoval extends SubProblem implements Runnable {

    private final int removalIdx;

    public static Map<List<Integer>, Double> removalToObjective;

    public SubProblemRemoval(List<Order> orderSequence, int vesselIdx, int removalIdx) {
        super(orderSequence, vesselIdx);
        this.removalIdx = removalIdx;
    }

    public static void initializeResultsStructure() {
        removalToObjective = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        super.solveSubProblem();
        List<Integer> removalKey = new ArrayList<>(Arrays.asList(super.getVesselIdx(), this.removalIdx));
        removalToObjective.put(removalKey, super.getShortestPathCost());
    }
}
