package alns;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RandomRemovalTest {

    @Test
    @DisplayName("test RandomRemoval")
    public void randomRemovalTest() {
        Problem.setUpProblem("example.json", false);
        Solution solution = createSolution();
        int numberOfOrders = 2;
        int counter = 0;

        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (List<Order> orderSequence : orderSequences) {
            for (Order order : orderSequence) counter++;
        }

        int ordersBefore = counter;
        counter = 0;

        Solution newSolutionOne = RandomRemoval.removeRandomOrders(solution, numberOfOrders);
        List<List<Order>> newOrderSequencesOne = newSolutionOne.getOrderSequences();
        for (List<Order> orderSequence : newOrderSequencesOne) {
            for (Order order : orderSequence) counter++;
        }

        int ordersAfter = counter;

        assertEquals(ordersBefore,ordersAfter + numberOfOrders);

        Solution newSolutionTwo = RandomRemoval.removeRandomOrders(newSolutionOne,0);
        assertEquals(newSolutionOne, newSolutionTwo);

    }

    private Solution createSolution() {
        List<Order> orders = Problem.orders;
        List<List<Order>> orderSequences = new ArrayList<>();
        for (int i = 0; i < Problem.getNumberOfVessels(); i++) orderSequences.add(new LinkedList<>());

        for (int i = 0; i < 2; i++) {
            orderSequences.get(0).add(orders.get(i));
        }
        for (int j = 2; j < 5; j++) {
            orderSequences.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 7; k++) {
            orderSequences.get(2).add(orders.get(k));
        }

        return new Solution(orderSequences);
    }
}
