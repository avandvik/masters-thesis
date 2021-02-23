package subproblem;

import objects.Order;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    // TODO: Implement (KP)
    public void breadthFirstSearch(Node root) {
        LinkedList<Node> queue = new LinkedList<>();

        queue.add(root);
        root.setVisited(true);

        while(queue.size() != 0) {
            Node s = queue.removeFirst();
            Set<Node> setOfChildren = s.getChildren();

            for(Node child : setOfChildren) {
                if(!child.isVisited()) {
                    child.setVisited(true);
                    queue.add(child);
                }
            }
        }

    }

    // TODO: Implement (Anders)
    public void generateTree(List<Order> orderSequence) {

    }

    private void createDummyTree() {
    }

    public static void main(String[] args) {
        /*Tree tree = new Tree();
        Node node1 = new Node("Root",0);
        Node node2 = new Node("A",10);
        Node node3 = new Node("B",11);
        Node node4 = new Node("C",12);
        Node node5 = new Node("D",13);
        Node node6 = new Node("E",14);
        Node node7 = new Node("F",15);
        Node node8 = new Node("G",16);
        Node node9 = new Node("H",17);
        node1.addChild(node2);
        node1.addChild(node3);
        node2.addChild(node4);
        node2.addChild(node5);
        node3.addChild(node6);
        node3.addChild(node7);
        node4.addChild(node8);
        node4.addChild(node9);
        tree.breadthFirstSearch(node1);*/
    }
}
