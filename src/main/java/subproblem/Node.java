package subproblem;

import objects.Order;

import java.util.*;

public class Node implements Comparable<Node> {

    private Order order;
    private Set<Node> parents = new HashSet<>();
    private Set<Node> children = new HashSet<>();
    private final int arrTime;
    private final int serviceStartTime;
    private final int discreteTime;  // The end time of incoming arc, start time of outgoing arc

    private Map<Node, Double> childToCost = new HashMap<>();

    private List<Node> bestPath = new LinkedList<>();
    private double bestCost = Double.POSITIVE_INFINITY;
    private boolean isVisited = false;

    public Node(Order order, Node parent, List<Integer> timePoints) {
        this.order = order;
        if (parent != null) this.parents.add(parent);
        this.arrTime = timePoints.get(0);
        this.serviceStartTime = timePoints.get(1);
        this.discreteTime = timePoints.get(2);
    }

    public List<Node> getBestPath() {
        return bestPath;
    }

    public Order getOrder() {
        return order;
    }

    public double getBestCost() {
        return bestCost;
    }

    public int getArrTime() {
        return arrTime;
    }

    public int getServiceStartTime() {
        return serviceStartTime;
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

    public boolean hasChild(Node child) {
        return children.contains(child);
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

    public void addToBestPath(Node node) {
        if (!this.bestPath.contains(node)) this.bestPath.add(node);
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
        return "(" + (this.order == null ? "Depot" : this.order) + ", " + getDiscreteTime() + ")";
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(getBestCost(), o.bestCost);
        // Returns -1 if cost of o is higher, 0 if equal and 1 if lower
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return discreteTime == node.discreteTime && order.equals(node.order);
    }
}
