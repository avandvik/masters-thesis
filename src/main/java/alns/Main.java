package alns;

import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void runALNS() {
        // Create initial solution by calling construction heuristic

        // Run iterations
        //   Run time segment
        //     1. Choose destroy and repair neighborhood (roulette wheel based on scores)
        //     2. Generate new solution by applying one destroy and one repair operator
        //     3. Check acceptance criterion (simulated annealing), update current solution if accepted
        //     4. Update scores (dependent on acceptance)
        //     5. Update global best if the fitness is better than global best
        //     6. Update scores (dependent on new global best) [MERGE 4 and 6?]


    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json", false);
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < 3; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 3; i < Problem.getNumberOfOrders(); i++) orderSequences.get(1).add(Problem.getOrder(i));
        Solution solution = new Solution(orderSequences);
        SubProblem.runSubProblem(solution);
    }
}
