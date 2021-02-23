package subproblem;

import objects.Order;

import java.util.*;

public class Tree {
    // Variable holding root node
    // Tree search (BFS) to find shortest path
    // Generate tree (calling ArcGeneration to find cost)
    // Solve subproblem (or a separate subproblem class)

    private Node root;
    private List<Node> shortestPath;
    private double globalBestCost;

    public Tree() {
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    // TODO: Implement (KP)
    public List<Node> findShortestPath() {
        LinkedList<Node> queue = initialize();

        // TODO: This can be done when creating the root node
        List<Node> rootPath = new ArrayList<>();
        rootPath.add(this.root);
        this.root.setBestPath(rootPath);
        this.root.setBestCost(0.0);

        while (queue.size() != 0) {
            Node currentNode = queue.removeFirst();
            Set<Node> setOfChildren = currentNode.getChildren(); // Retrieved in order right to left in tree
            if (currentNode != this.root) updateCurrentBest(currentNode);
            if(setOfChildren.isEmpty()) updateGlobalBest(currentNode);
            for (Node child : setOfChildren) {
                if (!child.isVisited()) {
                    child.setVisited(true);
                    queue.add(child);
                }
            }
        }
        return shortestPath;
    }

    private LinkedList<Node> initialize() {
        LinkedList<Node> queue = new LinkedList<>();
        this.shortestPath = new LinkedList<>();
        this.globalBestCost = Double.POSITIVE_INFINITY;
        queue.add(this.root);
        this.root.setVisited(true);
        return queue;
    }

    private void updateGlobalBest(Node currentNode) {
        if(currentNode.getBestCost() < this.globalBestCost) {
            this.globalBestCost = currentNode.getBestCost();
            this.shortestPath = deepCopy(currentNode.getBestPath());
        }
    }

    private void updateCurrentBest(Node currentNode) {
        currentNode.setBestCost(Double.POSITIVE_INFINITY);
        for (Node parent : currentNode.getParents()) {
            double currentCost = parent.getBestCost() + parent.getCostOfChild(currentNode);
            if (currentCost < currentNode.getBestCost()) {
                currentNode.setBestCost(currentCost);
                List<Node> path = deepCopy(parent.getBestPath());  // TODO: Research best way to copy
                path.add(currentNode);
                currentNode.setBestPath(path);
            }
        }
    }

    private List<Node> deepCopy(List<Node> original) {
        return new ArrayList<>(original);
    }

    // TODO: Implement (Anders)
    public void generateTree(List<Order> orderSequence) {

    }

    private Node createDummyTree() {
        Node node0 = new Node("Root", 0);
        Node node1 = new Node("1", 10);
        Node node2 = new Node("2", 11);
        Node node3 = new Node("3", 12);
        Node node4 = new Node("4", 13);
        Node node5 = new Node("5", 14);
        Node node6 = new Node("6", 15);
        Node node7 = new Node("7", 16);
        Node node8 = new Node("8", 17);

        node1.addParent(node0);
        node2.addParent(node0);
        node3.addParent(node1);
        node4.addParent(node1);
        node4.addParent(node2);
        node5.addParent(node2);
        node6.addParent(node3);
        node6.addParent(node4);
        node7.addParent(node4);
        node8.addParent(node4);
        node8.addParent(node5);

        node0.addChild(node1);
        node0.addChild(node2);
        node1.addChild(node3);
        node1.addChild(node4);
        node2.addChild(node4);
        node2.addChild(node5);
        node3.addChild(node6);
        node4.addChild(node6);
        node4.addChild(node7);
        node4.addChild(node8);
        node5.addChild(node8);

        node0.setChildToCost(node1,1.0);
        node0.setChildToCost(node2,2.0);
        node1.setChildToCost(node3,2.0);
        node1.setChildToCost(node4,1.0);
        node2.setChildToCost(node4,2.0);
        node2.setChildToCost(node5,2.0);
        node3.setChildToCost(node6,2.0);
        node4.setChildToCost(node6,1.0);
        node4.setChildToCost(node7,2.0);
        node4.setChildToCost(node8,2.0);
        node5.setChildToCost(node8,2.0);

        return node0;
    }

    public static void main(String[] args) {
        Tree tree = new Tree();
        Node root = tree.createDummyTree();
        tree.setRoot(root);
        tree.findShortestPath();

    }
}
