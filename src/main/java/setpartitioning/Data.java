package setpartitioning;

import alns.Main;
import data.Problem;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import objects.Installation;
import objects.Order;
import utils.Helpers;

import java.util.*;
import java.util.stream.Collectors;

public class Data {

    public static GRBEnv gurobiEnv;

    public static int nbrOrders;
    public static int nbrVessels;
    public static Map<Integer, Integer> vesselToNbrVoyages;

    public static List<Order> orders;
    public static List<Installation> installationsWithODOP;
    public static List<List<Double>> costOfVoyageForVessel;
    public static List<Double> ordersPostponeCost;
    public static List<List<List<Double>>> orderInVoyageForVessel;

    public static List<List<List<Order>>> voyageArray;  // vessel -> voyage
    public static List<List<List<Integer>>> voyageODOPArray;  // inst -> vessel -> voyageIdx

    public static Map<Integer, Map<Integer, List<Order>>> vesselToVoyageIdxToVoyage;
    public static Map<Integer, Map<Integer, Double>> vesselToVoyageIdxToCost;

    public static void initializeGurobiEnv() {
        try {
            gurobiEnv = new GRBEnv();
            gurobiEnv.set(GRB.IntParam.OutputFlag, 0);
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }
    }

    public static void makeConstants() {
        nbrOrders = Problem.orders.size();
        nbrVessels = Problem.getNumberOfVessels();
        vesselToNbrVoyages = new HashMap<>();
        for (int vIdx = 0; vIdx < nbrVessels; vIdx++) {
            vesselToNbrVoyages.put(vIdx, VoyagePool.vesselToSequenceToCost.get(vIdx).size());
        }
    }

    public static void makeArrays() {
        makeOrdersArray();
        makeInstallationsArray();
        makeVesselToVoyageIndices();
        makeVoyageArray();
        makeVoyageODOPArray();
        makeCostOfVoyageForVessel();
        makeOrdersPostponeCosts();
        makeOrderInVoyageForVessel();
    }

    private static void makeOrdersArray() {
        orders = Helpers.deepCopyList(Problem.orders, false);
    }

    private static void makeInstallationsArray() {
        installationsWithODOP = new ArrayList<>();
        for (Installation inst : Problem.installations) {
            if (Problem.instHasODOP(inst)) installationsWithODOP.add(inst);
        }
    }

    private static void makeVesselToVoyageIndices() {
        vesselToVoyageIdxToVoyage = new HashMap<>();
        vesselToVoyageIdxToCost = new HashMap<>();
        for (int vIdx = 0; vIdx < nbrVessels; vIdx++) {
            vesselToVoyageIdxToVoyage.put(vIdx, new HashMap<>());
            vesselToVoyageIdxToCost.put(vIdx, new HashMap<>());
            int voyageIdx = 0;
            for (Map.Entry<List<Order>, Double> entry : VoyagePool.vesselToSequenceToCost.get(vIdx).entrySet()) {
                List<Order> voyage = entry.getKey();
                double cost = entry.getValue();
                vesselToVoyageIdxToVoyage.get(vIdx).put(voyageIdx, voyage);
                vesselToVoyageIdxToCost.get(vIdx).put(voyageIdx, cost);
                voyageIdx++;
            }
        }
    }

    private static void makeVoyageArray() {
        voyageArray = new ArrayList<>();
        for (int vIdx = 0; vIdx < nbrVessels; vIdx++) {
            voyageArray.add(new ArrayList<>(Collections.nCopies(vesselToNbrVoyages.get(vIdx), null)));
            for (int voyageIdx = 0; voyageIdx < vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                List<Order> voyage = vesselToVoyageIdxToVoyage.get(vIdx).get(voyageIdx);
                voyageArray.get(vIdx).add(voyageIdx, new LinkedList<>(voyage));
            }
        }
    }

    private static void makeVoyageODOPArray() {
        voyageODOPArray = new ArrayList<>();
        for (int instIdx = 0; instIdx < installationsWithODOP.size(); instIdx++) {  // NB! Not instId!
            Installation inst = installationsWithODOP.get(instIdx);
            voyageODOPArray.add(new ArrayList<>());
            for (int vIdx = 0; vIdx < nbrVessels; vIdx++) {
                voyageODOPArray.get(instIdx).add(new ArrayList<>());
                for (int voyageIdx = 0; voyageIdx < vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                    List<Order> voyage = vesselToVoyageIdxToVoyage.get(vIdx).get(voyageIdx);
                    List<Installation> splitInstallations = getInstallationsBeingSplit(voyage);
                    for (Installation splitInst : splitInstallations) {
                        if (splitInst.equals(inst)) {
                            voyageODOPArray.get(instIdx).get(vIdx).add(voyageIdx);
                        }
                    }
                }
            }
        }
    }

    private static List<Installation> getInstallationsBeingSplit(List<Order> voyage) {
        List<Installation> instSequence = Helpers.getInstSequence(voyage);
        List<Installation> splitInstallations = instSequence.stream()
                .filter(inst -> installationsWithODOP.contains(inst)).collect(Collectors.toList());
        splitInstallations.removeIf(inst -> voyageServicesODOP(voyage, inst));
        return splitInstallations;
    }

    private static boolean voyageServicesODOP(List<Order> voyage, Installation inst) {
        boolean containsOD = false;
        boolean containsOP = false;
        for (Order order : voyage) {
            if (Problem.getInstallation(order).equals(inst)) {
                if (!order.isMandatory() && order.isDelivery()) containsOD = true;
                if (!order.isMandatory() && !order.isDelivery()) containsOP = true;
            }
        }
        return containsOD && containsOP;
    }

    private static void makeCostOfVoyageForVessel() {
        costOfVoyageForVessel = new ArrayList<>();
        for (int vIdx = 0; vIdx < nbrVessels; vIdx++) {
            costOfVoyageForVessel.add(new ArrayList<>(Collections.nCopies(vesselToNbrVoyages.get(vIdx), 0.0)));
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                double cost = vesselToVoyageIdxToCost.get(vIdx).get(voyageIdx);
                costOfVoyageForVessel.get(vIdx).add(voyageIdx, cost);
            }
        }
    }

    private static void makeOrdersPostponeCosts() {
        ordersPostponeCost = new ArrayList<>();
        for (Order order : Data.orders) {
            ordersPostponeCost.add(order.getPostponementPenalty());
        }
    }

    private static void makeOrderInVoyageForVessel() {
        orderInVoyageForVessel = new ArrayList<>();
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            orderInVoyageForVessel.add(new ArrayList<>());
            for (int voyageIdx = 0; voyageIdx < Data.vesselToNbrVoyages.get(vIdx); voyageIdx++) {
                List<Order> voyage = vesselToVoyageIdxToVoyage.get(vIdx).get(voyageIdx);
                orderInVoyageForVessel.get(vIdx).add(new ArrayList<>(Collections.nCopies(orders.size(), 0.0)));
                for (Order order : Data.orders) {
                    if (voyage.contains(order)) {
                        int idx = order.getOrderId();
                        orderInVoyageForVessel.get(vIdx).get(voyageIdx).set(idx, 1.0);
                    }
                }
            }
        }
    }
}
