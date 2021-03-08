package alns.heuristics;

import alns.Evaluator;
import alns.Solution;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.*;

public class ConstructionTest {

    @Test
    @DisplayName("test getFeasibleInsertions")
    public void getFeasibleInsertionsTest() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());
        for (int i = 0; i < 2; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 2; i < 5; i++) orderSequences.get(1).add(Problem.getOrder(i));
        for (int i = 5; i < 7; i++) orderSequences.get(2).add(Problem.getOrder(i));
        Order orderToBePlaced = Problem.orders.get(Problem.orders.size() - 1);

        Map<Integer, List<Integer>> expectedIndices = new HashMap<>();
        List<Integer> firstRowIndices = new ArrayList<>(Arrays.asList(0, 2));
        List<Integer> secondRowIndices = new ArrayList<>(Arrays.asList(0, 1, 3));
        List<Integer> thirdRowIndices = new ArrayList<>(Arrays.asList(0, 1, 2));
        expectedIndices.put(0, firstRowIndices);
        expectedIndices.put(1, secondRowIndices);
        expectedIndices.put(2, thirdRowIndices);

        assertEquals(expectedIndices, Construction.getAllFeasibleInsertions(orderSequences, orderToBePlaced));

        List<Solution> expectedSolutions = new ArrayList<>();
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            for (int idx : expectedIndices.get(vesselNumber)) {
                List<Order> orderSequence = new LinkedList<>(orderSequences.get(vesselNumber));
                orderSequence.add(idx, orderToBePlaced);
                List<List<Order>> orderSequencesCopy = new ArrayList<>();
                for (int j = 0; j < Problem.getNumberOfVessels(); j++) {
                    if (j == vesselNumber) {
                        orderSequencesCopy.add(orderSequence);
                        continue;
                    }
                    orderSequencesCopy.add(j, new LinkedList<>(orderSequences.get(j)));
                }
                Solution newSolution = new Solution(orderSequencesCopy, new HashSet<>(), false);
                expectedSolutions.add(newSolution);
            }
        }

        Set<Order> postponedOrder = new HashSet<>(Collections.singletonList(orderToBePlaced));
        Solution postponedSolution = new Solution(orderSequences, postponedOrder, true);
        expectedSolutions.add(postponedSolution);
    }

    @Test
    @DisplayName("test constructRandomInitialSolution")
    public void constructRandomInitialSolutionTest() {
        Problem.setUpProblem("basicTestData.json",true, 10);
        assertTrue(Evaluator.isSolutionFeasible(Construction.constructRandomInitialSolution()));
    }
}
