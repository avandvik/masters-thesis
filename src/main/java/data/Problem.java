package data;

import arcs.ArcGenerator;
import objects.Installation;
import objects.Order;
import objects.Vessel;
import utils.DistanceCalculator;
import utils.IO;

import java.util.*;
import java.util.stream.Collectors;

public class Problem {

    /* =========== PROBLEM PARAMETERS =========== */
    // Instance objects
    public static List<Installation> installations;
    public static List<Order> orders;
    public static List<Vessel> vessels;

    // Instance info
    public static int planningPeriodHours;
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

    // Random object
    public static Random random;
    public static int currentSeed;

    // Speed optimization
    public static boolean speedOpt;


    /* =========== INSTALLATION =========== */

    public static Installation getDepot() {
        return installations.get(0);
    }

    public static Installation getInstallation(Order order) {
        return installations.get(order.getInstallationId());
    }

    public static double findMaxDistance() {
        double maxDistance = Double.NEGATIVE_INFINITY;
        for (Installation fromInst : installations) {
            for (Installation toInst : installations) {
                double distance = DistanceCalculator.distance(fromInst, toInst, "N");
                if (distance > maxDistance) maxDistance = distance;
            }
        }
        return maxDistance;
    }

    public static boolean instHasODOP(Installation inst) {
        List<Order> orders = getOrdersFromInstallation(inst);
        boolean hasOD = false;
        boolean hasOP = false;
        for (Order order : orders) {
            if (order.isMandatory()) return false;
            if (!order.isMandatory() && order.isDelivery()) hasOD = true;
            if (!order.isMandatory() && !order.isDelivery()) hasOP = true;
        }
        return hasOD && hasOP;
    }

    public static boolean instHasMD(Installation inst) {
        List<Order> orders = getOrdersFromInstallation(inst);
        for (Order order : orders) {
            if (order.isMandatory()) return true;
        }
        return false;
    }

    /* =========== ORDERS =========== */

    public static Order getOrder(int orderIndex) {
        return Problem.orders.get(orderIndex);
    }

    public static List<Order> getOrdersFromInstallation(Installation installation) {
        return Problem.orders.stream().filter(o -> o.getInstallationId() == installation.getId()).collect(Collectors.toList());
    }

    public static List<Order> getScheduledOrdersInst(Installation inst, Collection<Order> postponed) {
        return Problem.orders.stream()
                .filter(o -> o.getInstallationId() == inst.getId() && !postponed.contains(o))
                .collect(Collectors.toList());
    }

    public static Order getMandatoryOrder(Order optionalOrder) {
        List<Order> orders = getOrdersFromInstallation(getInstallation(optionalOrder));
        for (Order order : orders) {
            if (order.isMandatory()) return order;
        }
        return null;
    }

    public static int getNumberOfOrders() {
        return Problem.orders.size();
    }

    public static void calcAndSetPostponementPenalties() {
        for (Order order : Problem.orders) {
            if (!order.isMandatory()) {
                Installation depot = Problem.getDepot();
                Installation inst = Problem.getInstallation(order);
                double emergencyCost = calcCost(depot, inst, order, Problem.designSpeed);
                order.setPostponementPenalty(emergencyCost);
            }
        }
    }

    private static double calcCost(Installation depot, Installation inst, Order order, double speed) {
        double distance = DistanceCalculator.distance(depot, inst, "N");
        int startTime = Problem.preparationEndTime;
        int arrTime = startTime + Problem.hourToDiscTimePoint(distance / speed);
        int vIdx = 0;  // The first vessel will always be a fleet vessel
        int serviceEndTime = arrTime + ArcGenerator.calculateServiceDuration(order);
        double sailCost = ArcGenerator.calculateFuelCostSailing(startTime, arrTime, speed, distance, vIdx) * 2;
        double serviceCost = ArcGenerator.calculateFuelCostServicing(arrTime, serviceEndTime);
        return sailCost + serviceCost;
    }


    /* =========== VESSEL =========== */

    public static int getNumberOfVessels() {
        return Problem.vessels.size();
    }

    public static boolean isSpotVessel(int vesselIdx) {
        return vesselIdx == Problem.getNumberOfVessels() - 1;
    }

    public static Vessel getVessel(int vesselIdx) {
        return Problem.vessels.get(vesselIdx);
    }

    public static void setVesselPoolAndCacheSize() {
        Parameters.vesselPoolSize = Parameters.totalPoolSize / (getNumberOfVessels() - 1);
        Parameters.vesselCacheSize = Parameters.cacheSize / (getNumberOfVessels() - 1);
    }

    /* =========== WEATHER =========== */

    public static double getSpeedImpact(int weatherState) {
        return Problem.wsToSpeedImpact.get(weatherState);
    }

    public static int getWorstWeatherState(int startTime, int endTime) {
        return Collections.max(Problem.weatherForecastDisc.subList(startTime, endTime + 1));
    }


    /* =========== TIME =========== */

    public static int getFirstTimePoint() {
        return 0;
    }

    public static int getFinalTimePoint() {
        return Problem.planningPeriodDisc - 1;
    }

    public static int getEndOfDayTimePoint() {
        return 24 * Problem.discretizationParam - 1;
    }

    public static int getGeneralReturnTime() {
        return vessels.get(0).getReturnTime() * Problem.discretizationParam - 1;
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

    public static void setRandom(int seed) {
        Problem.currentSeed = seed;
        Problem.random = new Random(seed);
    }

    /* =========== SETUP =========== */

    public static void setUpProblem(String fileName, boolean isTest, int randomSeed) {
        Constants.PATH_TO_INSTANCE = (isTest ? Constants.PATH_TO_TEST_DIR : Constants.PATH_TO_INSTANCES_DIR) + fileName;
        Problem.speedOpt = true;
        IO.setUpInstanceInfo();
        IO.setUpInstallations();
        IO.setUpVesselInfo();
        IO.setUpVessels();
        IO.setUpWeather();
        IO.setUpOrders();
        Problem.random = new Random(randomSeed);
        Problem.calcAndSetPostponementPenalties();
    }
}
