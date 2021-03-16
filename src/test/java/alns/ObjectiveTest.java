package alns;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ObjectiveTest {

    @Test
    @DisplayName("")
    public void runMultipleSPsTest() {
        Problem.setUpProblem("example_8.json", false, 10);
        Map<Integer, List<List<Order>>> vesselToOrderSequences = createVesselToOrderSequences();
        // Map<Integer, List<Double>> results = Objective.runMultipleSPs(vesselToOrderSequences);
        // assertEquals(createExpectedResults(), results);
    }


    private Map<Integer, List<List<Order>>> createVesselToOrderSequences() {
        Map<Integer, List<List<Order>>> vesselToOrderSequences = new HashMap<>();
        List<List<Order>> orderSequencesVesselOne = new ArrayList<>();
        orderSequencesVesselOne.add(new LinkedList<>(Arrays.asList(Problem.getOrder(0), Problem.getOrder(1))));
        orderSequencesVesselOne.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(3))));
        vesselToOrderSequences.put(0, orderSequencesVesselOne);
        List<List<Order>> orderSequencesVesselTwo = new ArrayList<>();
        orderSequencesVesselTwo.add(new LinkedList<>(Arrays.asList(Problem.getOrder(4), Problem.getOrder(5))));
        vesselToOrderSequences.put(1, orderSequencesVesselTwo);
        List<List<Order>> orderSequencesVesselThree = new ArrayList<>();
        orderSequencesVesselThree.add(new LinkedList<>(Arrays.asList(Problem.getOrder(6), Problem.getOrder(7))));
        vesselToOrderSequences.put(2, orderSequencesVesselThree);
        List<List<Order>> orderSequencesVesselFour = new ArrayList<>();
        orderSequencesVesselFour.add(new LinkedList<>());
        vesselToOrderSequences.put(3, orderSequencesVesselFour);
        return vesselToOrderSequences;
    }

    private Map<Integer, List<Double>> createExpectedResults() {
        Map<Integer, List<Double>> expectedResults = new HashMap<>();
        expectedResults.put(0, new ArrayList<>(Arrays.asList(1025.445416666667, 887.3112500000002)));
        expectedResults.put(1, new ArrayList<>(Collections.singletonList(720.6379166666669)));
        expectedResults.put(2, new ArrayList<>(Collections.singletonList(10782.45)));
        expectedResults.put(3, new ArrayList<>());
        return expectedResults;
    }
}
