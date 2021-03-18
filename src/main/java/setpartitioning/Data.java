package setpartitioning;

import alns.Main;
import data.Problem;
import objects.Order;

import java.util.*;

public class Data {

    public static List<Order> orders;
    public static List<List<Double>> costOfRouteForVessel;
    public static List<Double> costOfPostponedOrders;
    public static List<List<List<Double>>> orderInRouteForVessel;

    public static List<List<List<Order>>> routeArray;

    public static void makeArrays() {
        Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost = Main.vesselToSequenceToCost;
        if (vesselToRouteToCost == null) throw new NullPointerException("VesselToSequenceCost is null");

        makeRouteArray();

        makeOrders(vesselToRouteToCost);
        makeCostOfRouteForVessel(vesselToRouteToCost);
        makePostponedOrderCosts();
        makeOrderInRouteForVessel(vesselToRouteToCost);
    }

    private static void makeRouteArray() {
        routeArray = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            routeArray.add(new ArrayList<>());
            int routeIdx = 0;
            for (List<Order> route : Main.vesselToSequenceToCost.get(vesselIdx).keySet()) {
                routeArray.get(vesselIdx).add(new ArrayList<>());
                for (Order order : route) {
                    routeArray.get(vesselIdx).get(routeIdx).add(order);
                }
                routeIdx++;
            }
        }
    }

    private static void makeOrders(Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost) {
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
        orders.sort(Comparator.comparingInt(Order::getOrderId));
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

    private static void makePostponedOrderCosts() {
        costOfPostponedOrders = new ArrayList<>();
        for (Order postponeOrder : Data.orders) {
            costOfPostponedOrders.add(postponeOrder.getPostponementPenalty());
        }
    }

    private static void makeOrderInRouteForVessel(Map<Integer, Map<List<Order>, Double>> vesselToRouteToCost) {
        orderInRouteForVessel = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            orderInRouteForVessel.add(new ArrayList<>());
            Map<List<Order>, Double> routeToCost = vesselToRouteToCost.get(vesselIdx);
            List<List<Order>> routes = new ArrayList<>(routeToCost.keySet());
            for (int routeIdx = 0; routeIdx < routes.size(); routeIdx++) {
                List<Order> route = routes.get(routeIdx);
                orderInRouteForVessel.get(vesselIdx).add(new ArrayList<>(Collections.nCopies(orders.size(), 0.0)));
                for (Order order : Data.orders) {
                    if (route.contains(order)) {
                        int idx = order.getOrderId();
                        orderInRouteForVessel.get(vesselIdx).get(routeIdx).set(idx, 1.0);
                    }
                }
            }
        }
    }
}
