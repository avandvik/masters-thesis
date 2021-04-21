package setpartitioning;

import alns.Main;
import data.Problem;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import objects.Order;
import utils.Helpers;

import java.util.*;

public class Data {

    public static GRBEnv gurobiEnv;

    public static List<Order> orders;
    public static List<List<Double>> costOfRouteForVessel;
    public static List<Double> costOfPostponedOrders;
    public static List<List<List<Double>>> orderInRouteForVessel;

    public static List<List<List<Order>>> routeArray;

    public static void initializeGurobiEnv() {
        try {
            gurobiEnv = new GRBEnv();
            gurobiEnv.set(GRB.IntParam.OutputFlag, 0);
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }

    public static void makeArrays() {
        if (Main.vesselToSequenceToCost == null) throw new NullPointerException("VesselToSequenceCost is null");

        makeOrdersArray();
        makeRouteArray();
        makeCostOfRouteForVessel();
        makePostponedOrderCosts();
        makeOrderInRouteForVessel();
    }

    private static void makeOrdersArray() {
        orders = Helpers.deepCopyList(Problem.orders, false);
    }

    private static void makeRouteArray() {
        routeArray = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            routeArray.add(new ArrayList<>());
            List<List<Order>> routes = new ArrayList<>(Main.vesselToSequenceToCost.get(vesselIdx).keySet());
            for (List<Order> route : routes) {
                routeArray.get(vesselIdx).add(new LinkedList<>(route));
            }
        }
    }

    private static void makeCostOfRouteForVessel() {
        costOfRouteForVessel = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            costOfRouteForVessel.add(new ArrayList<>());
            Map<List<Order>, Double> routeToCost = Main.vesselToSequenceToCost.get(vesselIdx);
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

    private static void makeOrderInRouteForVessel() {
        orderInRouteForVessel = new ArrayList<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            orderInRouteForVessel.add(new ArrayList<>());
            Map<List<Order>, Double> routeToCost = Main.vesselToSequenceToCost.get(vesselIdx);
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
