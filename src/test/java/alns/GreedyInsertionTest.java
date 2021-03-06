package alns;

import alns.heuristics.GreedyInsertion;
import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GreedyInsertionTest {

    @Test
    @DisplayName("test GreedyInsertion")
    public void GreedyInsertionTest() {
        Problem.setUpProblem("example.json", false);
        List<Order> orders = Problem.orders;
        List<List<Order>> expectedOrderSequencesOne = new ArrayList<>();
        List<List<Order>> partialOrderSequencesOne = new ArrayList<>();
        List<List<Order>> partialOrderSequencesTwo = new ArrayList<>();
        List<List<Order>> expectedOrderSequencesTwo = new ArrayList<>();

        for (int i = 0; i < Problem.getNumberOfVessels(); i++) expectedOrderSequencesOne.add(new LinkedList<>());
        for (int j = 0; j < Problem.getNumberOfVessels(); j++) expectedOrderSequencesTwo.add(new LinkedList<>());
        for (int k = 0; k < Problem.getNumberOfVessels(); k++) partialOrderSequencesOne.add(new LinkedList<>());
        for (int m = 0; m < Problem.getNumberOfVessels(); m++) partialOrderSequencesTwo.add(new LinkedList<>());

        for (int i = 0; i < 3; i++) {
            expectedOrderSequencesOne.get(0).add(orders.get(i));
            // Removing first order from first partial order sequences (one possible insertion)
            if (i > 0) partialOrderSequencesOne.get(0).add(orders.get(i));
            // Removing third order from first partial order sequences (eight possible insertions)
            if (i != 2) partialOrderSequencesTwo.get(0).add(orders.get(i));
        }
        for (int j = 3; j < 5; j++) {
            expectedOrderSequencesOne.get(1).add(orders.get(j));
            partialOrderSequencesOne.get(1).add(orders.get(j));
            partialOrderSequencesTwo.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 8; k++) {
            expectedOrderSequencesOne.get(2).add(orders.get(k));
            partialOrderSequencesOne.get(2).add(orders.get(k));
            partialOrderSequencesTwo.get(2).add(orders.get(k));
        }

        GreedyInsertion greedyInsertion = new GreedyInsertion("greedy", false, true);

        Solution expectedSolution = new Solution(expectedOrderSequencesOne);
        Solution partialSolution = new Solution(partialOrderSequencesOne);
        Order orderToBePlaced = Problem.orders.get(0);
        Solution newSolution = greedyInsertion.getGreedyInsertion(partialSolution,orderToBePlaced);
        assertEquals(expectedSolution,newSolution);

        // Order to be placed has eight possible insertions. Expected to be put first in first sequence/vessel.
        expectedOrderSequencesTwo.get(0).add(orders.get(2));
        for (int i = 0; i < 3; i++) {
            if (i != 2) expectedOrderSequencesTwo.get(0).add(orders.get(i));
        }
        for (int j = 3; j < 5; j++) {
            expectedOrderSequencesTwo.get(1).add(orders.get(j));
        }
        for (int k = 5; k < 8; k++) {
            expectedOrderSequencesTwo.get(2).add(orders.get(k));
        }

        Solution expectedSolutionTwo = new Solution(expectedOrderSequencesTwo);
        Solution partialSolutionTwo = new Solution(partialOrderSequencesTwo);
        Order orderToBePlacedTwo = Problem.orders.get(2);
        Solution newSolutionTwo = greedyInsertion.getGreedyInsertion(partialSolutionTwo, orderToBePlacedTwo);
        assertEquals(expectedSolutionTwo,newSolutionTwo);
    }
}
