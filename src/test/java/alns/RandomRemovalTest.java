package alns;

import alns.heuristics.RandomRemoval;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class RandomRemovalTest {

    @Test
    @DisplayName("test RandomRemoval")
    public void randomRemovalTest() {
        Problem.setUpProblem("basicTestData.json", true);
        Solution solution = createSolution();
        int numberOfOrders = 2;
        int counter = 0;

        List<List<Order>> orderSequences = solution.getOrderSequences();
        for (List<Order> orderSequence : orderSequences) {
            for (Order order : orderSequence) counter++;
        }

        int ordersBefore = counter;
        counter = 0;

        RandomRemoval randomRemoval = new RandomRemoval("random", true, false);

        Set<Order> ordersToRemove = randomRemoval.findOrdersToRemove(solution, numberOfOrders);
        Solution partialSolutionOne = randomRemoval.destroy(solution, ordersToRemove);
        List<List<Order>> newOrderSequencesOne = partialSolutionOne.getOrderSequences();
        for (List<Order> orderSequence : newOrderSequencesOne) {
            for (Order order : orderSequence) counter++;
        }

        int ordersAfter = counter;

        assertEquals(ordersBefore,ordersAfter + numberOfOrders);

        Set<Order> ordersToRemoveTwo = randomRemoval.findOrdersToRemove(partialSolutionOne, 0);
        Solution partialSolutionTwo = randomRemoval.destroy(partialSolutionOne, ordersToRemoveTwo);
        assertEquals(partialSolutionOne, partialSolutionTwo);
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
