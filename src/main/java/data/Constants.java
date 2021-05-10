package data;

public class Constants {
    public static final String ROOT_PATH = System.getProperty("user.dir");

    public static String OUTPUT_PATH = ROOT_PATH + "/output/local/";
    public static String GENERATOR_PATH = ROOT_PATH + "/src/main/resources/generated/";

    public static String PATH_TO_INSTANCES = ROOT_PATH + "/src/main/resources/instances/";
    public static String PATH_TO_CONSTANT = ROOT_PATH + "/src/main/resources/constant/";
    public static String PATH_TO_TEST = ROOT_PATH + "/src/main/resources/test/";

    public static String VESSEL_FILE = Constants.PATH_TO_CONSTANT + "vessels.json";
    public static String INSTALLATION_FILE = Constants.PATH_TO_CONSTANT + "installations.json";
    public static String WEATHER_FILE = Constants.PATH_TO_CONSTANT + "weather.json";

    public static final String ID_KEY = "id";
    public static final String OPENING_HOUR_KEY = "opening_hour";
    public static final String CLOSING_HOUR_KEY = "closing_hour";
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String STD_DEMAND_MD_KEY = "std_size_MD";
    public static final String STD_DEMAND_OD_KEY = "std_size_OD";
    public static final String STD_DEMAND_OP_KEY = "std_size_OP";
    public static final String CAPACITY_KEY = "capacity";
    public static final String AVAILABLE_VESSELS_KEY = "available_vessels";
    public static final String FLEET_KEY = "fleet";
    public static final String SPOT_VESSEL_KEY = "SPOT";
    public static final String RETURN_TIME_KEY = "return_time";
    public static final String PLANNING_PERIOD_KEY = "planning_period_hours";
    public static final String DISCRETIZATION_KEY = "discretization_parameter";
    public static final String WEATHER_SCENARIO_KEY = "weather_scenario";
    public static final String INSTALLATION_ORDERING_KEY = "installation_ordering";
    public static final String RANDOM_SEED_KEY = "random_seed";
    public static final String MIN_SPEED_KEY = "min_speed";
    public static final String DESIGN_SPEED_KEY = "design_speed";
    public static final String MAX_SPEED_KEY = "max_speed";
    public static final String FC_DESIGN_SPEED_KEY = "fc_design_speed";
    public static final String FC_DEPOT_KEY = "fc_depot";
    public static final String FC_IDLING_KEY = "fc_idling";
    public static final String FC_SERVICING_KEY = "fc_servicing";
    public static final String FUEL_PRICE_KEY = "fuel_price";
    public static final String SPOT_HOUR_RATE_KEY = "spot_hour_rate";
    public static final String UNIT_SERVICE_TIME_KEY = "real_service_time_per_unit";
    public static final String PREP_END_KEY = "preparation_end_hour";
    public static final String SQM_KEY = "square_meters_in_one_cargo_unit";

    public static final String ORDERS_KEY = "orders";
    public static final String ORDER_SIZE_KEY = "size";
    public static final String INSTALLATION_KEY = "installation";
    public static final String TRANSPORT_KEY = "transport";
    public static final String DELIVERY_VALUE = "delivery";
    public static final String PICKUP_VALUE = "pickup";
    public static final String MANDATORY_KEY = "mandatory";
    public static final String TRUE_VALUE = "True";
    public static final String FALSE_VALUE = "False";
    public static final String WORST_WEATHER_KEY = "worst_weather_state";
    public static final String SCENARIOS_KEY = "scenarios";
    public static final String SPEED_IMPACT_KEY = "speed_impact";
    public static final String SERVICE_IMPACT_KEY = "service_impact";

    public static final String REMOVAL_RANDOM_NAME = "random removal";
    public static final String REMOVAL_RELATED_NAME = "related removal";
    public static final String REMOVAL_WORST_NAME = "worst removal";
    public static final String REMOVAL_SPOT_NAME = "spot removal";
    public static final String REMOVAL_SPREAD_NAME = "spread removal";
    public static final String REMOVAL_CLUSTER_NAME = "cluster removal";
    public static final String INSERTION_GREEDY_NAME = "greedy insertion";
    public static final String INSERTION_REGRET_NAME = "regret insertion";
    public static final String INSERTION_MAX_PENALTY_NAME = "max penalty cost insertion";
    public static final String INSERTION_MAX_ORDER_SIZE_NAME = "max order size insertion";

    public static final String INSTANCE_NAME_KEY = "instance";
    public static final String OBJECTIVE_VALUE_KEY = "best_objective";
    public static final String VOYAGES_KEY = "voyages";
    public static final String SEQUENCE_KEY = "sequence";
    public static final String TIME_POINTS_KEY = "time_points";
    public static final String ARRIVAL_TIME_KEY = "arrival_time";
    public static final String SERVICE_TIME_KEY = "service_time";
    public static final String END_TIME_KEY = "end_time";
    public static final String ITER_TO_OBJ_KEY = "iteration_to_objective";
    public static final String HEURISTIC_TO_ITER_TO_WEIGHT_KEY = "heuristic_to_iteration_to_weight";
    public static final String ITER_BEST_FOUND_KEY = "best_sol_found_in_iteration";
    public static final String RUNTIME_KEY = "runtime";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public static boolean SOLSTORM = false;

    public static void overwritePathsSolstorm(String outputDir) {
        Constants.OUTPUT_PATH = "/storage/users/anderhva/" + outputDir + "/";
        Constants.PATH_TO_INSTANCES = "/home/anderhva/masters-thesis/instances/";
        Constants.PATH_TO_CONSTANT = "/home/anderhva/masters-thesis/constant/";
        Constants.VESSEL_FILE = Constants.PATH_TO_CONSTANT + "vessels.json";
        Constants.INSTALLATION_FILE = Constants.PATH_TO_CONSTANT + "installations.json";
        Constants.WEATHER_FILE = Constants.PATH_TO_CONSTANT + "weather.json";
        Constants.SOLSTORM = true;
    }
}
