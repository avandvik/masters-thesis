package data;

public class Constants {

    public static String FILE_NAME;

    public static final String ROOT_PATH = System.getProperty("user.dir");
    public static String OUTPUT_PATH = ROOT_PATH + "/output/local/";
    public static String GENERATOR_PATH = ROOT_PATH + "/src/main/resources/generated/";
    public static String PATH_TO_INSTANCES_DIR = ROOT_PATH + "/src/main/resources/instances/";
    public static String PATH_TO_INSTANCE = PATH_TO_INSTANCES_DIR + FILE_NAME;
    public static String PATH_TO_CONSTANT_DIR = ROOT_PATH + "/src/main/resources/constant/";
    public static String PATH_TO_TEST_DIR = ROOT_PATH + "/src/main/resources/test/";
    public static String PATH_TO_VESSELS = Constants.PATH_TO_CONSTANT_DIR + "vessels.json";
    public static String PATH_TO_INSTALLATIONS = Constants.PATH_TO_CONSTANT_DIR + "installations.json";
    public static String PATH_TO_WEATHER = Constants.PATH_TO_CONSTANT_DIR + "weather.json";

    public static final double MAX_HEAP_SIZE = Math.round(Runtime.getRuntime().maxMemory() / 1e9 * 100.0) / 100.0;

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

    public static final String REMOVAL_RANDOM_NAME = "random_removal";
    public static final String REMOVAL_RELATED_NAME = "related_removal";
    public static final String REMOVAL_WORST_NAME = "worst_removal";
    public static final String REMOVAL_SPOT_NAME = "spot_removal";
    public static final String REMOVAL_SPREAD_NAME = "spread_removal";
    public static final String REMOVAL_CLUSTER_NAME = "cluster_removal";
    public static final String INSERTION_GREEDY_NAME = "greedy_insertion";
    public static final String INSERTION_REGRET_NAME = "regret_insertion";
    public static final String INSERTION_MAX_PENALTY_NAME = "max_penalty_cost_insertion";
    public static final String INSERTION_MAX_ORDER_SIZE_NAME = "max_order_size_insertion";

    public static final String INSTANCE_NAME_KEY = "instance";
    public static final String OBJECTIVE_VALUE_KEY = "best_objective";
    public static final String FUEL_COSTS_KEY = "best_objective_fuel_costs";
    public static final String PENALTY_COSTS_KEY = "best_objective_penalty_costs";
    public static final String VOYAGES_KEY = "voyages";
    public static final String SEQUENCE_KEY = "sequence";
    public static final String TIME_POINTS_KEY = "time_points";
    public static final String ARRIVAL_TIME_KEY = "arrival_time";
    public static final String SERVICE_TIME_KEY = "service_time";
    public static final String END_TIME_KEY = "end_time";
    public static final String ITER_TO_OBJ_KEY = "iteration_to_objective";
    public static final String HEURISTIC_TO_ITER_TO_WEIGHT_KEY = "heuristic_to_iteration_to_weight";
    public static final String HEURISTIC_TO_SELECTIONS_KEY = "heuristic_to_selections";
    public static final String ITER_BEST_FOUND_KEY = "best_sol_found_in_iteration";
    public static final String RUNTIME_KEY = "runtime";
    public static final String NBR_ITERATIONS_KEY = "number_of_iterations";
    public static final String NBR_LOCAL_SEARCH_RUNS_KEY = "number_of_local_search_runs";
    public static final String AVG_LOCAL_SEARCH_IMPROVEMENT_KEY = "avg_improvement_local_search";
    public static final String BEST_LOCAL_SEARCH_IMPROVEMENT_KEY = "best_improvement_local_search";
    public static final String NBR_IMPROVEMENTS_SP_KEY = "number_of_improvements_by_set_partitioning";
    public static final String NBR_IMPROVEMENTS_LS_KEY = "number_of_improvements_by_local_search";
    public static final String NBR_IMPROVEMENTS_DR_KEY = "number_of_improvements_by_destroy_repair";
    public static final String NBR_IMPROVEMENTS_LS_OPERATORS_KEY = "number_of_improvements_by_local_search_operators";
    public static final String INITIAL_SOLUTION_OBJECTIVE_KEY = "construction_heuristic_objective";
    public static final String BEST_SOLUTION_FOUND_BY_KEY = "best_solution_found_by";
    public static final String PARAMETERS_KEY = "parameters";
    public static final String NOISE_CONTROL_KEY = "noise_control";
    public static final String NEW_GLOBAL_SCORE_KEY = "new_global_score";
    public static final String NEW_LOCAL_SCORE_KEY = "new_local_score";
    public static final String NEW_SOLUTION_SCORE_KEY = "new_solution_score";
    public static final String REACTION_KEY = "reaction";
    public static final String START_TEMPERATURE_KEY = "start_temperature";
    public static final String COOLING_RATE_KEY = "cooling_rate";
    public static final String REMOVAL_UPPER_PERCENTAGE_KEY = "removal_upper_percentage";
    public static final String REMOVAL_LOWER_PERCENTAGE_KEY = "removal_lower_percentage";
    public static final String REGRET_KEY = "regret_k";
    public static final String DETERMINISM_KEY = "determinism";
    public static final String LOCAL_SEARCH_CONDITION_KEY = "local_search_condition";
    public static final String LOWER_WEIGHT_THRESHOLD_KEY = "lower_weight_threshold";
    public static final String PREDETERMINED_ITERATIONS_KEY = "predetermined_iterations";
    public static final String MAX_ITERATIONS_SOLUTION_KEY = "max_iterations_solution";
    public static final String SEGMENT_ITERATIONS_KEY = "segment_iterations";
    public static final String CACHE_SIZE_KEY = "cache_size";
    public static final String POOL_SIZE_KEY = "pool_size";
    public static final String LOCAL_SEARCH_KEY = "local_search";
    public static final String SET_PARTITIONING_KEY = "set_partitioning";

    public static final String ONE_EXCHANGE_NAME = "one_exchange";
    public static final String TWO_EXCHANGE_NAME = "two_exchange";
    public static final String ONE_RELOCATE_NAME = "one_relocate";
    public static final String TWO_RELOCATE_NAME = "two_relocate";
    public static final String POSTPONE_SCHEDULED_NAME = "postpone_scheduled";
    public static final String SCHEDULE_POSTPONED_NAME = "schedule_postponed";
    public static final String VOYAGE_EXCHANGE_NAME = "voyage_exchange";

    public static final String CONSTRUCTION_NAME = "construction_heuristic";
    public static final String LOCAL_SEARCH_NAME = "local_search";
    public static final String DESTROY_REPAIR_NAME = "destroy_repair";
    public static final String SET_PARTITIONING_NAME = "set_partitioning";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public static boolean SOLSTORM = false;

    public static void setSolstormConstants(String outputDir, String fileName) {
        Constants.FILE_NAME = fileName;
        Constants.OUTPUT_PATH = "/storage/users/anderhva/" + outputDir + "/";
        Constants.PATH_TO_INSTANCES_DIR = "/home/anderhva/masters-thesis/instances/";
        Constants.PATH_TO_CONSTANT_DIR = "/home/anderhva/masters-thesis/constant/";
        Constants.PATH_TO_VESSELS = Constants.PATH_TO_CONSTANT_DIR + "vessels.json";
        Constants.PATH_TO_INSTALLATIONS = Constants.PATH_TO_CONSTANT_DIR + "installations.json";
        Constants.PATH_TO_WEATHER = Constants.PATH_TO_CONSTANT_DIR + "weather.json";
        Constants.SOLSTORM = true;
    }
}
