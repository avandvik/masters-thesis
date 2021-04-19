package alns;

import data.Problem;
import objects.Order;
import objects.Vessel;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import utils.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EvaluatorTest {

    @Test
    @DisplayName("test isFeasibleLoad")
    public void isFeasibleLoadTest() {
        Problem.setUpProblem("load.json", true, 10);
        int capacity = (int) Math.floor(1000 / Problem.sqmInCargoUnit);
        Vessel vessel = new Vessel(99, "Test", capacity, 80);

        testExceedingTotalLoad(vessel);
        testLimitLoadStart(vessel);
        testFeasibleTotalDeliveryLoad(vessel);
        testInfeasibleTotalDeliveryLoad(vessel);
        testFeasiblePickupAfterDelivery(vessel);
        testInfeasiblePickupAfterDeliveryAndPickup(vessel);
        testFeasibleDeliveryPickupDelivery(vessel);
        testInfeasiblePickupAtEnd(vessel);
    }

    private void testExceedingTotalLoad(Vessel vessel) {
        /* All orders in sequence: total delivery load exceeds vessel capacity */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders);
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testLimitLoadStart(Vessel vessel) {
        /* One delivery order of 1000 sqm = 125 cu: total delivery load = vessel capacity */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(0, 1));
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testFeasibleTotalDeliveryLoad(Vessel vessel) {
        /* Two delivery orders of total 510 sqm = 64 cu: total delivery load < vessel capacity */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(1, 3));
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testInfeasibleTotalDeliveryLoad(Vessel vessel) {
        /* Two delivery orders of 1010 sqm = 127 cu: total delivery load > vessel capacity */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(0, 2));
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testFeasiblePickupAfterDelivery(Vessel vessel) {
        /* Two delivery orders of total 500 + 490 sqm = 125 cu, followed by one pickup order of 10 sqm = 2 cu */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(2, 5));
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testInfeasiblePickupAfterDeliveryAndPickup(Vessel vessel) {
        /* Two deliveries totalling 125 cu, then two pickups of 126 cu: total load at last order > vessel capacity */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(2, 6));
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testFeasibleDeliveryPickupDelivery(Vessel vessel) {
        /* Two delivery orders of total 500 + 490 sqm = 125 cu, with pickup order of 490 sqm = 62 cu in between */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(6, 9));
        assertTrue(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }

    private void testInfeasiblePickupAtEnd(Vessel vessel) {
        /* Two deliveries total 125 cu, pickup order of 62 cu in between, final pickup of 63 cu exceeding capacity */
        LinkedList<Order> orderSequence = new LinkedList<>(Problem.orders.subList(6, 10));
        assertFalse(Evaluator.isFeasibleLoadSequence(orderSequence, vessel));
    }


    @Test
    @DisplayName("test isFeasibleDuration")
    public void isFeasibleDurationTest() {
        Problem.setUpProblem("manyOrders.json", true, 10);
        testInfeasibleDuration();
        testFeasibleDuration();
    }

    private void testInfeasibleDuration() {
        /* Too many orders to service within the return time */
        List<Order> orderSequence = new LinkedList<>(Problem.orders);
        assertFalse(Evaluator.isFeasibleDurationSequence(orderSequence));
    }

    private void testFeasibleDuration() {
        /* Few enough orders to service within the return time */
        List<Order> orderSequence = new LinkedList<>(Problem.orders.subList(0, 11));
        assertTrue(Evaluator.isFeasibleDurationSequence(orderSequence));
    }


    @Test
    @DisplayName("test isInstInMoreThanOneSequence")
    public void isInstInMoreThanOneSequenceTest() {
        Problem.setUpProblem("allOrderCombos.json", true, 10);

        // Check valid
        List<List<Integer>> validInstSequences = getValidInstSequences();
        assertFalse(Evaluator.instInMoreThanOneSequence(validInstSequences));

        // Check invalid (one installation in different sequences)
        List<List<Integer>> invalidSequences = getInvalidInstSequencesOneInst();
        assertTrue(Evaluator.instInMoreThanOneSequence(invalidSequences));

        // Check invalid (two installations in different sequences)
        invalidSequences = getInvalidInstSequencesTwoInst(invalidSequences);
        assertTrue(Evaluator.instInMoreThanOneSequence(invalidSequences));
    }

    private List<List<Integer>> getValidInstSequences() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Problem.orders.subList(0, 3)));
        orderSequences.add(new LinkedList<>(Problem.orders.subList(3, 7)));
        orderSequences.add(new LinkedList<>(Problem.orders.subList(7, 9)));
        return Helpers.getInstIdSequences(orderSequences);
    }

    private List<List<Integer>> getInvalidInstSequencesOneInst() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Problem.orders.subList(0, 2)));
        orderSequences.add(new LinkedList<>(Problem.orders.subList(3, 7)));
        List<Order> dealBreaker = Problem.orders.subList(7, 9);
        dealBreaker.add(Problem.getOrder(2));
        orderSequences.add(new LinkedList<>(dealBreaker));
        return Helpers.getInstIdSequences(orderSequences);
    }

    private List<List<Integer>> getInvalidInstSequencesTwoInst(List<List<Integer>> invalidSequences) {
        List<List<Integer>> invalidSequencesCopy = Helpers.deepCopy2DList(invalidSequences);
        invalidSequencesCopy.get(0).add(invalidSequencesCopy.get(1).remove(1));
        return invalidSequencesCopy;
    }

    @Test
    @DisplayName("test isIllegalPattern")
    public void isIllegalPatternTest() {
        Problem.setUpProblem("allOrderCombos.json", true, 10);

        testVariousVisitOrders();
        testSpread();
        testBothVisitOrderAndSpread();
    }

    private void testVariousVisitOrders() {
        List<Order> orderSequence = new LinkedList<>(Problem.orders);

        // Check valid (All combos present MD-OD-OP, MD-OD, MD-OP, OD-OP)
        assertFalse(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstIdSequence(orderSequence)));

        // Check illegal visit order: OD-MD
        Collections.swap(orderSequence, 3, 4);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstIdSequence(orderSequence)));

        // Check illegal visit order: OP-MD
        Collections.swap(orderSequence, 3, 4);
        Collections.swap(orderSequence, 5, 6);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstIdSequence(orderSequence)));

        // Check illegal visit order: OP-OD
        Collections.swap(orderSequence, 5, 6);
        Collections.swap(orderSequence, 7, orderSequence.size() - 1);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstIdSequence(orderSequence)));
    }

    private void testSpread() {
        List<Order> orderSequence = new LinkedList<>(Problem.orders);

        Collections.swap(orderSequence, 7, orderSequence.size() - 1);
        orderSequence.add(orderSequence.remove(1));
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstIdSequence(orderSequence)));
    }

    private void testBothVisitOrderAndSpread() {
        List<Order> orderSequence = new LinkedList<>(Problem.orders);

        orderSequence.add(1, orderSequence.remove(orderSequence.size() - 1));
        Collections.swap(orderSequence, 0, 2);
        orderSequence.add(0, orderSequence.remove(3));
        Collections.swap(orderSequence, 6, 7);
        assertTrue(Evaluator.isIllegalPattern(orderSequence, Helpers.getInstIdSequence(orderSequence)));
    }
}
