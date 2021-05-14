package utils;

import alns.Solution;
import data.Constants;
import data.Parameters;
import data.Problem;
import data.SearchHistory;
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
        obj.put(Constants.INSTANCE_NAME_KEY, Constants.FILE_NAME);
        obj.put(Constants.OBJECTIVE_VALUE_KEY, solution.getObjective(false));
        obj.put(Constants.VOYAGES_KEY, new JSONObject());
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            Vessel vessel = Problem.getVessel(vesselIdx);
            ((JSONObject) obj.get(Constants.VOYAGES_KEY)).put(vessel, new JSONObject());
            JSONArray orderSequence = new JSONArray();
            for (Order order : solution.getOrderSequence(vesselIdx)) orderSequence.add(order.getOrderId());
            JSONObject vesselObj = ((JSONObject) ((JSONObject) obj.get(Constants.VOYAGES_KEY)).get(vessel));
            vesselObj.put(Constants.SEQUENCE_KEY, orderSequence);
            vesselObj.put(Constants.TIME_POINTS_KEY, new JSONObject());
            Node prevNode = null;
            for (Node node : solution.getShortestPaths().get(vesselIdx)) {
                JSONObject timePoints = new JSONObject();
                if (node.getOrder() != null) {
                    timePoints.put(Constants.ARRIVAL_TIME_KEY, node.getArrTime(prevNode));
                    timePoints.put(Constants.SERVICE_TIME_KEY, node.getServiceStartTime(prevNode));
                    timePoints.put(Constants.END_TIME_KEY, node.getDiscreteTime());
                    int orderId = node.getOrder().getOrderId();
                    ((JSONObject) vesselObj.get(Constants.TIME_POINTS_KEY)).put(orderId, timePoints);
                } else {
                    timePoints.put(Constants.END_TIME_KEY, node.getDiscreteTime());
                    boolean isStartDepot = node.getDiscreteTime() == Problem.preparationEndTime;
                    String depotName = isStartDepot ? "SD" : "ED";
                    ((JSONObject) vesselObj.get(Constants.TIME_POINTS_KEY)).put(depotName, timePoints);
                }
                prevNode = node;
            }
        }
        String baseName = Constants.FILE_NAME.substring(0, Constants.FILE_NAME.lastIndexOf("."));
        String path = Constants.OUTPUT_PATH + baseName + "_" + Problem.currentSeed + "_solution.json";
        writeToFile(path, obj);
    }

    @SuppressWarnings("unchecked")
    public static void saveSearchHistory() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.INSTANCE_NAME_KEY, Constants.FILE_NAME);
        obj.put(Constants.OBJECTIVE_VALUE_KEY, SearchHistory.getBestObjective());
        obj.put(Constants.ITER_TO_OBJ_KEY, SearchHistory.getIterationToObjective());
        obj.put(Constants.HEURISTIC_TO_ITER_TO_WEIGHT_KEY, SearchHistory.getHeuristicToIterationToWeight());
        obj.put(Constants.ITER_BEST_FOUND_KEY, SearchHistory.getIterationBestSolutionFound());
        obj.put(Constants.RUNTIME_KEY, SearchHistory.getRuntime());
        obj.put(Constants.NBR_ITERATIONS_KEY, SearchHistory.getNbrIterations());

        JSONObject parametersObj = new JSONObject();
        parametersObj.put(Constants.NOISE_CONTROL_KEY, Parameters.noiseRate);
        parametersObj.put(Constants.NEW_GLOBAL_SCORE_KEY, Parameters.newGlobalBest);
        parametersObj.put(Constants.NEW_LOCAL_SCORE_KEY, Parameters.newLocalImprovement);
        parametersObj.put(Constants.NEW_SOLUTION_SCORE_KEY, Parameters.newLocal);
        parametersObj.put(Constants.LOWER_WEIGHT_THRESHOLD_KEY, Parameters.initialWeight);
        parametersObj.put(Constants.REACTION_KEY, Parameters.reaction);
        parametersObj.put(Constants.START_TEMPERATURE_KEY, Parameters.startTemperature);
        parametersObj.put(Constants.COOLING_RATE_KEY, Parameters.coolingRate);
        parametersObj.put(Constants.REMOVAL_UPPER_PERCENTAGE_KEY, 0.5);
        parametersObj.put(Constants.REMOVAL_LOWER_PERCENTAGE_KEY, 0.0);
        parametersObj.put(Constants.REGRET_KEY, Parameters.regretParameter);
        parametersObj.put(Constants.DETERMINISM_RELATED_KEY, Parameters.rnRelated);
        parametersObj.put(Constants.DETERMINISM_WORST_KEY, Parameters.rnWorst);
        parametersObj.put(Constants.LOCAL_SEARCH_CONDITION_KEY, Parameters.localSearchCondition);
        parametersObj.put(Constants.PREDETERMINED_ITERATIONS_KEY, Parameters.totalIter);
        parametersObj.put(Constants.MAX_ITERATIONS_SOLUTION_KEY, Parameters.maxIterSolution);
        parametersObj.put(Constants.SET_PARTITIONING_ITERATIONS_KEY, Parameters.setPartIter);
        parametersObj.put(Constants.SEGMENT_ITERATIONS_KEY, Parameters.segmentIter);
        parametersObj.put(Constants.CACHE_SIZE_KEY, Parameters.cacheSize);
        parametersObj.put(Constants.POOL_SIZE_KEY, Parameters.poolSize);
        parametersObj.put(Constants.LOCAL_SEARCH_KEY, Parameters.localSearch);
        parametersObj.put(Constants.SET_PARTITIONING_KEY, Parameters.setPartitioning);
        obj.put(Constants.PARAMETERS_KEY, parametersObj);

        String baseName = Constants.FILE_NAME.substring(0, Constants.FILE_NAME.lastIndexOf("."));
        String path = Constants.OUTPUT_PATH + baseName + "_" + Problem.currentSeed + "_history.json";
        writeToFile(path, obj);
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
        JSONObject jsonInstallations = getJSONObject(Constants.PATH_TO_INSTALLATIONS);
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
        JSONObject jsonOrders = (JSONObject) getJSONObject(Constants.PATH_TO_INSTANCE).get(Constants.ORDERS_KEY);
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
        String vesselPath = Constants.PATH_TO_VESSELS;
        String instancePath = Constants.PATH_TO_INSTANCE;
        JSONObject vessels = (JSONObject) getJSONObject(vesselPath).get(Constants.FLEET_KEY);
        JSONObject availVessels = (JSONObject) getJSONObject(instancePath).get(Constants.AVAILABLE_VESSELS_KEY);
        for (Object key : availVessels.keySet()) {
            String name = (String) key;
            JSONObject jsonVessel = (JSONObject) vessels.get(key);
            int id = Math.toIntExact((long) jsonVessel.get(Constants.ID_KEY));
            double capacitySqm = (double) jsonVessel.get(Constants.CAPACITY_KEY);
            int capacityUnits = (int) Math.floor(capacitySqm / Problem.sqmInCargoUnit);
            double fcDesignSpeed = (double) jsonVessel.get(Constants.FC_DESIGN_SPEED_KEY);
            int retTime = Math.toIntExact((long) ((JSONObject) availVessels.get(key)).get(Constants.RETURN_TIME_KEY));
            Vessel vessel = new Vessel(id, name, capacityUnits, fcDesignSpeed, retTime);
            Problem.vessels.add(vessel);
        }
        Collections.sort(Problem.vessels);
    }

    public static void setUpInstanceInfo() {
        JSONObject jsonInstanceInfo = getJSONObject(Constants.PATH_TO_INSTANCE);
        Problem.planningPeriodHours = ((int) ((double) jsonInstanceInfo.get(Constants.PLANNING_PERIOD_KEY)));
        Problem.discretizationParam = ((int) ((double) jsonInstanceInfo.get(Constants.DISCRETIZATION_KEY)));
        Problem.timeUnit = (double) 1 / Problem.discretizationParam;
        Problem.weatherScenario = (String) jsonInstanceInfo.get(Constants.WEATHER_SCENARIO_KEY);
        Problem.installationOrdering = (String) jsonInstanceInfo.get(Constants.INSTALLATION_ORDERING_KEY);
        Problem.planningPeriodDisc = Problem.planningPeriodHours * Problem.discretizationParam;
    }

    public static void setUpVesselInfo() {
        JSONObject jsonVesselInfo = getJSONObject(Constants.PATH_TO_VESSELS);
        Problem.minSpeed = (double) jsonVesselInfo.get(Constants.MIN_SPEED_KEY);
        Problem.designSpeed = (double) jsonVesselInfo.get(Constants.DESIGN_SPEED_KEY);
        Problem.maxSpeed = (double) jsonVesselInfo.get(Constants.MAX_SPEED_KEY);
        Problem.fcDepot = (double) jsonVesselInfo.get(Constants.FC_DEPOT_KEY);
        Problem.fcIdling = (double) jsonVesselInfo.get(Constants.FC_IDLING_KEY);
        Problem.fcServicing = (double) jsonVesselInfo.get(Constants.FC_SERVICING_KEY);
        Problem.fuelPrice = (double) jsonVesselInfo.get(Constants.FUEL_PRICE_KEY);
        Problem.spotHourRate = (double) jsonVesselInfo.get(Constants.SPOT_HOUR_RATE_KEY);
        double realServiceTimeUnit = (double) jsonVesselInfo.get(Constants.UNIT_SERVICE_TIME_KEY);
        Problem.discServiceTimeUnit = realServiceTimeUnit * Problem.discretizationParam;
        int preparationEndHour = Math.toIntExact((long) jsonVesselInfo.get(Constants.PREP_END_KEY));
        Problem.preparationEndTime = preparationEndHour * Problem.discretizationParam - 1;
        Problem.sqmInCargoUnit = (double) jsonVesselInfo.get(Constants.SQM_KEY);
    }

    public static void setUpWeather() {
        Problem.wsToSpeedImpact = new HashMap<>();
        Problem.wsToServiceImpact = new HashMap<>();
        JSONObject jsonWeather = getJSONObject(Constants.PATH_TO_WEATHER);
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
        try {
            return (JSONObject) jsonParser.parse(new FileReader(path));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
