package alns;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SolutionTest {

    @Test
    @DisplayName("test Constructor")
    public void testConstructor() {
        Problem.setUpProblem("basicTestData.json", true);
        List<List<Order>> orderSequences = getOrderSequences();

        Solution solutionOne = new Solution(orderSequences);
        assertNotNull(solutionOne);
        assertEquals(orderSequences, solutionOne.getOrderSequences());
    }

    @Test
    @DisplayName("test getInstSequences")
    public void testGetInstSequences() {
        Problem.setUpProblem("basicTestData.json", true);
        List<List<Order>> orderSequences = getOrderSequences();
        Solution solution = new Solution(orderSequences);
        List<List<Integer>> instSequences = solution.getInstSequences();
        List<List<Integer>> instSequencesExpected = getExpectedInstSequences();
        assertEquals(instSequencesExpected, instSequences);
    }

    private List<List<Order>> getOrderSequences() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>());  // PSV 1
        orderSequences.add(new LinkedList<>());  // PSV 4
        orderSequences.add(new LinkedList<>());  // SPOT
        for (int i = 0; i < 3; i++) orderSequences.get(0).add(Problem.getOrder(i));
        for (int i = 3; i < Problem.getNumberOfOrders(); i++) orderSequences.get(1).add(Problem.getOrder(i));
        return orderSequences;
    }

    private List<List<Integer>> getExpectedInstSequences() {
        List<List<Integer>> instSequencesExpected = new ArrayList<>();
        List<Integer> instSequenceOne = new ArrayList<>(Arrays.asList(21, 21, 13));
        List<Integer> instSequenceTwo = new ArrayList<>(Arrays.asList(5, 5, 11, 25, 17));
        List<Integer> instSequenceThree = new ArrayList<>();
        instSequencesExpected.add(instSequenceOne);
        instSequencesExpected.add(instSequenceTwo);
        instSequencesExpected.add(instSequenceThree);
        return instSequencesExpected;
    }
}
