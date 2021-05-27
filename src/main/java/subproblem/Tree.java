package subproblem;

import arcs.ArcGenerator;
import data.Constants;
import data.Problem;
import objects.Installation;
import objects.Order;
import objects.Vessel;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class Tree {

    private Node root;
    private final List<Node> nodes = new ArrayList<>();
    private List<Node> shortestPath;
    private double globalBestCost;

    private final int vIdx;

    public Tree(int vesselIdx) {
        this.vIdx = vesselIdx;
    }

    private void setRoot(Node root) {
        this.root = root;
        this.root.setBestCost(0.0);
        this.root.setBestPath(new ArrayList<>(Collections.singletonList(this.root)));
    }

    private void addNode(Node node) {
        Node existingNode = getExistingNode(node.getDiscreteTime(), node.getOrder());
        if (existingNode == null) {
            this.nodes.add(node);
        }
    }

    private Node getExistingNode(int time, Order order) {
        for (Node node : this.nodes) {
            if (node.getDiscreteTime() == time) {
                if (node.getOrder() == null && order == null) {
                    return node;
                } else if (node.getOrder() != null && node.getOrder().equals(order)) {
                    return node;
                }
            }
        }
        return null;
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    private List<Node> getNodesExclDepot() {
        return this.nodes.subList(1, this.nodes.size());
    }

    protected double getGlobalBestCost() {
        return globalBestCost;
    }

    protected List<Node> findShortestPath() {
        List<Node> queue = initialize();
        while (queue.size() != 0) {
            Node currentNode = ((LinkedList<Node>) queue).removeFirst();
            Set<Node> setOfChildren = currentNode.getChildren();
            if (setOfChildren.isEmpty()) updateGlobalBest(currentNode);
            for (Node childNode : setOfChildren) {
                updateChildBest(currentNode, childNode);
                if (!childNode.isVisited()) {
                    childNode.setVisited(true);
                    queue.add(childNode);
                }
            }
        }
        return this.shortestPath;
    }

    private List<Node> initialize() {
        List<Node> queue = new LinkedList<>();
        this.shortestPath = new LinkedList<>();
        this.globalBestCost = Double.POSITIVE_INFINITY;
        queue.add(this.root);
        this.root.setVisited(true);
        return queue;
    }

    private void updateGlobalBest(Node currentNode) {
        if (currentNode.getBestCost() < this.globalBestCost) {
            this.globalBestCost = currentNode.getBestCost();
            this.shortestPath = Helpers.deepCopyList(currentNode.getBestPath(), false);
        }
    }

    private void updateChildBest(Node currentNode, Node childNode) {
        double costFromCurrentNode = currentNode.getBestCost() + currentNode.getCostOfChild(childNode);
        if (costFromCurrentNode < childNode.getBestCost()) {
            childNode.setBestCost(costFromCurrentNode);
            List<Node> path = Helpers.deepCopyList(currentNode.getBestPath(), false);
            path.add(childNode);
            childNode.setBestPath(path);
        }
    }

    protected void generateTree(List<Order> orderSequence, boolean isSpotVessel) {
        Order firstOrder = ((LinkedList<Order>) orderSequence).getFirst();
        this.generateNodesDepotToOrder(firstOrder, isSpotVessel);
        List<Node> queue = Helpers.deepCopyList(this.getNodesExclDepot(), false);
        Set<Node> addedNodes = new HashSet<>();
        while (!queue.isEmpty()) {
            Node fromNode = queue.remove(0);
            Order toOrder = Helpers.getNextElement((LinkedList<Order>) orderSequence, fromNode.getOrder());
            if (toOrder == null) break;
            this.generateNodesOrderToOrder(fromNode, toOrder, isSpotVessel);
            List<Node> newNodes = getNewNodes(addedNodes, toOrder);
            addedNodes.addAll(newNodes);
            queue.addAll(newNodes);
        }
        List<Node> lastLayer = getLastLayer(((LinkedList<Order>) orderSequence).getLast(), this.getNodesExclDepot());
        for (Node lastNode : lastLayer) generateNodesOrderToDepot(lastNode, isSpotVessel);
    }

    private List<Node> getNewNodes(Set<Node> addedNodes, Order toOrder) {
        List<Node> newNodes = new ArrayList<>();
        for (Node node : this.getNodesExclDepot()) {
            if (node.getOrder().equals(toOrder) && !addedNodes.contains(node)) {
                newNodes.add(node);
            }
        }
        return newNodes;
    }

    private List<Node> getLastLayer(Order lastOrder, List<Node> nodes) {
        List<Node> lastLayer = new ArrayList<>();
        for (Node node : nodes) {
            if (node.getOrder().equals(lastOrder)) lastLayer.add(node);
        }
        return lastLayer;
    }

    private void generateNodesDepotToOrder(Order firstOrder, boolean isSpotVessel) {
        Installation depot = Problem.getDepot();
        double distance = DistanceCalculator.distance(depot, firstOrder, "N");
        int startTime = Problem.preparationEndTime;
        createNodes(null, firstOrder, isSpotVessel, distance, startTime, 1);
    }

    private void generateNodesOrderToOrder(Node fromNode, Order toOrder, boolean isSpotVessel) {
        Order fromOrder = fromNode.getOrder();
        int startTime = fromNode.getDiscreteTime();
        double distance = DistanceCalculator.distance(fromOrder, toOrder, "N");
        createNodes(fromNode, toOrder, isSpotVessel, distance, startTime, 2);
    }

    private void generateNodesOrderToDepot(Node fromNode, boolean isSpotVessel) {
        Installation depot = Problem.getDepot();
        int startTime = fromNode.getDiscreteTime();
        double distance = DistanceCalculator.distance(depot, fromNode.getOrder(), "N");
        createNodes(fromNode, null, isSpotVessel, distance, startTime, 3);
    }

    private void createNodes(Node fromNode, Order toOrder, boolean isSpot, double dist, int startTime, int c) {
        List<Double> speeds = ArcGenerator.getSpeeds(dist, startTime);
        if (speeds == null) return;  // No valid speeds given the distance and start time
        Map<Double, Integer> speedsToArrTimes = ArcGenerator.mapSpeedsToArrTimes(dist, startTime, speeds);
        int serviceDuration = toOrder != null ? ArcGenerator.calculateServiceDuration(toOrder) : 0;
        Map<Double, List<Integer>> speedsToTimes = ArcGenerator.mapSpeedsToTimePoints(speedsToArrTimes, dist,
                serviceDuration, toOrder != null ? Problem.getInstallation(toOrder) : Problem.getDepot());
        if (speedsToTimes.values().isEmpty()) return;  // Check for early break
        Map<Double, Double> speedsToCosts = ArcGenerator.mapSpeedsToCosts(speedsToTimes, dist, startTime, this.vIdx);
        addNodesToTree(speedsToCosts, speedsToTimes, fromNode, toOrder, c);
    }

    private void addNodesToTree(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimePoints,
                                Node fromNode, Order toOrder, int c) {
        switch (c) {
            case 1:
                addNodesDepotOrder(speedsToCosts, speedsToTimePoints, toOrder);
                break;
            case 2:
                addNodesOrderOrder(speedsToCosts, speedsToTimePoints, fromNode, toOrder);
                break;
            case 3:
                addNodesOrderDepot(speedsToCosts, speedsToTimePoints, fromNode);
                break;
            default:
                System.out.println("Case not recognized.");
        }
    }

    private void addNodesDepotOrder(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimes,
                                    Order toOrder) {
        List<Integer> timePointsDepot = new ArrayList<>(Arrays.asList(Problem.preparationEndTime,
                Problem.preparationEndTime, Problem.preparationEndTime));
        Node depotNode = new Node(null, null, timePointsDepot, 0.0);
        this.addNode(depotNode);
        this.setRoot(depotNode);
        for (double speed : speedsToCosts.keySet()) {
            double cost = speedsToCosts.get(speed);
            List<Integer> timePoints = speedsToTimes.get(speed);
            int endTime = timePoints.get(2);
            Node existingNode = getExistingNode(endTime, toOrder);
            if (existingNode == null) {
                Node newNode = new Node(toOrder, depotNode, timePoints, speed);
                depotNode.addChild(newNode);
                depotNode.setChildToCost(newNode, cost);
                this.addNode(newNode);
            } else {
                if (cost < depotNode.getCostOfChild(existingNode)) {
                    depotNode.setChildToCost(existingNode, cost);
                    existingNode.setParentToTimePoints(null, timePoints);
                    existingNode.setParentToSpeed(null, speed);
                }
            }
        }
    }

    private void addNodesOrderOrder(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimes,
                                    Node fromNode, Order toOrder) {
        for (double speed : speedsToCosts.keySet()) {
            double cost = speedsToCosts.get(speed);
            List<Integer> timePoints = speedsToTimes.get(speed);
            int endTime = timePoints.get(2);
            Node existingNode = getExistingNode(endTime, toOrder);
            if (existingNode == null) {
                Node newNode = new Node(toOrder, fromNode, timePoints, speed);
                fromNode.addChild(newNode);
                fromNode.setChildToCost(newNode, cost);
                this.addNode(newNode);
            } else {
                existingNode.addParent(fromNode);
                existingNode.setParentToTimePoints(fromNode, timePoints);
                existingNode.setParentToSpeed(fromNode, speed);
                if (fromNode.hasChild(existingNode)) {
                    double bestCost = Math.min(fromNode.getCostOfChild(existingNode), cost);
                    fromNode.setChildToCost(existingNode, bestCost);
                } else {
                    fromNode.addChild(existingNode);
                    fromNode.setChildToCost(existingNode, cost);
                }
            }
        }
    }

    private void addNodesOrderDepot(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimes,
                                    Node fromNode) {
        double minCostSpeed = Helpers.getKeyWithMinValue(speedsToCosts);
        double cost = speedsToCosts.get(minCostSpeed);
        List<Integer> timePoints = speedsToTimes.get(minCostSpeed);
        int endTime = timePoints.get(2);
        Node existingDepotNode = getExistingNode(endTime, null);
        if (existingDepotNode == null) {
            Node depotNode = new Node(null, fromNode, timePoints, minCostSpeed);
            fromNode.addChild(depotNode);
            fromNode.setChildToCost(depotNode, cost);
            this.addNode(depotNode);
        } else {
            existingDepotNode.addParent(fromNode);
            existingDepotNode.setParentToTimePoints(fromNode, timePoints);
            existingDepotNode.setParentToSpeed(fromNode, minCostSpeed);
            fromNode.addChild(existingDepotNode);
            fromNode.setChildToCost(existingDepotNode, cost);
        }
    }

    protected void printTree() {
        for (Node node : this.nodes) {
            System.out.println(node);
            System.out.println("\tChildren");
            List<Node> sortedChildren = new ArrayList<>(node.getChildren());
            sortedChildren.sort(Comparator.comparing(Node::getDiscreteTime));
            for (Node child : sortedChildren) {
                double cost = Math.round(node.getCostOfChild(child) * 100.0) / 100.0;
                double speed = child.getSpeed(node);
                System.out.println("\t\t" + child + " at cost " + cost + " and speed " + speed);
            }
            System.out.println("\tParents");
            for (Node parent : node.getParents()) {
                System.out.println("\t\t" + parent);
            }
            System.out.println();
        }
    }
}
