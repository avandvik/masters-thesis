import alns.Evaluator;
import alns.Solution;
import data.Problem;
import objects.Order;
import objects.Vessel;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class EvaluatorTest {

    @Test
    public void testIsFeasibleLoad() {
        Problem.setUpProblem("evaluatorTest.json");

        List<Order> orderSequenceOne = Problem.orders;
        List<Order> orderSequenceTwo = Problem.orders.subList(0, 3);
        Vessel vessel = Problem.getVessel(0);

        assertTrue(Evaluator.isFeasibleLoad(orderSequenceOne, vessel));
        assertTrue(Evaluator.isFeasibleLoad(orderSequenceTwo, vessel));
    }
}
