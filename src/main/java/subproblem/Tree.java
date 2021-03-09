package subproblem;

import arcs.ArcGenerator;
import data.Problem;
import objects.Installation;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class Tree {

    private Node root;
    private List<Node> nodes = new ArrayList<>();
    private List<Node> shortestPath;
    private double globalBestCost;

    public Tree() {
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

    protected List<Node> getShortestPath() {
        return shortestPath;
    }

    protected List<Node> findShortestPath() {
        List<Node> queue = initialize();
        while (queue.size() != 0) {
            Node currentNode = ((LinkedList<Node>) queue).removeFirst();
            Set<Node> setOfChildren = currentNode.getChildren();
            if (currentNode != this.root) updateCurrentBest(currentNode);
            if (setOfChildren.isEmpty()) {
                updateGlobalBest(currentNode);
            }
            for (Node child : setOfChildren) {
                if (!child.isVisited()) {
                    child.setVisited(true);
                    queue.add(child);
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

    private void updateCurrentBest(Node currentNode) {
        for (Node parent : currentNode.getParents()) {
            double currentCost = parent.getBestCost() + parent.getCostOfChild(currentNode);
            if (currentCost < currentNode.getBestCost()) {
                currentNode.setBestCost(currentCost);
                List<Node> path = Helpers.deepCopyList(parent.getBestPath(), false);
                path.add(currentNode);
                currentNode.setBestPath(path);
            }
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
            if (node.getOrder().equals(lastOrder)) {
                lastLayer.add(node);
            }
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

    private void createNodes(Node fromNode, Order toOrder, boolean isSpot, double distance, int startTime, int c) {
        List<Double> speeds = ArcGenerator.getSpeeds(distance, startTime);
        Map<Double, Integer> speedsToArrTimes = ArcGenerator.mapSpeedsToArrTimes(distance, startTime, speeds);
        int serviceDuration = toOrder != null ? ArcGenerator.calculateServiceDuration(toOrder) : 0;
        Map<Double, List<Integer>> speedsToTimePoints = ArcGenerator.mapSpeedsToTimePoints(speedsToArrTimes, distance,
                serviceDuration, toOrder != null ? Problem.getInstallation(toOrder) : Problem.getDepot());
        Map<Double, Double> speedsToCosts = ArcGenerator.mapSpeedsToCosts(speedsToTimePoints, distance, startTime,
                isSpot);
        addNodesToTree(speedsToCosts, speedsToTimePoints, fromNode, toOrder, c);

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

    private void addNodesDepotOrder(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimePoints,
                                    Order toOrder) {
        List<Integer> timePointsDepot = new ArrayList<>(Arrays.asList(Problem.preparationEndTime,
                Problem.preparationEndTime, Problem.preparationEndTime));
        Node depotNode = new Node(null, null, timePointsDepot);
        this.addNode(depotNode);
        this.setRoot(depotNode);
        for (double speed : speedsToCosts.keySet()) {
            double cost = speedsToCosts.get(speed);
            List<Integer> timePoints = speedsToTimePoints.get(speed);
            int endTime = timePoints.get(2);

            Node existingNode = getExistingNode(endTime, toOrder);
            if (existingNode == null) {
                Node newNode = new Node(toOrder, depotNode, timePoints);
                depotNode.addChild(newNode);
                depotNode.setChildToCost(newNode, cost);
                this.addNode(newNode);
            } else {
                if (cost < depotNode.getCostOfChild(existingNode)) {
                    depotNode.setChildToCost(existingNode, cost);
                }
            }
        }
    }

    private void addNodesOrderOrder(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimePoints,
                                    Node fromNode, Order toOrder) {
        for (double speed : speedsToCosts.keySet()) {
            double cost = speedsToCosts.get(speed);
            List<Integer> timePoints = speedsToTimePoints.get(speed);
            int endTime = timePoints.get(2);

            Node existingNode = getExistingNode(endTime, toOrder);
            if (existingNode == null) {
                Node newNode = new Node(toOrder, fromNode, timePoints);
                fromNode.addChild(newNode);
                fromNode.setChildToCost(newNode, cost);
                this.addNode(newNode);
            } else {
                existingNode.addParent(fromNode);
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

    private void addNodesOrderDepot(Map<Double, Double> speedsToCosts, Map<Double, List<Integer>> speedsToTimePoints,
                                    Node fromNode) {
        double minCostSpeed = Helpers.getKeyWithMinValue(speedsToCosts);
        double cost = speedsToCosts.get(minCostSpeed);
        List<Integer> timePoints = speedsToTimePoints.get(minCostSpeed);
        int endTime = timePoints.get(2);

        Node existingDepotNode = getExistingNode(endTime, null);
        if (existingDepotNode == null) {
            Node depotNode = new Node(null, fromNode, timePoints);
            fromNode.addChild(depotNode);
            fromNode.setChildToCost(depotNode, cost);
            this.addNode(depotNode);
        } else {
            existingDepotNode.addParent(fromNode);
            fromNode.addChild(existingDepotNode);
            fromNode.setChildToCost(existingDepotNode, cost);
        }
    }

    protected void printTree() {
        for (Node node : this.nodes) {
            System.out.println(node);
            System.out.println("\tChildren");
            for (Node child : node.getChildren()) {
                System.out.println("\t\t" + child + " at cost " + node.getCostOfChild(child));
            }
            System.out.println("\tParents");
            for (Node parent : node.getParents()) {
                System.out.println("\t\t" + parent);
            }
            System.out.println();
        }
    }
}
