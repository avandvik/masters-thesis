import alns.Evaluator;
<<<<<<< HEAD
=======
import alns.Solution;
>>>>>>> e5240d6db585c5ba027f05c699ca090ac30438f2
import data.Problem;
import objects.Order;
import objects.Vessel;
import org.junit.Test;
<<<<<<< HEAD

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

=======
import static org.junit.Assert.*;

import java.util.List;

>>>>>>> e5240d6db585c5ba027f05c699ca090ac30438f2
public class EvaluatorTest {

    @Test
    public void testIsFeasibleLoad() {
<<<<<<< HEAD
        Problem.setUpProblem("load/load.json", true);

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
        assertFalse(Evaluator.isFeasibleLoad(orders, vessel));

        // Testing for one delivery order of 1000 sqm = 125 cu: total delivery load = vessel capacity
        assertTrue(Evaluator.isFeasibleLoad(orderSequenceOne, vessel));
        // Testing for two delivery order of total 510 sqm = 64 cu: total delivery load < vessel capacity
        assertTrue(Evaluator.isFeasibleLoad(orderSequenceTwo, vessel));
        // Testing for two delivery orders of 1010 sqm = 127 cu: total delivery load > vessel capacity
        assertFalse(Evaluator.isFeasibleLoad(orderSequenceThree, vessel));

        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, followed by one pickup order of 10 sqm = 2 cu
        assertTrue(Evaluator.isFeasibleLoad(orderSequenceFour, vessel));
        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, followed by two pickup order of total
        // 10 + 990 sqm = 126 cu: total load at last order > vessel capacity
        assertFalse(Evaluator.isFeasibleLoad(orderSequenceFive, vessel));

        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, with pickup order of 490 sqm = 62 cu in between
        assertTrue(Evaluator.isFeasibleLoad(orderSequenceSix, vessel));
        // Testing for two delivery orders of total 500 + 490 sqm = 125 cu, with pickup order of 490 sqm = 62 cu in between,
        // and one pickup order of 500 sqm = 63 cu at the end: total load at last order > vessel capacity
        assertFalse(Evaluator.isFeasibleLoad(orderSequenceSeven, vessel));
    }

    @Test
    public void testIsFeasibleDuration() {
        Problem.setUpProblem("duration/duration_long.json", true);
        List<Order> ordersOne = Problem.orders;
        List<Order> orderSequenceOne = new LinkedList<>();
        orderSequenceOne.addAll(ordersOne);
        assertFalse(Evaluator.isFeasibleDuration(orderSequenceOne));

        Problem.setUpProblem("duration/duration_short.json", true);
        List<Order> ordersTwo = Problem.orders;
        List<Order> orderSequenceTwo = new LinkedList<>();
        orderSequenceOne.addAll(ordersTwo);
        assertTrue(Evaluator.isFeasibleDuration(orderSequenceTwo));
    }

    @Test
    public void testIsInstInMoreThanOneSequence() {
        Problem.setUpProblem("visitOrder/instInMoreThanOneSequence.json", true);

        List<Order> ordersOne = Problem.orders;
        List<Integer> installationsOne = new LinkedList<>();
        for (int i = 0; i < ordersOne.size(); i++) installationsOne.add(ordersOne.get(i).getInstallationId());

        List<List<Integer>> instSequencesOne = new ArrayList<List<Integer>>();
        List<Integer> instSequenceOne = new LinkedList<>();
        List<Integer> instSequenceTwo = new LinkedList<>();
        for (int j = 0; j < installationsOne.size() - 1; j += 2) {
            instSequenceOne.add(installationsOne.get(j));
            instSequenceTwo.add(installationsOne.get(j + 1));
        }
        instSequencesOne.add(instSequenceOne);
        instSequencesOne.add(instSequenceTwo);

        assertTrue(Evaluator.instInMoreThanOneSequence(instSequencesOne));

    }

    @Test
    public void testIsIllegalPattern() {
        Problem.setUpProblem("visitOrder/illegalPattern.json", true);
        List<Order> orders = Problem.orders;
        List<Order> orderSequenceThree = new LinkedList<>();
        for (int j = 0; j < orderSequenceThree.size(); j++) orderSequenceThree.add(orders.get(j));
        List<Integer> instSequenceThree = new LinkedList<>();
        for (int i = 0; i < orders.size(); i++) instSequenceThree.add(orders.get(i).getInstallationId());

        assertFalse(Evaluator.isIllegalPattern(orderSequenceThree,instSequenceThree));
    }
}
=======
        Problem.setUpProblem("evaluatorTest.json");

        List<Order> orderSequenceOne = Problem.orders;
        List<Order> orderSequenceTwo = Problem.orders.subList(0, 3);
        Vessel vessel = Problem.getVessel(0);

        assertFalse(Evaluator.isFeasibleLoad(orderSequenceOne, vessel));
        assertFalse(Evaluator.isFeasibleLoad(orderSequenceTwo, vessel));

        Problem.

    }
}
>>>>>>> e5240d6db585c5ba027f05c699ca090ac30438f2
