package utils;

import alns.Solution;
import data.Constants;
import data.Problem;
import objects.Installation;
import objects.Order;
import objects.Vessel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import subproblem.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IO {

    @SuppressWarnings("unchecked")
    public static void saveSolution(Solution solution) {
        JSONObject obj = new JSONObject();

        obj.put("instance", Problem.fileName);
        obj.put("objective", solution.getFitness(false));
        obj.put("voyages", new JSONObject());

        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            Vessel vessel = Problem.getVessel(vesselIdx);
            ((JSONObject) obj.get("voyages")).put(vessel, new JSONObject());

            JSONArray orderSequence = new JSONArray();
            for (Order order : solution.getOrderSequence(vesselIdx)) orderSequence.add(order.getOrderId());
            ((JSONObject) ((JSONObject) obj.get("voyages")).get(vessel)).put("sequence", orderSequence);

            ((JSONObject) ((JSONObject) obj.get("voyages")).get(vessel)).put("time_points", new JSONObject());
            for (Node node : solution.getShortestPaths().get(vesselIdx)) {
                JSONObject timePointsOrder = new JSONObject();
                if (node.getOrder() != null) {
                    timePointsOrder.put("arrival_time", node.getArrTime());
                    timePointsOrder.put("service_time", node.getServiceStartTime());
                    timePointsOrder.put("end_time", node.getDiscreteTime());
                    ((JSONObject) ((JSONObject) ((JSONObject) obj.get("voyages"))
                            .get(vessel)).get("time_points")).put(node.getOrder().getOrderId(), timePointsOrder);
                } else {
                    timePointsOrder.put("end_time", node.getDiscreteTime());
                    boolean isStartDepot = node.getDiscreteTime() == Problem.preparationEndTime;
                    ((JSONObject) ((JSONObject) ((JSONObject) obj.get("voyages"))
                            .get(vessel)).get("time_points")).put(isStartDepot ? "SD" : "ED", timePointsOrder);
                }
            }
        }
        writeToFile(Constants.OUTPUT_PATH + Problem.fileName, obj);
    }


    public static void writeToFile(String path, JSONObject obj) {
        try {
            FileWriter file = new FileWriter(path);
            file.write(obj.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void setUpInstallations() {
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
            Installation installation = new Installation(id, name, openingHour, closingHour, latitude, longitude);
            Problem.installations.add(installation);
        }
        Collections.sort(Problem.installations);
    }

    public static void setUpOrders() {
        Problem.orders = new ArrayList<>();
        JSONObject jsonOrders = (JSONObject) getJSONObject(Problem.pathToInstanceFile).get(Constants.ORDERS_KEY);
        for (Object key : jsonOrders.keySet()) {
            JSONObject jsonOrder = (JSONObject) jsonOrders.get(key);
            int orderId = Integer.parseInt((String) key);
            double orderSizeSqm = (double) jsonOrder.get(Constants.ORDER_SIZE_KEY);
            int orderSizeUnits = (int) Math.ceil(orderSizeSqm / Problem.sqmInCargoUnit);
            int installationId = Math.toIntExact((long) jsonOrder.get(Constants.INSTALLATION_KEY));
            boolean isDelivery = ((jsonOrder.get(Constants.TRANSPORT_KEY)).equals(Constants.DELIVERY_VALUE));
            boolean isMandatory = ((jsonOrder.get(Constants.MANDATORY_KEY)).equals(Constants.TRUE_VALUE));
            Order order = new Order(orderId, isMandatory, isDelivery, orderSizeUnits, installationId);
            Problem.orders.add(order);
        }
        Collections.sort(Problem.orders);
    }

    public static void setUpVessels() {
        Problem.vessels = new ArrayList<>();
        JSONObject jsonVessels = (JSONObject) getJSONObject(Constants.VESSEL_FILE).get(Constants.FLEET_KEY);
        JSONObject jsonAvailableVessels =
                (JSONObject) getJSONObject(Problem.pathToInstanceFile).get(Constants.AVAILABLE_VESSELS_KEY);
        for (Object key : jsonAvailableVessels.keySet()) {
            String name = (String) key;
            JSONObject jsonVessel = (JSONObject) jsonVessels.get(key);
            int id = Math.toIntExact((long) jsonVessel.get(Constants.ID_KEY));
            double capacitySqm = (double) jsonVessel.get(Constants.CAPACITY_KEY);
            int capacityUnits = (int) Math.floor(capacitySqm / Problem.sqmInCargoUnit);
            int returnTime =
                    Math.toIntExact((long) ((JSONObject) jsonAvailableVessels.get(key)).get(Constants.RETURN_TIME_KEY));
            Vessel vessel = new Vessel(id, name, capacityUnits, returnTime);
            Problem.vessels.add(vessel);
        }
        Collections.sort(Problem.vessels);
    }

    public static void setUpInstanceInfo() {
        JSONObject jsonInstanceInfo = getJSONObject(Problem.pathToInstanceFile);
        Problem.planningPeriodHours = ((int) ((double) jsonInstanceInfo.get(Constants.PLANNING_PERIOD_KEY)));
        Problem.discretizationParam = ((int) ((double) jsonInstanceInfo.get(Constants.DISCRETIZATION_KEY)));
        Problem.timeUnit = (double) 1 / Problem.discretizationParam;
        Problem.weatherScenario = (String) jsonInstanceInfo.get(Constants.WEATHER_SCENARIO_KEY);
        Problem.installationOrdering = (String) jsonInstanceInfo.get(Constants.INSTALLATION_ORDERING_KEY);
        Problem.planningPeriodDisc = Problem.planningPeriodHours * Problem.discretizationParam;
    }

    public static void setUpVesselInfo() {
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

    public static void setUpWeather() {
        Problem.wsToSpeedImpact = new HashMap<>();
        Problem.wsToServiceImpact = new HashMap<>();
        JSONObject jsonWeather = getJSONObject(Constants.WEATHER_FILE);
        Problem.worstWeatherState = Math.toIntExact((long) jsonWeather.get(Constants.WORST_WEATHER_KEY));
        JSONObject jsonWS = (JSONObject) jsonWeather.get(Constants.SCENARIOS_KEY);
        JSONArray jsonWeatherForecast = (JSONArray) jsonWS.get(Problem.weatherScenario);
        processWeatherForecast(jsonWeatherForecast);
        createDiscWeatherForecast();
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

    public static JSONObject getJSONObject(String path) {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = IO.class.getResourceAsStream(path);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
