package subproblem;

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
            if (node.getDiscreteTime() == time && node.getOrder().equals(order)) {
                return node;
            }
        }
        return null;
    }

    private List<Node> getNodesExclDepot()  {
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
            if (setOfChildren.isEmpty()) updateGlobalBest(currentNode);
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
            this.shortestPath = Helpers.deepCopyList(currentNode.getBestPath());
        }
    }

    private void updateCurrentBest(Node currentNode) {
        for (Node parent : currentNode.getParents()) {
            double currentCost = parent.getBestCost() + parent.getCostOfChild(currentNode);
            if (currentCost < currentNode.getBestCost()) {
                currentNode.setBestCost(currentCost);
                List<Node> path = Helpers.deepCopyList(parent.getBestPath());
                path.add(currentNode);
                currentNode.setBestPath(path);
            }
        }
    }

    protected void generateTree(List<Order> orderSequence, boolean isSpotVessel) {
        Order firstOrder = ((LinkedList<Order>) orderSequence).getFirst();
        this.generateNodesDepotToOrder(firstOrder, isSpotVessel);

        List<Node> queue = Helpers.deepCopyList(this.getNodesExclDepot());
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

    private void createNodes(Node fromNode, Order toOrder, boolean isSpot, double distance, int startTime, int c) {
        List<Double> speeds = ArcGeneration.getSpeeds(distance, startTime);
        Map<Double, Integer> speedsToArrTimes = ArcGeneration.mapSpeedsToArrTimes(distance, startTime, speeds);
        int serviceDuration = toOrder != null ? ArcGeneration.calculateServiceDuration(toOrder) : 0;
        Map<Double, List<Integer>> speedsToTimePoints = ArcGeneration.mapSpeedsToTimePoints(speedsToArrTimes, distance,
                serviceDuration, toOrder != null ? Problem.getInstallation(toOrder) : Problem.getDepot());
        Map<Double, Double> speedsToCosts = ArcGeneration.mapSpeedsToCosts(speedsToTimePoints, distance, startTime,
                isSpot);
        Map<Double, Integer> speedsToEndTimes = ArcGeneration.mapSpeedsToEndTimes(speedsToTimePoints);
        addNodesToTree(speedsToCosts, speedsToEndTimes, fromNode, toOrder, c);

    }

    private void addNodesToTree(Map<Double, Double> speedsToCosts, Map<Double, Integer> speedsToEndTimes,
                                Node fromNode, Order toOrder, int c) {
        switch (c) {
            case 1:
                addNodesDepotOrder(speedsToCosts, speedsToEndTimes, toOrder);
                break;
            case 2:
                addNodesOrderOrder(speedsToCosts, speedsToEndTimes, fromNode, toOrder);
                break;
            case 3:
                addNodesOrderDepot(speedsToCosts, speedsToEndTimes, fromNode);
                break;
            default:
                System.out.println("Case not recognized.");
        }
    }

    private void addNodesDepotOrder(Map<Double, Double> speedsToCosts, Map<Double, Integer> speedsToEndTimes,
                                    Order toOrder) {
        Node depotNode = new Node(null, Problem.preparationEndTime, null);
        this.addNode(depotNode);
        this.setRoot(depotNode);
        for (double speed : speedsToCosts.keySet()) {
            double cost = speedsToCosts.get(speed);
            int endTime = speedsToEndTimes.get(speed);
            Node newNode = new Node(toOrder, endTime, depotNode);
            depotNode.addChild(newNode);
            depotNode.setChildToCost(newNode, cost);
            this.addNode(newNode);
        }
    }

    private void addNodesOrderOrder(Map<Double, Double> speedsToCosts, Map<Double, Integer> speedsToEndTimes,
                                    Node fromNode, Order toOrder) {
        for (double speed : speedsToCosts.keySet()) {
            double cost = speedsToCosts.get(speed);
            int endTime = speedsToEndTimes.get(speed);
            Node node = getExistingNode(endTime, toOrder);
            if (node != null) {
                node.addParent(fromNode);
            } else {
                node = new Node(toOrder, endTime, fromNode);
            }
            fromNode.addChild(node);
            fromNode.setChildToCost(node, cost);
            this.addNode(node);
        }
    }

    private void addNodesOrderDepot(Map<Double, Double> speedsToCosts, Map<Double, Integer> speedsToEndTimes,
                                    Node fromNode) {
        double minCostSpeed = Helpers.getKeyWithMinValue(speedsToCosts);
        double cost = speedsToCosts.get(minCostSpeed);
        int endTime = speedsToEndTimes.get(minCostSpeed);
        Node depotNode = new Node(null, endTime, fromNode);
        fromNode.addChild(depotNode);
        fromNode.setChildToCost(depotNode, cost);
        this.addNode(depotNode);
    }

    protected void printTree(Tree tree) {
        for (Node node : tree.nodes) {
            System.out.println(node);
            System.out.println("\tChildren");
            for (Node child : node.getChildren()) {
                System.out.println("\t" + child + " at cost " + node.getCostOfChild(child));
            }
            System.out.println("\tParents");
            for (Node parent : node.getParents()) {
                System.out.println("\t" + parent);
            }
        }
    }
}
