package alns;

import data.Problem;
import objects.Order;
import objects.Vessel;
import org.junit.Test;
import utils.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EvaluatorTest {

    @Test
    public void testIsFeasibleLoad() {
        Problem.setUpProblem("load/load.json", true, 10);

        List<Order> orders = Problem.orders;
        List<Order> orderSequenceOne = Problem.orders.subList(0, 1);
        List<Order> orderSequenceTwo = Problem.orders.subList(1, 3);
        List<Order> orderSequenceThree = Problem.orders.subList(0, 2);
        List<Order> orderSequenceFour = Problem.orders.subList(2, 5);
        List<Order> orderSequenceFive = Problem.orders.subList(2, 6);
        List<Order> orderSequenceSix = Problem.orders.subList(6, 9);
        List<Order> orderSequenceSeven = Problem.orders.subList(6, 10);
        Vessel vessel = Problem.getVessel(0);

        // Testing for all orders in sequence: total delivery load should exceed vessel capacity
        assertFalse(Evaluator.isFeasibleLoadSequence(orders, vessel));

        // Testing for one delivery order of 1000 sqm = 125 cu: total delivery load = vessel capacity
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequenceOne, vessel));
        // Testing for two delivery order of total 510 sqm = 64 cu: total delivery load < vessel capacity
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequenceTwo, vessel));
        // Testing for two delivery orders of 1010 sqm = 127 cu: total delivery load > vessel capacity
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequenceThree, vessel));

        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, followed by one pickup order of 10 sqm =
        // 2 cu
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequenceFour, vessel));
        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, followed by two pickup order of total
        // 10 + 990 sqm = 126 cu: total load at last order > vessel capacity
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequenceFive, vessel));

        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, with pickup order of 490 sqm = 62 cu in
        // between
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequenceSix, vessel));
        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, with pickup order of 490 sqm = 62 cu in
        // between,
        // and one pickup order of 500 sqm = 63 cu at the end: total load at last order > vessel capacity
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequenceSeven, vessel));
    }

    @Test
    public void testIsFeasibleDuration() {
        Problem.setUpProblem("duration/duration_long.json", true, 10);
        List<Order> ordersOne = Problem.orders;
        List<Order> orderSequenceOne = new LinkedList<>();
        orderSequenceOne.addAll(ordersOne);
        assertFalse(Evaluator.isFeasibleDurationSequence(orderSequenceOne));

        Problem.setUpProblem("duration/duration_short.json", true, 10);
        List<Order> ordersTwo = Problem.orders;
        List<Order> orderSequenceTwo = new LinkedList<>();
        orderSequenceOne.addAll(ordersTwo);
        assertTrue(Evaluator.isFeasibleDurationSequence(orderSequenceTwo));
    }

    @Test
    public void testIsInstInMoreThanOneSequence() {
        Problem.setUpProblem("visits/allOrderCombos.json", true, 10);

        // Check valid
        List<List<Integer>> validSequences = new ArrayList<>();
        List<Integer> sequenceOne = new LinkedList<>();
        for (int i = 0; i < 3; i++) sequenceOne.add(Problem.orders.get(i).getInstallationId());
        List<Integer> sequenceTwo = new LinkedList<>();
        for (int i = 3; i < 7; i++) sequenceTwo.add(Problem.orders.get(i).getInstallationId());
        List<Integer> sequenceThree = new LinkedList<>();
        for (int i = 7; i < 9; i++) sequenceThree.add(Problem.orders.get(i).getInstallationId());

        validSequences.add(sequenceOne);
        validSequences.add(sequenceTwo);
        validSequences.add(sequenceThree);

        assertFalse(Evaluator.instInMoreThanOneSequence(validSequences));

        // Check invalid (one installation in different sequences)
        List<List<Integer>> invalidSequences = new ArrayList<>();
        List<Integer> sequenceFour = new LinkedList<>();
        for (int i = 0; i < 2; i++) sequenceFour.add(Problem.orders.get(i).getInstallationId());
        List<Integer> sequenceFive = new LinkedList<>();
        for (int i = 3; i < 7; i++) sequenceFive.add(Problem.orders.get(i).getInstallationId());
        List<Integer> sequenceSix = new LinkedList<>();
        for (int i = 7; i < 9; i++) sequenceSix.add(Problem.orders.get(i).getInstallationId());
        sequenceSix.add(Problem.orders.get(2).getInstallationId());

        invalidSequences.add(sequenceFour);
        invalidSequences.add(sequenceFive);
        invalidSequences.add(sequenceSix);

        assertTrue(Evaluator.instInMoreThanOneSequence(invalidSequences));

        // Check invalid (two installations in different sequences)
        invalidSequences.get(0).add(invalidSequences.get(1).remove(1));
        assertTrue(Evaluator.instInMoreThanOneSequence(invalidSequences));
    }

    @Test
    public void testIsIllegalPattern() {
        Problem.setUpProblem("visits/allOrderCombos.json", true, 10);
        List<Order> orderSequence = new LinkedList<>(Problem.orders);

        // Check valid (All combos present MD-OD-OP, MD-OD, MD-OP, OD-OP)
        assertFalse(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstSequence(orderSequence)));

        // Check illegal visit order: OD-MD
        Collections.swap(orderSequence, 3, 4);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstSequence(orderSequence)));

        // Check illegal visit order: OP-MD
        Collections.swap(orderSequence, 3, 4);
        Collections.swap(orderSequence, 5, 6);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstSequence(orderSequence)));


        // Check illegal visit order: OP-OD
        Collections.swap(orderSequence, 5, 6);
        Collections.swap(orderSequence, 7, orderSequence.size() - 1);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstSequence(orderSequence)));

        // Check spread
        Collections.swap(orderSequence, 7, orderSequence.size() - 1);
        orderSequence.add(orderSequence.remove(1));
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstSequence(orderSequence)));

        // Check that it was actually spread that was the reason in previous test
        List<Order> partialSequence = new LinkedList<>(orderSequence.subList(0, orderSequence.size() - 1));
        assertFalse(Evaluator.isIllegalPattern(partialSequence, Helpers.getInstSequence(partialSequence)));

        // Check multiple
        orderSequence.add(1, orderSequence.remove(orderSequence.size() - 1));
        Collections.swap(orderSequence, 0, 2);
        orderSequence.add(0, orderSequence.remove(3));
        Collections.swap(orderSequence, 6, 7);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstSequence(orderSequence)));
    }
}
