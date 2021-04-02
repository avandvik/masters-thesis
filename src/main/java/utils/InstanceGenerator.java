package utils;

import data.Constants;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstanceGenerator {

    private final static JSONObject instance = new JSONObject();

    // Miscellaneous instance information
    private final static int seed = 11;
    private final static Random rn = new Random(seed);
    private final static double planningPeriodHours = 80.0;
    private final static double discretizationParameter = 4.0;
    private final static String weatherScenario = "perfect";
    private final static String installationOrdering = "random";
    private final static int returnTime = 80;
    private final static String fileName = "test.json";  // Include .json at the end

    // Installation specifics
    private final static int numberOfInstallations = 5;

    // Order specifics
    private final static double MDLower = 0.5;  // At least MDLower % of the installations must have an MD order
    private final static double MDUpper = 0.7;  // At most MDUpper % of the installations must have an MD order
    private final static double ODLower = 0.2;
    private final static double ODUpper = 0.4;
    private final static double OPLower = 0.2;
    private final static double OPUpper = 0.4;
    private final static double sizeDeviation = 0.2;  // Size in [stdSize * (1-sizeDev), stdSize * (1+sizeDev)]

    // Helper fields used in instance generation
    private final static Map<Integer, List<Double>> instIdToOrderSize = new HashMap<>();
    private final static Set<Integer> installationsWithOrders = new HashSet<>();
    private static String finalFleetVessel;

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
        IO.writeToFile(Constants.GENERATOR_PATH + fileName, instance);
    }

    @SuppressWarnings("unchecked")
    private static void addMiscInfoToJSON() {
        instance.put(Constants.PLANNING_PERIOD_KEY, planningPeriodHours);
        instance.put(Constants.DISCRETIZATION_KEY, discretizationParameter);
        instance.put(Constants.WEATHER_SCENARIO_KEY, weatherScenario);
        instance.put(Constants.INSTALLATION_ORDERING_KEY, installationOrdering);
        instance.put(Constants.RANDOM_SEED_KEY, seed);
    }

    private static void mapInstIdToOrderSizes() {
        JSONObject jsonInstallations = IO.getJSONObject(Constants.INSTALLATION_FILE);
        for (Object key : jsonInstallations.keySet()) {
            JSONObject jsonInstallation = (JSONObject) jsonInstallations.get(key);
            int id = Math.toIntExact((long) jsonInstallation.get(Constants.ID_KEY));
            if (id == 0) continue;  // To avoid adding depot
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
        instance.put(Constants.ORDERS_KEY, new JSONObject());

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
                    mandatory = Constants.TRUE_VALUE;
                    transport = Constants.DELIVERY_VALUE;
                    break;
                case "OD":
                    sizeIdx = 1;
                    mandatory = Constants.FALSE_VALUE;
                    transport = Constants.DELIVERY_VALUE;
                    break;
                case "OP":
                    sizeIdx = 2;
                    mandatory = Constants.FALSE_VALUE;
                    transport = Constants.PICKUP_VALUE;
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
        String orderIdx = Integer.toString(((JSONObject) instance.get(Constants.ORDERS_KEY)).size());
        ((JSONObject) instance.get(Constants.ORDERS_KEY)).put(orderIdx, new JSONObject());
        ((JSONObject) ((JSONObject) instance.get(Constants.ORDERS_KEY)).get(orderIdx)).put(Constants.MANDATORY_KEY,
                mandatory);
        ((JSONObject) ((JSONObject) instance.get(Constants.ORDERS_KEY)).get(orderIdx)).put(Constants.TRANSPORT_KEY,
                transport);
        ((JSONObject) ((JSONObject) instance.get(Constants.ORDERS_KEY)).get(orderIdx)).put(Constants.ORDER_SIZE_KEY,
                orderSize);
        ((JSONObject) ((JSONObject) instance.get(Constants.ORDERS_KEY)).get(orderIdx)).put(Constants.INSTALLATION_KEY
                , id);
    }

    @SuppressWarnings("unchecked")
    private static void assignAvailableVessels() {

        // Count the size occupied by MD orders and by all orders in total
        List<Double> orderSizes = countOrderSizes();
        double MDSize = orderSizes.get(0);
        double totalSize = orderSizes.get(1);

        instance.put(Constants.AVAILABLE_VESSELS_KEY, new JSONObject());

        // Initialize fields
        double fleetCapacity = 0.0;
        JSONObject jsonFleet = (JSONObject) IO.getJSONObject(Constants.VESSEL_FILE).get(Constants.FLEET_KEY);
        List<String> vessels = new ArrayList<String>(jsonFleet.keySet());
        vessels.remove(Constants.SPOT_VESSEL_KEY);  // To not add spot vessel twice
        Collections.sort(vessels);  // To add PSV_1 first, then PSV_2, etc.
        finalFleetVessel = vessels.get(vessels.size() - 1);  // To break out of loop at the bottom

        // Add enough fleet vessels to cover the MD orders
        for (String vessel : vessels) {
            JSONObject jsonVessel = (JSONObject) jsonFleet.get(vessel);
            double capacity = (double) jsonVessel.get(Constants.CAPACITY_KEY);
            addVessel(vessel);
            vessels.remove(vessel);
            fleetCapacity += capacity;
            if (fleetCapacity >= MDSize) break;
        }

        // Add the spot vessel
        JSONObject jsonSpot = (JSONObject) jsonFleet.get(Constants.SPOT_VESSEL_KEY);
        double capacity = (double) jsonSpot.get(Constants.CAPACITY_KEY);
        addVessel(Constants.SPOT_VESSEL_KEY);
        fleetCapacity += capacity;

        // If the fleet (included the spot vessel) can't cover the total demand, add more vessels
        while (fleetCapacity < totalSize) {
            String vessel = vessels.get(rn.nextInt(vessels.size()));
            JSONObject jsonVessel = (JSONObject) jsonFleet.get(vessel);
            fleetCapacity += (double) jsonVessel.get(Constants.CAPACITY_KEY);
            addVessel(vessel);
            if (vessel.equals(finalFleetVessel)) break;  // To avoid infinite loop
        }
    }

    private static List<Double> countOrderSizes() {
        double MDSize = 0.0;
        double totalSize = 0.0;
        JSONObject orders = (JSONObject) instance.get(Constants.ORDERS_KEY);
        for (Object orderIdx : orders.keySet()) {
            double size = (double) ((JSONObject) orders.get(orderIdx)).get(Constants.ORDER_SIZE_KEY);
            totalSize += size;
            boolean mandatory =
                    ((JSONObject) orders.get(orderIdx)).get(Constants.MANDATORY_KEY).equals(Constants.TRUE_VALUE);
            boolean delivery =
                    ((JSONObject) orders.get(orderIdx)).get(Constants.TRANSPORT_KEY).equals(Constants.DELIVERY_VALUE);
            if (mandatory && delivery) MDSize += size;
        }
        return new ArrayList<>(Arrays.asList(MDSize, totalSize));
    }

    @SuppressWarnings("unchecked")
    private static void addVessel(String key) {
        ((JSONObject) instance.get(Constants.AVAILABLE_VESSELS_KEY)).put(key, new JSONObject());
        ((JSONObject) ((JSONObject) instance.get(Constants.AVAILABLE_VESSELS_KEY)).get(key)).put(Constants.RETURN_TIME_KEY,
                returnTime);
    }

    public static void main(String[] args) {
        InstanceGenerator.generateInstance();
    }
}
