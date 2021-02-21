

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
}
