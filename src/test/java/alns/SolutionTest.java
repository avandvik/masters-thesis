package alns;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

public class SolutionTest {

    @Test
    @DisplayName("test Constructor")
    public void testConstructor() {
        Problem.setUpProblem("basicTestData.json", true);
        List<List<Order>> orderSequences = new ArrayList<>();



    }

    @Test
    @DisplayName("test getInstSequences")
    public void testGetInstSequences() {

    }
}
