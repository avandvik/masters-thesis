package subproblem;

import data.Problem;
import objects.Order;

import java.util.LinkedList;
import java.util.List;

public class Tree {
    // Variable holding root node
    // Tree search (BFS) to find shortest path
    // Generate tree (calling ArcGeneration to find cost)
    // Solve subproblem (or a separate subproblem class)

    private Node root;
    private List<Node> nodes;

    public Tree() {
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    // TODO: Implement (KP)
    public List<Node> findShortestPath() {
        List<Node> shortestPath = new LinkedList<>();
        return shortestPath;
    }

    // TODO: Implement (Anders)
    public void generateTree(LinkedList<Order> orderSequence, boolean isSpotVessel) {
        // Depot to first order
        Order firstOrder = orderSequence.getFirst();
        ArcGeneration.generateArcsFromDepotToOrder(firstOrder, isSpotVessel);

        // Order to order

        // Order to depot

    }

    private void createDummyTree() {

    }

    private List<Order> createDummyOrderSequence() {
        List<Order> orderSequence = new LinkedList<>();
        Order orderOne = new Order(1, true, true, 100, 1);
        Order orderTwo = new Order(2, false, true, 100, 1);
        Order orderThree = new Order(3, false, false, 100, 5);
        Order orderFour = new Order(4, true, true, 100, 10);
        orderSequence.add(orderOne);
        orderSequence.add(orderTwo);
        orderSequence.add(orderThree);
        orderSequence.add(orderFour);
        return orderSequence;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        Tree tree = new Tree();
        LinkedList<Order> orderSequence = (LinkedList<Order>) tree.createDummyOrderSequence();
        boolean isSpotVessel = false;
        tree.generateTree(orderSequence, isSpotVessel);
    }
}
