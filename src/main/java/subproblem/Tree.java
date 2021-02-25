package subproblem;

import data.Problem;
import objects.Installation;
import objects.Order;
import utils.DistanceCalculator;

import java.util.*;
import java.util.stream.IntStream;

public class Tree {

    private Node root;
    private List<Node> nodes = new ArrayList<>();
    private List<Node> shortestPath;
    private double globalBestCost;

    public Tree() {
    }

    private void setRoot(Node root) {
        this.root = root;
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

    public List<Node> findShortestPath() {
        LinkedList<Node> queue = initialize();
        while (queue.size() != 0) {
            Node currentNode = queue.removeFirst();
            Set<Node> setOfChildren = currentNode.getChildren();  // Retrieved in order right to left in tree
            if (currentNode != this.root) updateCurrentBest(currentNode);
            if (setOfChildren.isEmpty()) updateGlobalBest(currentNode);
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
        if (currentNode.getBestCost() < this.globalBestCost) {
            this.globalBestCost = currentNode.getBestCost();
            this.shortestPath = deepCopy(currentNode.getBestPath());
        }
    }

    private void updateCurrentBest(Node currentNode) {
        currentNode.setBestCost(Double.POSITIVE_INFINITY);  // TODO: This can be removed, but double check first
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

    public void generateTree(LinkedList<Order> orderSequence, boolean isSpotVessel) {
        Order firstOrder = orderSequence.getFirst();
        this.generateNodesDepotToOrder(firstOrder, isSpotVessel);
        Node firstOrderNode = this.nodes.get(1);
        Order secondOrder = orderSequence.get(1);
        this.generateNodesOrderToOrder(firstOrderNode, secondOrder, isSpotVessel);
        Node secondOrderNode = this.nodes.get(this.nodes.size() - 1);
        this.generateNodesOrderToDepot(secondOrderNode, isSpotVessel);

        System.out.println(this.nodes);
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
        double minCostSpeed = getMinCostSpeed(speedsToCosts);
        double cost = speedsToCosts.get(minCostSpeed);
        int endTime = speedsToEndTimes.get(minCostSpeed);
        Node depotNode = new Node(null, endTime, fromNode);
        fromNode.addChild(depotNode);
        fromNode.setChildToCost(depotNode, cost);
        this.addNode(depotNode);
    }

    private double getMinCostSpeed(Map<Double, Double> speedsToCosts) {
        Map.Entry<Double, Double> min = null;
        for (Map.Entry<Double, Double> entry : speedsToCosts.entrySet()) {
            if (min == null || entry.getValue() < min.getValue()) {
                min = entry;
            }
        }
        return min.getKey();
    }

    private List<Order> createDummyOrderSequence(int length, int seedValue) {
        Integer[] indicesArray = IntStream.range(0, Problem.orders.size()).boxed().toArray( Integer[]::new );
        List<Integer> indices = Arrays.asList(indicesArray);
        Collections.shuffle(indices, new Random(seedValue));
        List<Order> orderSequence = new LinkedList<>();
        for (int i = 0; i < length; i++) orderSequence.add(Problem.orders.get(indices.get(i)));
        return orderSequence;
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example.json");
        Tree tree = new Tree();
        int numberOfOrders = 4;
        int seedValue = 69;
        LinkedList<Order> orderSequence = (LinkedList<Order>) tree.createDummyOrderSequence(numberOfOrders, seedValue);
        boolean isSpotVessel = false;
        tree.generateTree(orderSequence, isSpotVessel);
    }
}
