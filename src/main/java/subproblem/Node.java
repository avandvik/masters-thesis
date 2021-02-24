package subproblem;

import objects.Order;

import java.util.*;

public class Node implements Comparable<Node> {

    private String nodeName;
    private Order order;
    private Set<Node> parents = new HashSet<>();
    private Set<Node> children = new HashSet<>();
    private final int discreteTime;

    private Map<Node, Double> childToCost = new HashMap<>();

    private List<Node> bestPath = new LinkedList<>();
    private double bestCost = -1;
    private boolean isVisited;

    public Node(String nodeName, Set<Node> parents, Set<Node> children, int discreteTime) {
        this.nodeName = nodeName;
        this.parents = parents;
        this.children = children;
        this.discreteTime = discreteTime;
        this.isVisited = false;
    }

    public Node(String nodeName, int discreteTime) {
        this.nodeName = nodeName;
        this.discreteTime = discreteTime;
        this.isVisited = false;
    }

    public Node(Order order, int discreteTime) {
        this.order = order;
        this.discreteTime = discreteTime;
    }

    public List<Node> getBestPath() {
        return bestPath;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Order getOrder() {
        return order;
    }

    public double getBestCost() {
        return bestCost;
    }

    public int getDiscreteTime() {
        return discreteTime;
    }

    public Set<Node> getParents() {
        return parents;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public double getCostOfChild(Node child) {
        return this.childToCost.get(child);
    }

    public void setChildToCost(Node child, double cost) {
        this.childToCost.put(child, cost);
    }

    public void setBestPath(List<Node> bestPath) {
        this.bestPath = bestPath;
    }

    public void setBestCost(double bestCost) {
        this.bestCost = bestCost;
    }

    public void addParent(Node parent) {
        this.parents.add(parent);
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    @Override
    public String toString() {
        return "(" + this.nodeName + ", " + getDiscreteTime() + ")";
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(getBestCost(), o.bestCost);
        // Returns -1 if cost of o is higher, 0 if equal and 1 if lower
    }
}
