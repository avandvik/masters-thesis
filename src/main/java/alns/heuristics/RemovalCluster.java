package alns.heuristics;

import alns.Solution;
import alns.heuristics.protocols.Destroyer;
import data.Messages;
import data.Parameters;
import data.Problem;
import objects.Order;
import utils.DistanceCalculator;
import utils.Helpers;

import java.util.*;

public class RemovalCluster extends Heuristic implements Destroyer {

    public RemovalCluster(String name) {
        super(name);
    }

    @Override
    public Solution destroy(Solution solution) {
        int nbrOrders = getNbrOrdersToRemove(solution);
        Solution newSolution = getClusterRemoval(solution, nbrOrders);
        // if (!Evaluator.isPartFeasible(newSolution)) throw new IllegalStateException(Messages.solutionInfeasible);
        newSolution.clearSubProblemResults();
        return newSolution;
    }

    private Solution getClusterRemoval(Solution solution, int numberOfOrders) {
        /*  */

        Solution newSolution = Helpers.deepCopySolution(solution);
        newSolution.clearSubProblemResults();
        int removedOrders = 0;
        List<List<Order>> orderSequencesLeft = Helpers.deepCopy2DList(newSolution.getOrderSequences());
        while (removedOrders < numberOfOrders && orderSequencesLeft.size() > 0) {
            List<Order> orderSequence = getAndRemoveRandomSequence(orderSequencesLeft);
            if (orderSequence.isEmpty()) continue;
            List<List<Order>> clusters = applyKMeans(orderSequence);
            List<Order> pickedCluster = pickCluster(clusters);
            updateSolution(pickedCluster, newSolution);
            removedOrders += pickedCluster.size();
        }
        return newSolution;
    }

    private List<Order> getAndRemoveRandomSequence(List<List<Order>> orderSequences) {
        int rnSequenceIdx = Problem.random.nextInt(orderSequences.size());
        return orderSequences.remove(rnSequenceIdx);
    }

    private List<List<Order>> applyKMeans(List<Order> orderSequence) {
        if (orderSequence.size() < Parameters.k) {
            List<List<Order>> clusters = new ArrayList<>();
            clusters.add(orderSequence);
            clusters.add(new ArrayList<>());
            return clusters;
        }
        List<List<Order>> clusters = new ArrayList<>();
        for (int attempt = 0; attempt < Parameters.kMeansAttempts; attempt++) {
            clusters = kMeans(orderSequence);
            if (!containsEmptyCluster(clusters)) break;
        }
        if (clusters.isEmpty()) throw new IllegalStateException(Messages.emptyCluster);
        return clusters;
    }

    private List<List<Order>> kMeans(List<Order> orderSequence) {
        List<Order> initialOrders = getRandomCentroids(orderSequence);
        List<List<Double>> newCentroids = Helpers.convertOrdersToCoordinates(initialOrders);
        List<List<Double>> prevCentroids = null;
        List<List<Order>> clusters = null;
        while (!newCentroids.equals(prevCentroids)) {
            clusters = getClusters(orderSequence, newCentroids);  // Assignment step
            prevCentroids = newCentroids;
            newCentroids = getCentroids(clusters);  // Re-estimation step
            for (List<Double> centroid : newCentroids) {
                if (centroid == null) break;
            }
        }
        return clusters;
    }

    private List<Order> getRandomCentroids(List<Order> orderSequence) {
        List<Order> copyOfOrderSequence = Helpers.deepCopyList(orderSequence, false);
        Collections.shuffle(copyOfOrderSequence, Problem.random);
        return copyOfOrderSequence.subList(0, Parameters.k);
    }

    private List<List<Order>> getClusters(List<Order> orderSequence, List<List<Double>> centroids) {
        List<List<Order>> newClusters = new ArrayList<>();
        for (int centroidIdx = 0; centroidIdx < Parameters.k; centroidIdx++) newClusters.add(new ArrayList<>());
        for (Order order : orderSequence) {
            double minSqDistance = Double.POSITIVE_INFINITY;
            int bestCentroidIdx = 0;
            for (int centroidIdx = 0; centroidIdx < Parameters.k; centroidIdx++) {
                if (centroids.get(centroidIdx) == null) continue;
                double latCentroid = centroids.get(centroidIdx).get(0);
                double lonCentroid = centroids.get(centroidIdx).get(1);
                double sqDistance = Math.pow(DistanceCalculator.distance(latCentroid, lonCentroid, order, "N"), 2.0);
                if (sqDistance < minSqDistance) {
                    minSqDistance = sqDistance;
                    bestCentroidIdx = centroidIdx;
                }
            }
            newClusters.get(bestCentroidIdx).add(order);
        }
        return newClusters;
    }

    private List<List<Double>> getCentroids(List<List<Order>> clusters) {
        List<List<Double>> newCentroids = new ArrayList<>();
        for (List<Order> cluster : clusters) {
            if (cluster.isEmpty()) {
                newCentroids.add(null);
                continue;
            }
            double avgLat = getAvgLatitude(cluster);
            double avgLon = getAvgLongitude(cluster);
            if (avgLat == -1.0 || avgLon == -1.0) throw new IllegalStateException(Messages.errorInAvgLatLon);
            newCentroids.add(new ArrayList<>(Arrays.asList(avgLat, avgLon)));
        }
        return newCentroids;
    }

    private double getAvgLatitude(List<Order> cluster) {
        return cluster.stream().mapToDouble(o -> Problem.getInstallation(o).getLatitude()).average().orElse(-1.0);
    }

    private double getAvgLongitude(List<Order> cluster) {
        return cluster.stream().mapToDouble(o -> Problem.getInstallation(o).getLongitude()).average().orElse(-1.0);
    }

    private boolean containsEmptyCluster(List<List<Order>> clusters) {
        for (List<Order> cluster : clusters) {
            if (cluster.isEmpty()) return true;
        }
        return false;
    }

    private List<Order> pickCluster(List<List<Order>> clusters) {
        clusters.removeIf(List::isEmpty);
        return clusters.get(Problem.random.nextInt(clusters.size()));
    }

    private void updateSolution(List<Order> cluster, Solution newSolution) {
        List<Order> ordersToRemove = new ArrayList<>();
        for (Order order : cluster) ordersToRemove.addAll(getOrdersToRemove(order));
        for (List<Order> orderSequence : newSolution.getOrderSequences()) orderSequence.removeAll(ordersToRemove);
        // newSolution.removeFromOrderSequences(ordersToRemove);
        newSolution.removeFromPostponedOrders(ordersToRemove);
        newSolution.getUnplacedOrders().addAll(ordersToRemove);
    }
}
