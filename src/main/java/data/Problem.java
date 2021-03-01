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

public class Problem {

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
    public static double timeUnit;
    public static String weatherScenario;
    public static String installationOrdering;
    public static int preparationEndTime;
    public static double sqmInCargoUnit;

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
    public static int worstWeatherState;
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

    public static int getGeneralReturnTime() {
        return vessels.get(0).getReturnTime() * Problem.discretizationParam - 1;
    }

    public static int getFinalTimePoint() {
        return Problem.planningPeriodDisc - 1;
    }

    public static int getFirstTimePoint() {
        return 0;
    }

    public static int getEndOfDayTimePoint() {
        return 24 * Problem.discretizationParam - 1;
    }

    public static int getNumberOfVessels() {
        return Problem.vessels.size();
    }

    public static boolean isSpotVessel(Vessel vessel) {
        return Problem.vessels.indexOf(vessel) == Problem.getNumberOfVessels() - 1;
    }

    public static Vessel getVessel(int vesselNumber) {
        return Problem.vessels.get(vesselNumber);
    }

    public static double getSpeedImpact(int weatherState) {
        return Problem.wsToSpeedImpact.get(weatherState);
    }

    public static int getWorstWeatherState(int startTime, int endTime) {
        return Collections.max(Problem.weatherForecastDisc.subList(startTime, endTime + 1));
    }

    public static double hourToDiscDecimal(double timeHour) {
        return timeHour * Problem.discretizationParam;
    }

    public static double discTimeToHour(double timeDisc) {
        return timeDisc / Problem.discretizationParam;
    }

    public static int hourToDiscTimePoint(double timeHour) {
        return (int) Math.floor(hourToDiscDecimal(timeHour));
    }

    public static int discToDiscDayTime(int timeDisc) {
        return timeDisc % (24 * Problem.discretizationParam);
    }

    // TODO: Refactor at next big merge
    public static void setUpProblem(String fileName, boolean isTest) {
        Problem.fileName = fileName;
        Problem.pathToInstanceFile = (isTest ? Constants.PATH_TO_TEST : Constants.PATH_TO_INSTANCE) + fileName;
        setUpProblem();
    }

    public static void setUpProblem(String fileName) {
        Problem.fileName = fileName;
        Problem.pathToInstanceFile = Constants.PATH_TO_INSTANCE + fileName;
        setUpInstanceInfo();
        setUpInstallations();
        setUpVesselInfo();
        setUpVessels();
        setUpWeather();
        setUpOrders();
    }

    private static void setUpProblem() {
        setUpInstanceInfo();
        setUpInstallations();
        setUpVesselInfo();
        setUpVessels();
        setUpWeather();
        setUpOrders();
    }

    private static void setUpInstallations() {
        Problem.installations = new ArrayList<>();
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
            Problem.installations.add(installation);
        }
        Collections.sort(Problem.installations);
    }

    private static void setUpOrders() {
        Problem.orders = new ArrayList<>();
        JSONObject jsonOrders = (JSONObject) getJSONObject(pathToInstanceFile).get(Constants.ORDERS_KEY);
        int orderId = 0;
        for (Object key : jsonOrders.keySet()) {
            JSONObject jsonOrder = (JSONObject) jsonOrders.get(key);
            double orderSizeSqm = (double) jsonOrder.get(Constants.ORDER_SIZE_KEY);
            int orderSizeUnits = (int) Math.ceil(orderSizeSqm / Problem.sqmInCargoUnit);
            int installationId = Math.toIntExact((long) jsonOrder.get(Constants.INSTALLATION_KEY));
            boolean isDelivery = ((jsonOrder.get(Constants.TRANSPORTATION_TYPE_KEY)).equals(Constants.DELIVERY_KEY));
            boolean isMandatory = ((jsonOrder.get(Constants.MANDATORY_KEY)).equals(Constants.TRUE_KEY));
            Order order = new Order(orderId, isMandatory, isDelivery, orderSizeUnits, installationId);
            Problem.orders.add(order);
            orderId++;
        }
    }

    private static void setUpVessels() {
        Problem.vessels = new ArrayList<>();
        JSONObject jsonVessels = (JSONObject) getJSONObject(Constants.VESSEL_FILE).get(Constants.FLEET_KEY);
        JSONObject jsonAvailableVessels =
                (JSONObject) getJSONObject(pathToInstanceFile).get(Constants.AVAILABLE_VESSELS_KEY);
        for (Object key : jsonAvailableVessels.keySet()) {
            String name = (String) key;
            JSONObject jsonVessel = (JSONObject) jsonVessels.get(key);
            int id = Math.toIntExact((long) jsonVessel.get(Constants.ID_KEY));
            double capacity = (double) jsonVessel.get(Constants.CAPACITY_KEY);
            int returnTime =
                    Math.toIntExact((long) ((JSONObject) jsonAvailableVessels.get(key)).get(Constants.RETURN_TIME_KEY));
            Vessel vessel = new Vessel(id, name, capacity, returnTime);
            Problem.vessels.add(vessel);
        }
        Collections.sort(Problem.vessels);
    }

    private static void setUpInstanceInfo() {
        JSONObject jsonInstanceInfo = getJSONObject(Problem.pathToInstanceFile);
        Problem.planningPeriodHours = ((int) ((double) jsonInstanceInfo.get(Constants.PLANNING_PERIOD_KEY)));
        Problem.discretizationParam = ((int) ((double) jsonInstanceInfo.get(Constants.DISCRETIZATION_KEY)));
        Problem.timeUnit = (double) 1 / Problem.discretizationParam;
        Problem.weatherScenario = (String) jsonInstanceInfo.get(Constants.WEATHER_SCENARIO_KEY);
        Problem.installationOrdering = (String) jsonInstanceInfo.get(Constants.INSTALLATION_ORDERING_KEY);

        Problem.planningPeriodDisc = Problem.planningPeriodHours * Problem.discretizationParam;
    }

    private static void setUpVesselInfo() {
        JSONObject jsonVesselInfo = getJSONObject(Constants.VESSEL_FILE);
        Problem.minSpeed = (double) jsonVesselInfo.get(Constants.MIN_SPEED_KEY);
        Problem.designSpeed = (double) jsonVesselInfo.get(Constants.DESIGN_SPEED_KEY);
        Problem.maxSpeed = (double) jsonVesselInfo.get(Constants.MAX_SPEED_KEY);
        Problem.fcDesignSpeed = (double) jsonVesselInfo.get(Constants.FC_DESIGN_SPEED_KEY);
        Problem.fcDepot = (double) jsonVesselInfo.get(Constants.FC_DEPOT_KEY);
        Problem.fcIdling = (double) jsonVesselInfo.get(Constants.FC_IDLING_KEY);
        Problem.fcServicing = (double) jsonVesselInfo.get(Constants.FC_SERVICING_KEY);
        Problem.fuelPrice = (double) jsonVesselInfo.get(Constants.FUEL_PRICE_KEY);
        Problem.spotHourRate = (double) jsonVesselInfo.get(Constants.SPOT_HOUR_RATE_KEY);
        double realServiceTimeUnit = (double) jsonVesselInfo.get(Constants.SERVICE_TIME_KEY);
        Problem.discServiceTimeUnit = realServiceTimeUnit * Problem.discretizationParam;
        int preparationEndHour = Math.toIntExact((long) jsonVesselInfo.get(Constants.PREP_END_KEY));
        Problem.preparationEndTime = preparationEndHour * Problem.discretizationParam - 1;
        Problem.sqmInCargoUnit = (double) jsonVesselInfo.get(Constants.SQM_KEY);
    }

    private static void setUpWeather() {
        Problem.wsToSpeedImpact = new HashMap<>();
        Problem.wsToServiceImpact = new HashMap<>();
        JSONObject jsonWeather = getJSONObject(Constants.WEATHER_FILE);
        Problem.worstWeatherState = Math.toIntExact((long) jsonWeather.get(Constants.WORST_WEATHER_KEY));
        JSONObject jsonWS = (JSONObject) jsonWeather.get(Constants.SCENARIOS_KEY);
        JSONArray jsonWeatherForecast = (JSONArray) jsonWS.get(Problem.weatherScenario);
        Problem.processWeatherForecast(jsonWeatherForecast);
        Problem.createDiscWeatherForecast();
        JSONObject jsonSpeedImpact = (JSONObject) jsonWeather.get(Constants.SPEED_IMPACT_KEY);
        JSONObject jsonServiceImpact = (JSONObject) jsonWeather.get(Constants.SERVICE_IMPACT_KEY);
        addToHashMap(jsonSpeedImpact, Problem.wsToSpeedImpact);
        addToHashMap(jsonServiceImpact, Problem.wsToServiceImpact);
    }

    private static void processWeatherForecast(JSONArray jsonWeatherForecast) {
        Problem.weatherForecast = new ArrayList<>();
        for (Object objWeatherState : jsonWeatherForecast) {
            int weatherState = Math.toIntExact((long) objWeatherState);
            Problem.weatherForecast.add(weatherState);
        }
    }

    private static void createDiscWeatherForecast() {
        Problem.weatherForecastDisc = new ArrayList<>();
        for (int i = 0; i < Problem.weatherForecast.size() * Problem.discretizationParam; i++) {
            int idx = i / Problem.discretizationParam;
            Problem.weatherForecastDisc.add(Problem.weatherForecast.get(idx));
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
        Problem.setUpProblem("example.json");
    }
}


