package utils;

import data.Constants;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstanceGenerator {

    private final static JSONObject instance = new JSONObject();

    private final static int seed = 11;
    private final static Random rn = new Random(seed);
    private final static double planningPeriodHours = 80.0;
    private final static double discretizationParameter = 4.0;
    private final static String weatherScenario = "perfect";
    private final static String installationOrdering = "random";
    private final static int returnTime = 80;

    private final static int numberOfInstallations = 5;
    private final static Map<Integer, List<Double>> instIdToOrderSize = new HashMap<>();

    private final static double MDLower = 0.5;
    private final static double MDUpper = 0.7;
    private final static double ODLower = 0.2;
    private final static double ODUpper = 0.4;
    private final static double OPLower = 0.2;
    private final static double OPUpper = 0.4;

    private final static double sizeDeviation = 0.2;

    private final static Set<Integer> installationsWithOrders = new HashSet<>();

    public static void generateInstance() {
        // Add miscellaneous instance information
        addMiscInfoToJSON();

        // Choose installations and assign orders
        mapInstIdToOrderSizes();
        List<Integer> installationIds = drawInstallations();
        assignOrders(installationIds);

        // Assign available vessels
        assignAvailableVessels();

        // Save
        IO.writeToFile(Constants.GENERATOR_PATH + "test.json", instance);
    }

    @SuppressWarnings("unchecked")
    private static void addMiscInfoToJSON() {
        instance.put("planning_period_hours", planningPeriodHours);
        instance.put("discretization_parameter", discretizationParameter);
        instance.put("weather_scenario", weatherScenario);
        instance.put("installation_ordering", installationOrdering);
        instance.put("random_seed", seed);
    }

    private static void mapInstIdToOrderSizes() {
        JSONObject jsonInstallations = IO.getJSONObject(Constants.INSTALLATION_FILE);
        for (Object key : jsonInstallations.keySet()) {
            JSONObject jsonInstallation = (JSONObject) jsonInstallations.get(key);
            int id = Math.toIntExact((long) jsonInstallation.get(Constants.ID_KEY));
            if (id == 0) continue;
            double stdSizeMD = (double) jsonInstallation.get(Constants.STD_DEMAND_MD_KEY);
            double stdSizeOD = (double) jsonInstallation.get(Constants.STD_DEMAND_OD_KEY);
            double stdSizeOP = (double) jsonInstallation.get(Constants.STD_DEMAND_OP_KEY);
            instIdToOrderSize.put(id, Arrays.asList(stdSizeMD, stdSizeOD, stdSizeOP));
        }
    }

    private static List<Integer> drawInstallations() {
        List<Integer> installationIds = new ArrayList<>(instIdToOrderSize.keySet());
        Collections.shuffle(installationIds, rn);
        return installationIds.subList(0, numberOfInstallations);
    }

    @SuppressWarnings("unchecked")
    private static void assignOrders(List<Integer> installationIds) {
        instance.put("orders", new JSONObject());

        int numberOfMDs = getNumberOfOrdersOfType(MDLower, MDUpper);
        int numberOfODs = getNumberOfOrdersOfType(ODLower, ODUpper);
        int numberOfOPs = getNumberOfOrdersOfType(OPLower, OPUpper);

        addOrdersOfType(numberOfMDs, installationIds, "MD");
        addOrdersOfType(numberOfODs, installationIds, "OD");
        addOrdersOfType(numberOfOPs, installationIds, "OP");
    }

    private static int getNumberOfOrdersOfType(double lowerLimit, double upperLimit) {
        int min = (int) Math.ceil(numberOfInstallations * lowerLimit);
        int max = (int) Math.floor(numberOfInstallations * upperLimit);
        List<Integer> range = IntStream.rangeClosed(min, max).boxed().collect(Collectors.toList());
        return range.get(rn.nextInt(range.size()));
    }

    private static void addOrdersOfType(int numberOfOrders, List<Integer> installationIds, String orderType) {
        int ordersAdded = 0;
        List<Integer> installationIdsCopy = Helpers.deepCopyList(installationIds, false);
        while (ordersAdded < numberOfOrders) {
            int id = getInstallationId(installationIdsCopy);
            installationIdsCopy.remove(Integer.valueOf(id));
            installationsWithOrders.add(id);

            int sizeIdx = -1;
            String mandatory = "";
            String transport = "";
            switch (orderType) {
                case "MD":
                    sizeIdx = 0;
                    mandatory = "True";
                    transport = "delivery";
                    break;
                case "OD":
                    sizeIdx = 1;
                    mandatory = "False";
                    transport = "delivery";
                    break;
                case "OP":
                    sizeIdx = 2;
                    mandatory = "False";
                    transport = "pickup";
                    break;
            }

            double orderSize = calculateOrderSize(id, sizeIdx);
            addOrderToJSON(mandatory, transport, orderSize, id);
            ordersAdded++;
        }
    }

    private static int getInstallationId(List<Integer> installationIds) {
        List<Integer> installationsWithoutOrders = new ArrayList<>(installationIds);
        installationsWithoutOrders.removeAll(installationsWithOrders);
        if (installationsWithoutOrders.isEmpty()) {
            return installationIds.remove(rn.nextInt(installationIds.size()));
        } else {
            return installationsWithoutOrders.remove(rn.nextInt(installationsWithoutOrders.size()));
        }
    }

    private static double calculateOrderSize(int id, int sizeIdx) {
        double stdSize = instIdToOrderSize.get(id).get(sizeIdx);
        double minSize = stdSize * (1 - sizeDeviation);
        double maxSize = stdSize * (1 + sizeDeviation);
        return minSize + (maxSize - minSize) * rn.nextDouble();
    }

    @SuppressWarnings("unchecked")
    private static void addOrderToJSON(String mandatory, String transport, double orderSize, int id) {
        String orderIdx = Integer.toString(((JSONObject) instance.get("orders")).size());
        ((JSONObject) instance.get("orders")).put(orderIdx, new JSONObject());
        ((JSONObject) ((JSONObject) instance.get("orders")).get(orderIdx)).put("mandatory", mandatory);
        ((JSONObject) ((JSONObject) instance.get("orders")).get(orderIdx)).put("transport", transport);
        ((JSONObject) ((JSONObject) instance.get("orders")).get(orderIdx)).put("size", orderSize);
        ((JSONObject) ((JSONObject) instance.get("orders")).get(orderIdx)).put("installation", id);
    }

    @SuppressWarnings("unchecked")
    private static void assignAvailableVessels() {

        List<Double> orderSizes = countOrderSizes();
        double MDSize = orderSizes.get(0);
        double totalSize = orderSizes.get(1);

        instance.put("available_vessels", new JSONObject());

        double fleetCapacity = 0.0;
        JSONObject jsonFleet = (JSONObject) IO.getJSONObject(Constants.VESSEL_FILE).get("fleet");
        List<String> vessels = new ArrayList<String>(jsonFleet.keySet());
        vessels.remove("SPOT");
        Collections.sort(vessels);
        for (String vessel : vessels) {
            JSONObject jsonVessel = (JSONObject) jsonFleet.get(vessel);
            double capacity = (double) jsonVessel.get("capacity");
            addVessel(vessel);
            vessels.remove(vessel);
            fleetCapacity += capacity;
            if (fleetCapacity >= MDSize) break;
        }
        JSONObject jsonSpot = (JSONObject) jsonFleet.get("SPOT");
        double capacity = (double) jsonSpot.get("capacity");
        addVessel("SPOT");
        fleetCapacity += capacity;

        while (fleetCapacity < totalSize) {
            String vessel = vessels.get(rn.nextInt(vessels.size()));
            JSONObject jsonVessel = (JSONObject) jsonFleet.get(vessel);
            fleetCapacity += (double) jsonVessel.get("capacity");
            addVessel(vessel);
        }
    }

    private static List<Double> countOrderSizes() {
        double MDSize = 0.0;
        double totalSize = 0.0;
        JSONObject orders = (JSONObject) instance.get("orders");
        for (Object orderIdx : orders.keySet()) {
            double size = (double) ((JSONObject) orders.get(orderIdx)).get("size");
            totalSize += size;
            boolean mandatory = ((JSONObject) orders.get(orderIdx)).get("mandatory").equals("True");
            boolean delivery = ((JSONObject) orders.get(orderIdx)).get("transport").equals("delivery");
            if (mandatory && delivery) MDSize += size;
        }
        return new ArrayList<>(Arrays.asList(MDSize, totalSize));
    }

    @SuppressWarnings("unchecked")
    private static void addVessel(String key) {
        ((JSONObject) instance.get("available_vessels")).put(key, new JSONObject());
        ((JSONObject) ((JSONObject) instance.get("available_vessels")).get(key)).put("return_time", returnTime);
    }

    public static void main(String[] args) {
        InstanceGenerator.generateInstance();
    }
}
