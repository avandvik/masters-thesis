package subproblem;

import data.Problem;
import objects.Installation;
import objects.Order;

import java.util.*;

// TODO: Move to utils or arcgeneration package

public class ArcGeneration {

    public static List<Double> getSpeeds(double distance, int startTime) {
        if (distance == 0) return new ArrayList<>(Collections.singletonList(Problem.designSpeed));
        double averageMaxSpeed = calculateAverageMaxSpeed(startTime, distance);
        List<Double> speeds = new ArrayList<>();
        for (double speed = Problem.minSpeed; speed < averageMaxSpeed; speed += 1) speeds.add(speed);
        speeds.add(averageMaxSpeed);
        return speeds;
    }

    public static Map<Double, Integer> mapSpeedsToArrTimes(double distance, int startTime, List<Double> speeds) {
        Map<Double, Integer> speedsToArrTimes = new HashMap<>();
        for (Double speed : speeds) {
            int arrTime = startTime + hourToDiscTimePoint(distance / speed);
            if (!speedsToArrTimes.containsValue(arrTime)) speedsToArrTimes.put(speed, arrTime);
        }
        return speedsToArrTimes;
    }

    public static Map<Double, List<Integer>> mapSpeedsToTimePoints(Map<Double, Integer> speedsToArrTimes,
                                                                   double distance, int serviceDuration,
                                                                   Installation toInst) {
        if (toInst.equals(Problem.getDepot())) return mapSpeedsToTimePointsDepot(speedsToArrTimes);

        Map<Double, List<Integer>> speedsToTimePoints = mapSpeedsToTimePointsNoIdling(speedsToArrTimes, distance,
                serviceDuration, toInst);

        if (speedsToTimePoints.isEmpty())
            return mapSpeedsToTimePointsIdling(speedsToArrTimes, distance, serviceDuration, toInst);

        return speedsToTimePoints;
    }

    private static Map<Double, List<Integer>> mapSpeedsToTimePointsDepot(Map<Double, Integer> speedsToArrTimes) {
        Map<Double, List<Integer>> speedsToTimePoints = new HashMap<>();
        for (Map.Entry<Double, Integer> entry : speedsToArrTimes.entrySet()) {
            double speed = entry.getKey();
            int arrTime = entry.getValue();
            if (arrTime <= Problem.getGeneralReturnTime()) {
                speedsToTimePoints.put(speed, createTimePoints(arrTime, arrTime, arrTime));
            }
        }
        return speedsToTimePoints;
    }

    private static Map<Double, List<Integer>> mapSpeedsToTimePointsNoIdling(Map<Double, Integer> speedsToArrTimes,
                                                                            double distance, int serviceDuration,
                                                                            Installation toInst) {
        Map<Double, List<Integer>> speedsToTimePoints = new HashMap<>();
        for (Map.Entry<Double, Integer> entry : speedsToArrTimes.entrySet()) {
            double speed = entry.getKey();
            int arrTime = entry.getValue();
            int serviceEndTime = arrTime + serviceDuration - 1;
            if (!isReturnPossible(distance, serviceEndTime)) continue;
            if (isServicingPossible(arrTime, serviceEndTime, toInst)) {
                speedsToTimePoints.put(speed, createTimePoints(arrTime, arrTime, serviceEndTime));
            }
        }
        return speedsToTimePoints;
    }

    private static Map<Double, List<Integer>> mapSpeedsToTimePointsIdling(Map<Double, Integer> speedsToArrTimes,
                                                                          double distance, int serviceDuration,
                                                                          Installation toInst) {
        Map<Double, List<Integer>> speedsToTimePoints = new HashMap<>();
        for (Map.Entry<Double, Integer> entry : speedsToArrTimes.entrySet()) {
            double speed = entry.getKey();
            int arrTime = entry.getValue();
            int serviceStartTime = getServiceStartTimeAfterIdling(arrTime, serviceDuration, toInst);
            int serviceEndTime = serviceStartTime + serviceDuration;
            if (!isReturnPossible(distance, serviceEndTime)) continue;
            speedsToTimePoints.put(speed, createTimePoints(arrTime, serviceStartTime, serviceEndTime));
        }
        return speedsToTimePoints;
    }

    public static int getServiceStartTimeAfterIdling(int arrTime, int serviceDuration, Installation toInst) {
        int serviceStartTime = arrTime;
        while (serviceStartTime < Problem.getGeneralReturnTime() - serviceDuration &&
                !isServicingPossible(serviceStartTime, serviceStartTime + serviceDuration, toInst)) {
            serviceStartTime++;
        }
        return serviceStartTime;
    }

    public static boolean isReturnPossible(double distance, int endTime) {
        if (endTime >= Problem.getFinalTimePoint()) return false;
        double averageMaxSpeed = calculateAverageMaxSpeed(endTime, distance);
        int earliestArrTime = endTime + (int) Math.ceil(hourToDiscDecimal(distance / averageMaxSpeed));
        return earliestArrTime <= Problem.getGeneralReturnTime();
    }

    public static boolean isServicingPossible(int serviceStartTime, int serviceEndTime, Installation toInst) {
        int startDayTime = discToDiscDayTime(serviceStartTime);
        int endDayTime = discToDiscDayTime(serviceEndTime);
        int openTime = toInst.getOpeningHour() * Problem.discretizationParam - 1;
        int closeTime = toInst.getClosingHour() * Problem.discretizationParam - 1;
        boolean instOpen = true;
        if (openTime != Problem.getFirstTimePoint() && closeTime != Problem.getEndOfDayTimePoint()) {
            instOpen = startDayTime >= openTime && endDayTime <= closeTime;
        }
        int worstWeatherState = Problem.getWorstWeatherState(serviceStartTime, serviceEndTime);
        return instOpen && worstWeatherState < Problem.worstWeatherState;
    }

    public static Map<Double, Double> mapSpeedsToCosts(Map<Double, List<Integer>> speedsToTimePoints, double distance
            , int startTime, boolean isSpotVessel) {
        Map<Double, Double> speedsToCosts = new HashMap<>();
        for (Map.Entry<Double, List<Integer>> entry : speedsToTimePoints.entrySet()) {
            double speed = entry.getKey();
            List<Integer> timePoints = entry.getValue();
            double cost = calculateArcCost(startTime, timePoints.get(0), timePoints.get(1), timePoints.get(2)
                    , speed, distance, isSpotVessel);
            speedsToCosts.put(speed, cost);
        }
        return speedsToCosts;
    }

    public static Map<Double, Integer> mapSpeedsToEndTimes(Map<Double, List<Integer>> speedsToTimePoints) {
        Map<Double, Integer> speedsToEndTimes = new HashMap<>();
        for (Map.Entry<Double, List<Integer>> entry : speedsToTimePoints.entrySet()) {
            double speed = entry.getKey();
            List<Integer> timePoints = entry.getValue();
            speedsToEndTimes.put(speed, timePoints.get(timePoints.size() - 1));
        }
        return speedsToEndTimes;
    }

    public static double calculateAverageMaxSpeed(int startSailingTime, double distance) {
        double sailedDistance = 0.0;
        int currentTime = startSailingTime;
        while (sailedDistance < distance) {
            int ws = Problem.weatherForecastDisc.get(currentTime);
            double adjustedMaxSpeed = Problem.maxSpeed - Problem.getSpeedImpact(ws);
            sailedDistance += adjustedMaxSpeed * Problem.timeUnit;
            currentTime++;
        }
        double overshootTime = calculateOvershootTime(sailedDistance - distance, currentTime);
        return distance / (discTimePointToHour(currentTime - startSailingTime) - overshootTime);
    }

    private static double calculateOvershootTime(double overshootDistance, int sailingEndTime) {
        int ws = Problem.weatherForecastDisc.get(sailingEndTime - 1);
        return overshootDistance / (Problem.maxSpeed - Problem.getSpeedImpact(ws));
    }

    public static double calculateAverageMaxSpeed(int startSailingTime, double distance) {
        return 0.0;
    }

    public static List<Integer> createTimePoints(int arrTime, int serviceStartTime, int serviceEndTime) {
        List<Integer> timePoints = new ArrayList<>();
        timePoints.add(arrTime);
        timePoints.add(serviceStartTime);
        timePoints.add(serviceEndTime);
        return timePoints;
    }

    // TODO: The pickup duration must be addressed as an assumption if this is how we want to do this
    public static int calculateServiceDuration(Order order) {
        if (!order.isMandatory() && !order.isDelivery()) {
            return 1;
        }
        return (int) Math.ceil(order.getSize() * Problem.discServiceTimeUnit);
    }

    public static double calculateArcCost(int startTime, int arrTime, int serviceStartTime, int serviceEndTime,
                                          double speed, double distance, boolean isSpotVessel) {
        double sailCost = calculateFuelCostSailing(startTime, arrTime, speed, distance);
        double idlingCost = calculateFuelCostIdling(arrTime, serviceStartTime);
        double serviceCost = calculateFuelCostServicing(serviceStartTime, serviceEndTime);
        double charterCost = calculateCharterCost(startTime, serviceEndTime, isSpotVessel);
        return sailCost + idlingCost + serviceCost + charterCost;
    }

    public static double calculateFuelCostSailing(int startTime, int arrTime, double speed, double distance) {
        if (distance == 0 || startTime == arrTime) return 0;
        Map<Integer, Integer> wsToTimeSpent = mapWSToTimeSpent(startTime, arrTime);
        Map<Integer, Double> wsToDistanceTravelled = mapWSToDistanceTravelled(wsToTimeSpent, speed);
        double distanceInWSOneTwo = wsToDistanceTravelled.get(0) + wsToDistanceTravelled.get(1);

        // TODO: Generalize
        double consumption = calculateFuelConsumption(distanceInWSOneTwo, speed, 0)
                + calculateFuelConsumption(wsToDistanceTravelled.get(2), speed, 2)
                + calculateFuelConsumption(wsToDistanceTravelled.get(3), speed, 3);

        return consumption * Problem.fuelPrice;
    }

    public static double calculateFuelConsumption(double distance, double speed, int weatherState) {
        return (distance / (speed - Problem.wsToSpeedImpact.get(weatherState))
                * Problem.fcDesignSpeed * Math.pow(speed / Problem.designSpeed, 3));
    }

    public static double calculateFuelCostIdling(int arrTime, int serviceStartTime) {
        Map<Integer, Integer> wsToTimeSpent = mapWSToTimeSpent(arrTime, serviceStartTime);
        double cost = 0.0;
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            cost += wsToTimeSpent.get(ws) * Problem.wsToServiceImpact.get(ws) * Problem.fcIdling * Problem.fuelPrice;
        }
        return cost;
    }

    public static double calculateFuelCostServicing(int serviceStartTime, int serviceEndTime) {
        Map<Integer, Integer> wsToTimeSpent = mapWSToTimeSpent(serviceStartTime, serviceEndTime);
        double cost = 0.0;
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            cost += wsToTimeSpent.get(ws) * Problem.wsToServiceImpact.get(ws)
                    * Problem.wsToServiceImpact.get(ws) * Problem.fcIdling * Problem.fuelPrice;
        }
        return cost;
    }

    public static double calculateCharterCost(int startTime, int endTime, boolean isSpotVessel) {
        return isSpotVessel ? Problem.spotHourRate * discTimePointToHour(endTime - startTime) : 0.0;
    }

    public static double hourToDiscDecimal(double timeHour) {
        return timeHour * Problem.discretizationParam;
    }

    public static double discTimePointToHour(int timeDisc) {
        return (double) timeDisc / Problem.discretizationParam;
    }

    public static int hourToDiscTimePoint(double timeHour) {
        return (int) Math.floor(hourToDiscDecimal(timeHour));
    }

    public static int discToDiscDayTime(int timeDisc) {
        return timeDisc % (24 * Problem.discretizationParam);
    }

    public static Map<Integer, Double> mapWSToDistanceTravelled(Map<Integer, Integer> wsToTimeSpent, double speed) {
        Map<Integer, Double> wsToDistanceTravelled = new HashMap<>();
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            wsToDistanceTravelled.put(ws, wsToTimeSpent.get(ws) * speed);
        }
        return wsToDistanceTravelled;
    }

    public static Map<Integer, Integer> mapWSToTimeSpent(int startTime, int arrTime) {
        Map<Integer, Integer> wsStateToTimeSpent = new HashMap<>();
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            wsStateToTimeSpent.put(ws, getTimeInWS(startTime, arrTime, ws));
        }
        return wsStateToTimeSpent;
    }

    public static int getTimeInWS(int startTime, int arrTime, int weatherState) {
        /* Returns the number of discrete time points spent in a weather state */
        int currentTime = startTime;
        int timeSpentInWS = 0;
        while (currentTime < arrTime) {
            if (Problem.weatherForecastDisc.get(currentTime) == weatherState) timeSpentInWS++;
            currentTime++;
        }
        return timeSpentInWS;
    }
}
