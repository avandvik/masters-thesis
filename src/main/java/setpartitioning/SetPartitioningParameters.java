package setpartitioning;

import alns.Main;
import data.Problem;
import objects.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetPartitioningParameters {

    public static List<List<Double>> costOfRouteForVessel;
    public static List<Double> costOfPostponedOrders;
    public static List<List<List<Double>>> orderInRouteForVessel;
    public static List<Order> orders;

    public static void makeParameters() {
        Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost = Main.vesselToSequenceToCost;
        if (vesselToRouteToCost == null) throw new NullPointerException("VesselToSequenceCost is null");

        getOrders(vesselToRouteToCost);
        makeCostOfRouteForVessel(vesselToRouteToCost);
        makePostponedOrderCosts(vesselToRouteToCost);
        makeOrderInRouteForVessel(vesselToRouteToCost);
    }

    private static void makeCostOfRouteForVessel(Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost) {
        costOfRouteForVessel = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            costOfRouteForVessel.add(new ArrayList<>());
            Map<List<Order>, Double> routeToCost = vesselToRouteToCost.get(vesselIdx);
            List<List<Order>> routes = new ArrayList<>(routeToCost.keySet());
            for (List<Order> route : routes) {
                double cost = routeToCost.get(route);
                costOfRouteForVessel.get(vesselIdx).add(cost);
            }
        }
    }

    private static void makePostponedOrderCosts(Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost) {
        costOfPostponedOrders = new ArrayList<>();
        for (Order postponeOrder : SetPartitioningParameters.orders) {
            costOfPostponedOrders.add(postponeOrder.getPostponementPenalty());
        }
    }

    private static void makeOrderInRouteForVessel(Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost ) {
        orderInRouteForVessel = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            orderInRouteForVessel.add(new ArrayList<>());
            Map<List<Order>, Double> routeToCost = vesselToRouteToCost.get(vesselIdx);
            // TODO: Double check order of the routes
            List<List<Order>> routes = new ArrayList<>(routeToCost.keySet());
            for (int routeIdx = 0; routeIdx < routes.size(); routeIdx++) {
                List<Order> route = routes.get(routeIdx);
                for (Order order : SetPartitioningParameters.orders) {
                    if (route.contains(order)) {
                        orderInRouteForVessel.get(vesselIdx).get(routeIdx).add(1.0);
                    } else {
                        orderInRouteForVessel.get(vesselIdx).get(routeIdx).add(0.0);
                    }
                }
            }
        }
    }

    private static void getOrders(Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost ) {
        orders = new ArrayList<>();
        List<List<Double>> costOfRouteForVessel = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            costOfRouteForVessel.add(new ArrayList<>());
            Map<List<Order>, Double> routeToCost = vesselToRouteToCost.get(vesselIdx);
            List<List<Order>> routes = new ArrayList<>(routeToCost.keySet());
            for (List<Order> route : routes) {
                double cost = routeToCost.get(route);
                costOfRouteForVessel.get(vesselIdx).add(cost);
                // Making list of all orders in the problem
                for (Order order : route) {
                    if (!orders.contains(order)) {
                        orders.add(order);
                    }
                }
            }
        }
    }
}
