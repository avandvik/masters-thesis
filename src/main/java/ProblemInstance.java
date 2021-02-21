import objects.Installation;
import objects.Order;
import objects.Vessel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProblemInstance {

    private static String fileName;
    private static String pathToInstanceFile;

    // Instance objects
    public static List<Installation> installations;
    public static List<Order> orders;
    public static List<Vessel> vessels;

    // Instance info
    public static double planningPeriodHours;
    public static double discretizationParam;
    public static String weatherScenario;
    public static String installationOrdering;

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
    public static double realServiceTimeUnit;

    // Weather
    public static Map<Integer, Integer> wsToSpeedImpact;
    public static Map<Integer, Integer> wsToServiceImpact;
    public static List<Integer> weatherForecast;

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
            int openingHour = Math.toIntExact((long) jsonInstallation.get(Constants.CLOSING_HOUR_KEY));
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
        // Read: fileName
        ProblemInstance.orders = new ArrayList<>();
        JSONObject jsonObject = getJSONObject(pathToInstanceFile);
        JSONObject jsonOrders = (JSONObject) jsonObject.get("orders");
        int orderId = 0;

        for(Object key : jsonOrders.keySet()) {
            JSONObject jsonOrder = (JSONObject) jsonOrders.get(key);
            double orderSize = (double) jsonOrder.get("size");
            int installationId = Math.toIntExact((long) jsonOrder.get("installation"));
            boolean isDelivery = ((jsonOrder.get("transport")).equals("delivery"));
            boolean isMandatory = ((jsonOrder.get("mandatory")).equals("true"));
            Order order = new Order(orderId,isMandatory,isDelivery,
                    orderSize,installationId);
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
        JSONObject jsonInstance = getJSONObject(pathToInstanceFile);
        ProblemInstance.planningPeriodHours = (double) jsonInstance.get(Constants.PLANNING_PERIOD_KEY);
        ProblemInstance.discretizationParam = (double) jsonInstance.get(Constants.DISCRETIZATION_KEY);
        ProblemInstance.weatherScenario = (String) jsonInstance.get(Constants.WEATHER_SCENARIO_KEY);
        ProblemInstance.installationOrdering = (String) jsonInstance.get(Constants.INSTALLATION_ORDERING_KEY);
    }

    private static void setUpVesselInfo() {
        // Read: VESSEL_FILE

    }

    private static void setUpWeather() {
        // Read: WEATHER_FILE

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

    public static void main (String[]args){
        ProblemInstance.setUpProblem("example.json");

    }
}


