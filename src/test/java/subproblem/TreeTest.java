package subproblem;

import data.Problem;
import objects.Order;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

public class TreeTest {

    @Test
    @DisplayName("Test generateTree")
    public void testGenerateTree() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        LinkedList<Order> orderSequence = new LinkedList<>();
        for (int i = 0; i < 5; i++) orderSequence.add(Problem.orders.get(i));
        Tree tree = new Tree(0);  // 0 for fleet vessel
        tree.generateTree(orderSequence, false);

        for (Node node : tree.getNodes()) {
            if (node.getOrder() != null) {
                // 1. Check that all children have current node as parent
                for (Node child : node.getChildren()) assertTrue(child.getParents().contains(node));

                // 2. Check that the current nodes parents have the current node as child
                for (Node parent : node.getParents()) assertTrue(parent.getChildren().contains(node));

                // 3. Check that all internal nodes have at least one parent and one child
                assertTrue(node.getChildren().size() > 0);
                assertTrue(node.getParents().size() > 0);
            } else {
                // 4. Check root and leaf nodes
                assertTrue((node.getParents().size() == 0 && node.getChildren().size() > 0)
                        || (node.getParents().size() > 0 && node.getChildren().size() == 0));
            }
        }

        Order lastOrder = orderSequence.getLast();
        int numberOfRoots = 0;
        int numberOfLeaves = 0;
        int numberOfNodesLastOrder = 0;
        for (Node node : tree.getNodes()) {
            if (node.getParents().size() == 0) numberOfRoots++;
            if (node.getChildren().size() == 0) numberOfLeaves++;
            if (node.getOrder() != null && node.getOrder().equals(lastOrder)) numberOfNodesLastOrder++;
        }

        // 5. Check that there is only one root node
        assertEquals(1, numberOfRoots);

        // 6. Check that there is one leaf node for each lastLayer node
        assertEquals(numberOfNodesLastOrder, numberOfLeaves);
    }

    @Test
    @DisplayName("Test findShortestPath")
    public void testFindShortestPath() {
        Problem.setUpProblem("basicTestData.json", true, 10);
        LinkedList<Order> orderSequence = new LinkedList<>();
        for (int i = 0; i < 5; i++) orderSequence.add(Problem.orders.get(i));
        Tree tree = new Tree(0);  // Fleet vessel
        tree.generateTree(orderSequence, false);
        List<Node> shortestPath = tree.findShortestPath();
        Node firstNode = shortestPath.get(0);
        assertNull(firstNode.getOrder());
        assertEquals(63, firstNode.getDiscreteTime());
        Node secondNode = shortestPath.get(1);
        assertEquals(orderSequence.get(0), secondNode.getOrder());
        assertEquals(133, secondNode.getDiscreteTime());
        Node thirdNode = shortestPath.get(2);
        assertEquals(orderSequence.get(1), thirdNode.getOrder());
        assertEquals(134, thirdNode.getDiscreteTime());
        Node fourthNode = shortestPath.get(3);
        assertEquals(orderSequence.get(2), fourthNode.getOrder());
        assertEquals(180, fourthNode.getDiscreteTime());
        Node fifthNode = shortestPath.get(4);
        assertEquals(orderSequence.get(3), fifthNode.getOrder());
        assertEquals(211, fifthNode.getDiscreteTime());
        Node sixthNode = shortestPath.get(5);
        assertEquals(orderSequence.get(4), sixthNode.getOrder());
        assertEquals(217, sixthNode.getDiscreteTime());
        Node seventhNode = shortestPath.get(6);
        assertNull(seventhNode.getOrder());
        assertEquals(236, seventhNode.getDiscreteTime());
    }
}
