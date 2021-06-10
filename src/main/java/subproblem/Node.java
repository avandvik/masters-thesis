package subproblem;

import objects.Order;

import java.util.*;

public class Node implements Comparable<Node> {

    private final Order order;
    private final Set<Node> parents = new HashSet<>();
    private final Set<Node> children = new HashSet<>();

    private final Map<Node, List<Integer>> parentToTimePoints = new HashMap<>();  // arrTime, serviceTime, discreteTime
    private final Map<Node, Double> parentToSpeed = new HashMap<>();
    private final int discreteTime;  // The end time of incoming arc, start time of outgoing arc

    private final Map<Node, Double> childToCost = new HashMap<>();

    private List<Node> bestPath = new LinkedList<>();
    private double bestCost = Double.POSITIVE_INFINITY;
    private boolean isVisited = false;

    public Node(Order order, Node parent, List<Integer> timePoints, double speed) {
        this.order = order;
        if (parent != null) this.parents.add(parent);
        this.discreteTime = timePoints.get(2);
        this.parentToTimePoints.put(parent, timePoints);  // First depot node will have null as parent
        this.parentToSpeed.put(parent, speed);  // First depot node will have null as parent and 0.0 as speed
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

    public int getArrTime(Node parent) {
        return parentToTimePoints.get(parent).get(0);
    }

    public int getServiceStartTime(Node parent) {
        return parentToTimePoints.get(parent).get(1);
    }

    public int getDiscreteTime() {
        return discreteTime;
    }

    public double getSpeed(Node parent) {
        return parentToSpeed.get(parent);
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

    public void setBestCost(double bestCost) {
        this.bestCost = bestCost;
    }

    public void addParent(Node parent) {
        this.parents.add(parent);
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public void setParentToTimePoints(Node parent, List<Integer> timePoints) {
        this.parentToTimePoints.put(parent, timePoints);
    }

    public void setParentToSpeed(Node parent, Double speed) {
        this.parentToSpeed.put(parent, speed);
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
