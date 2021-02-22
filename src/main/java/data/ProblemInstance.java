package data;

import objects.Installation;
import objects.Order;
import objects.Vessel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ProblemInstance {

    private static String fileName;
    private static String pathToInstanceFile;

    // Instance objects
    public static List<Installation> installations;
    public static List<Order> orders;
    public static List<Vessel> vessels;

    // Instance info
    public static int planningPeriodHours;  // Should be int
    public static int planningPeriodDisc;
    public static int discretizationParam;
    public static String weatherScenario;
    public static String installationOrdering;
    public static int preparationEndTime;

    // Vessel info
    public static double minSpeed;
    public static double designSpeed;
    public static double maxSpeed;
    public static double fcDesignSpeed;
    public static double fcDepot;
    public static double fcIdling;
    public static double fcServicing;
    public static double fuelPrice;
    public static double spotHourRate;
    public static double discServiceTimeUnit;

    // Weather
    public static Map<Integer, Double> wsToSpeedImpact;
    public static Map<Integer, Double> wsToServiceImpact;
    public static List<Integer> weatherForecast;
    public static List<Integer> weatherForecastDisc;

    public static Installation getDepot() {
        return installations.get(0);
    }

    public static Installation getInstallation(int idx) {
        if (idx < 0 || idx > installations.size() - 1) {
            System.out.println("Index out of range in installation list.");
            return null;
        }
        return installations.get(idx);
    }

    public static Installation getInstallation(Order order) {
        return installations.get(order.getInstallationId());
    }

    public static void setUpProblem(String fileName) {
        ProblemInstance.fileName = fileName;
        ProblemInstance.pathToInstanceFile = Constants.PATH_TO_INSTANCE + fileName;
        setUpInstallations();
        setUpOrders();
        setUpInstanceInfo();
        setUpVessels();
        setUpVesselInfo();
        setUpWeather();
    }

    private static void setUpInstallations() {
        ProblemInstance.installations = new ArrayList<>();
        JSONObject jsonInstallations = getJSONObject(Constants.INSTALLATION_FILE);
        for (Object key : jsonInstallations.keySet()) {
            JSONObject jsonInstallation = (JSONObject) jsonInstallations.get(key);
            String name = (String) key;
            int id = Math.toIntExact((long) jsonInstallation.get(Constants.ID_KEY));
            int openingHour = Math.toIntExact((long) jsonInstallation.get(Constants.OPENING_HOUR_KEY));
            int closingHour = Math.toIntExact((long) jsonInstallation.get(Constants.CLOSING_HOUR_KEY));
            double latitude = (double) jsonInstallation.get(Constants.LATITUDE_KEY);
            double longitude = (double) jsonInstallation.get(Constants.LONGITUDE_KEY);
            double typicalDemand = (double) jsonInstallation.get(Constants.TYPICAL_DEMAND_KEY);
            Installation installation = new Installation(id, name, openingHour, closingHour, latitude, longitude,
                    typicalDemand);
            ProblemInstance.installations.add(installation);
        }
        Collections.sort(ProblemInstance.installations);
    }

    private static void setUpOrders() {
        ProblemInstance.orders = new ArrayList<>();
        JSONObject jsonOrders = (JSONObject) getJSONObject(pathToInstanceFile).get(Constants.ORDERS_KEY);
        int orderId = 0;
        for (Object key : jsonOrders.keySet()) {
            JSONObject jsonOrder = (JSONObject) jsonOrders.get(key);
            double orderSize = (double) jsonOrder.get(Constants.ORDER_SIZE_KEY);
            int installationId = Math.toIntExact((long) jsonOrder.get(Constants.INSTALLATION_KEY));
            boolean isDelivery = ((jsonOrder.get(Constants.TRANSPORTATION_TYPE_KEY)).equals(Constants.DELIVERY_KEY));
            boolean isMandatory = ((jsonOrder.get(Constants.MANDATORY_KEY)).equals(Constants.TRUE_KEY));
            Order order = new Order(orderId, isMandatory, isDelivery, orderSize, installationId);
            ProblemInstance.orders.add(order);
            orderId++;
        }
    }

    private static void setUpVessels() {
        ProblemInstance.vessels = new ArrayList<>();
        JSONObject jsonVessels = (JSONObject) getJSONObject(Constants.VESSEL_FILE).get(Constants.FLEET_KEY);
        JSONObject jsonAvailableVessels =
                (JSONObject) getJSONObject(pathToInstanceFile).get(Constants.AVAILABLE_VESSELS_KEY);
        for (Object key : jsonAvailableVessels.keySet()) {
            String name = (String) key;
            JSONObject jsonVessel = (JSONObject) jsonVessels.get(key);
            int id = Math.toIntExact((long) jsonVessel.get(Constants.ID_KEY));
            double capacity = (double) jsonVessel.get(Constants.CAPACITY_KEY);
            double returnTime = (double) ((JSONObject) jsonAvailableVessels.get(key)).get(Constants.RETURN_TIME_KEY);
            Vessel vessel = new Vessel(id, name, capacity, returnTime);
            ProblemInstance.vessels.add(vessel);
        }
        Collections.sort(ProblemInstance.vessels);
    }

    private static void setUpInstanceInfo() {
        JSONObject jsonInstanceInfo = getJSONObject(ProblemInstance.pathToInstanceFile);
        ProblemInstance.planningPeriodHours = ((int) ((double) jsonInstanceInfo.get(Constants.PLANNING_PERIOD_KEY)));
        ProblemInstance.discretizationParam = ((int) ((double) jsonInstanceInfo.get(Constants.DISCRETIZATION_KEY)));
        ProblemInstance.weatherScenario = (String) jsonInstanceInfo.get(Constants.WEATHER_SCENARIO_KEY);
        ProblemInstance.installationOrdering = (String) jsonInstanceInfo.get(Constants.INSTALLATION_ORDERING_KEY);

        ProblemInstance.planningPeriodDisc = ProblemInstance.planningPeriodHours * ProblemInstance.discretizationParam;
    }

    private static void setUpVesselInfo() {
        JSONObject jsonVesselInfo = getJSONObject(Constants.VESSEL_FILE);
        ProblemInstance.minSpeed = (double) jsonVesselInfo.get(Constants.MIN_SPEED_KEY);
        ProblemInstance.designSpeed = (double) jsonVesselInfo.get(Constants.DESIGN_SPEED_KEY);
        ProblemInstance.maxSpeed = (double) jsonVesselInfo.get(Constants.MAX_SPEED_KEY);
        ProblemInstance.fcDesignSpeed = (double) jsonVesselInfo.get(Constants.FC_DESIGN_SPEED_KEY);
        ProblemInstance.fcDepot = (double) jsonVesselInfo.get(Constants.FC_DEPOT_KEY);
        ProblemInstance.fcIdling = (double) jsonVesselInfo.get(Constants.FC_IDLING_KEY);
        ProblemInstance.fcServicing = (double) jsonVesselInfo.get(Constants.FC_SERVICING_KEY);
        ProblemInstance.fuelPrice = (double) jsonVesselInfo.get(Constants.FUEL_PRICE_KEY);
        ProblemInstance.spotHourRate = (double) jsonVesselInfo.get(Constants.SPOT_HOUR_RATE_KEY);
        double realServiceTimeUnit = (double) jsonVesselInfo.get(Constants.SERVICE_TIME_KEY);
        ProblemInstance.discServiceTimeUnit = realServiceTimeUnit * ProblemInstance.discretizationParam;
        int preparationEndHour = Math.toIntExact((long) jsonVesselInfo.get(Constants.PREP_END_KEY));
        ProblemInstance.preparationEndTime = preparationEndHour * ProblemInstance.discretizationParam;
    }

    private static void setUpWeather() {
        ProblemInstance.wsToSpeedImpact = new HashMap<>();
        ProblemInstance.wsToServiceImpact = new HashMap<>();
        JSONObject jsonWeather = getJSONObject(Constants.WEATHER_FILE);
        JSONObject jsonWS = (JSONObject) jsonWeather.get(Constants.SCENARIOS_KEY);
        JSONArray jsonWeatherForecast = (JSONArray) jsonWS.get(ProblemInstance.weatherScenario);
        ProblemInstance.processWeatherForecast(jsonWeatherForecast);
        ProblemInstance.createDiscWeatherForecast();
        JSONObject jsonSpeedImpact = (JSONObject) jsonWeather.get(Constants.SPEED_IMPACT_KEY);
        JSONObject jsonServiceImpact = (JSONObject) jsonWeather.get(Constants.SERVICE_IMPACT_KEY);
        addToHashMap(jsonSpeedImpact, ProblemInstance.wsToSpeedImpact);
        addToHashMap(jsonServiceImpact, ProblemInstance.wsToServiceImpact);
    }

    private static void processWeatherForecast(JSONArray jsonWeatherForecast) {
        ProblemInstance.weatherForecast = new ArrayList<>();
        for (Object objWeatherState : jsonWeatherForecast) {
            int weatherState = Math.toIntExact((long) objWeatherState);
            ProblemInstance.weatherForecast.add(weatherState);
        }
    }

    private static void createDiscWeatherForecast() {
        ProblemInstance.weatherForecastDisc = new ArrayList<>();
        for (int i = 0; i < ProblemInstance.planningPeriodDisc; i++) {
            int idx = i / ProblemInstance.discretizationParam;
            ProblemInstance.weatherForecastDisc.add(ProblemInstance.weatherForecast.get(idx));
        }
    }

    private static void addToHashMap(JSONObject jsonObject, Map<Integer, Double> map) {
        for (Object key : jsonObject.keySet()) {
            int intKey = Integer.parseInt((String) key);
            double doubleValue = (double) jsonObject.get(key);
            map.put(intKey, doubleValue);
        }
    }

    private static JSONObject getJSONObject(String path) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(path)) {
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        ProblemInstance.setUpProblem("example.json");

    }
}


