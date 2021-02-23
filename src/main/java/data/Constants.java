package data;

public class Constants {
    public static final String PATH_TO_CONSTANT = System.getProperty("user.dir") + "/src/main/resources/constant/";
    public static final String PATH_TO_INSTANCE = System.getProperty("user.dir") + "/src/main/resources/instance/";

    public static final String VESSEL_FILE = Constants.PATH_TO_CONSTANT + "vessels.json";
    public static final String INSTALLATION_FILE = Constants.PATH_TO_CONSTANT + "installations.json";
    public static final String WEATHER_FILE = Constants.PATH_TO_CONSTANT + "weather.json";

    public static final String ID_KEY = "id";
    public static final String OPENING_HOUR_KEY = "opening_hour";
    public static final String CLOSING_HOUR_KEY = "closing_hour";
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String TYPICAL_DEMAND_KEY = "typical_demand";
    public static final String CAPACITY_KEY = "capacity";
    public static final String AVAILABLE_VESSELS_KEY = "available_vessels";
    public static final String FLEET_KEY = "fleet";
    public static final String RETURN_TIME_KEY = "return_time";
    public static final String PLANNING_PERIOD_KEY = "planning_period_hours";
    public static final String DISCRETIZATION_KEY = "discretization_parameter";
    public static final String WEATHER_SCENARIO_KEY = "weather_scenario";
    public static final String INSTALLATION_ORDERING_KEY = "installation_ordering";
    public static final String MIN_SPEED_KEY = "min_speed";
    public static final String DESIGN_SPEED_KEY = "design_speed";
    public static final String MAX_SPEED_KEY = "max_speed";
    public static final String FC_DESIGN_SPEED_KEY = "fc_design_speed";
    public static final String FC_DEPOT_KEY = "fc_depot";
    public static final String FC_IDLING_KEY = "fc_idling";
    public static final String FC_SERVICING_KEY = "fc_servicing";
    public static final String FUEL_PRICE_KEY = "fuel_price";
    public static final String SPOT_HOUR_RATE_KEY = "spot_hour_rate";
    public static final String SERVICE_TIME_KEY = "real_service_time_per_unit";
    public static final String PREP_END_KEY = "preparation_end_hour";

    public static final String ORDERS_KEY = "orders";
    public static final String ORDER_SIZE_KEY = "size";
    public static final String INSTALLATION_KEY = "installation";
    public static final String TRANSPORTATION_TYPE_KEY = "transport";
    public static final String DELIVERY_KEY = "delivery";
    public static final String MANDATORY_KEY = "mandatory";
    public static final String TRUE_KEY = "true";
    public static final String WORST_WEATHER_KEY = "worst_weather_state";
    public static final String SCENARIOS_KEY = "scenarios";
    public static final String SPEED_IMPACT_KEY = "speed_impact";
    public static final String SERVICE_IMPACT_KEY = "service_impact";
}
