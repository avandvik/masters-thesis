package subproblem;

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
    public void generateTree(List<Order> orderSequence) {

    }

    private void createDummyTree() {

    }

    public static void main(String[] args) {
        Tree tree = new Tree();
        tree.createDummyTree();
        tree.findShortestPath();
    }
}
