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
    // Filename and path to instance
    public static String fileName;
    public static String pathToInstanceFile;

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

    // Random object
    public static Random random;


    /* =========== INSTALLATION =========== */

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

    /* =========== ORDERS =========== */

    public static Order getOrder(int orderIndex) {
        return Problem.orders.get(orderIndex);
    }

    public static List<Order> getOrdersFromInstallation(Installation installation) {
        return Problem.orders.stream().filter(o -> o.getInstallationId() == installation.getId()).collect(Collectors.toList());
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
        int serviceEndTime = arrTime + ArcGenerator.calculateServiceDuration(order);
        double sailCost = ArcGenerator.calculateFuelCostSailing(startTime, arrTime, speed, distance) * 2;
        double serviceCost = ArcGenerator.calculateFuelCostServicing(arrTime, serviceEndTime);
        return sailCost + serviceCost;
    }


    /* =========== VESSEL =========== */

    public static int getNumberOfVessels() {
        return Problem.vessels.size();
    }

    public static boolean isSpotVessel(Vessel vessel) {
        return Problem.vessels.indexOf(vessel) == Problem.getNumberOfVessels() - 1;
    }

    public static Vessel getVessel(int vesselNumber) {
        return Problem.vessels.get(vesselNumber);
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


    /* =========== SETUP =========== */

    public static void setUpProblem(String fileName, boolean isTest, int randomSeed) {
        Problem.fileName = fileName;
        Problem.pathToInstanceFile = (isTest ? Constants.PATH_TO_TEST : Constants.PATH_TO_INSTANCE) + fileName;
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


