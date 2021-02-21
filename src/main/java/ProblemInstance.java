import objects.Installation;
import objects.Order;
import objects.Vessel;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProblemInstance {

    private final String fileName;
    private final String pathToInstanceFile;

    // Instance objects
    private static List<Installation> installations;
    private static List<Order> orders;
    private static List<Vessel> vessels;

    // Instance info
    private static int planningPeriodHours;
    private static int discretizationParam;
    private static String weatherScenario;
    private static String installationOrdering;

    // Vessel info
    private static double minSpeed;
    private static double designSpeed;
    private static double maxSpeed;
    private static double fcDesignSpeed;
    private static double fcDepot;
    private static double fcIdling;
    private static double fcServicing;
    private static double fuelPrice;
    private static double spotHourRate;
    private static double realServiceTimeUnit;

    // Weather
    private static Map<Integer, Integer> wsToSpeedImpact;
    private static Map<Integer, Integer> wsToServiceImpact;
    private static List<Integer> weatherForecast;


    public ProblemInstance(String fileName) {
        this.fileName = fileName;
        this.pathToInstanceFile = Constants.PATH_TO_INSTANCE + fileName;
    }

    public void setUpProblem() {
        setUpInstallations();
        setUpOrders();
        setUpInstanceInfo();
        setUpVessels();
        setUpVesselInfo();
        setUpWeather();
    }

    private void setUpInstallations() {
        Object jsonInstallations = getJSONObject(Constants.INSTALLATION_FILE);
        System.out.println(jsonInstallations);
    }

    private void setUpOrders() {
        // Read: fileName
        JSONParser jsonParser = new JSONParser();



    }

    private void setUpVessels() {
        // Read: VESSEL_FILE and fileName

        // Add return time to each vessel object after they are created
    }

    private void setUpInstanceInfo() {
        // Read: fileName

    }

    private void setUpVesselInfo() {
        // Read: VESSEL_FILE

    }

    private void setUpWeather() {
        // Read: WEATHER_FILE

    }

    private Object getJSONObject(String path) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(path)) {
            return jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
    }
}
