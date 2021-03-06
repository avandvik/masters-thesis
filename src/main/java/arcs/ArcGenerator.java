package arcs;

import data.Problem;
import objects.Installation;
import objects.Order;
import objects.Vessel;

import java.util.*;

public class ArcGenerator {

    public static List<Double> getSpeeds(double distance, int startTime) {
        if (distance == 0) return new ArrayList<>(Collections.singletonList(Problem.designSpeed));
        if (!Problem.speedOpt) return new ArrayList<>(Collections.singletonList(Problem.designSpeed));
        double averageMaxSpeed;
        try {
            averageMaxSpeed = calculateAverageMaxSpeed(startTime, distance);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        List<Double> speeds = new ArrayList<>();
        for (double speed = Problem.minSpeed; speed < averageMaxSpeed; speed += 1) speeds.add(speed);
        speeds.add(averageMaxSpeed);
        return speeds;
    }

    public static Map<Double, Integer> mapSpeedsToArrTimes(double distance, int startTime, List<Double> speeds) {
        Map<Double, Integer> speedsToArrTimes = new HashMap<>();
        for (Double speed : speeds) {
            int arrTime = startTime + Problem.hourToDiscTimePoint(distance / speed);
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
            int serviceEndTime = arrTime + serviceDuration;
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
        if (endTime >= Problem.getFinalTimePoint() || distance < 0) return false;
        double averageMaxSpeed;
        try {
            averageMaxSpeed = calculateAverageMaxSpeed(endTime, distance);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        int earliestArrTime = endTime + (int) Math.ceil(Problem.hourToDiscDecimal(distance / averageMaxSpeed));
        return earliestArrTime <= Problem.getGeneralReturnTime();
    }

    public static boolean isServicingPossible(int serviceStartTime, int serviceEndTime, Installation toInst) {
        int startDayTime = Problem.discToDiscDayTime(serviceStartTime);
        int endDayTime = Problem.discToDiscDayTime(serviceEndTime);
        int openTime = toInst.getOpeningHour() * Problem.discretizationParam;
        int closeTime = toInst.getClosingHour() * Problem.discretizationParam;
        boolean instOpen = true;
        if (openTime != Problem.getFirstTimePoint() && closeTime != Problem.getEndOfDayTimePoint()) {
            instOpen = startDayTime >= openTime && endDayTime <= closeTime
                    && endDayTime >= openTime && startDayTime <= closeTime
                    && startDayTime < endDayTime;
        }
        int worstWeatherState = Problem.getWorstWeatherState(serviceStartTime, serviceEndTime);
        return instOpen && worstWeatherState < Problem.worstWeatherState;
    }

    public static Map<Double, Double> mapSpeedsToCosts(Map<Double, List<Integer>> speedsToTimePoints, double distance
            , int startTime, int vIdx) {
        Map<Double, Double> speedsToCosts = new HashMap<>();
        for (Map.Entry<Double, List<Integer>> entry : speedsToTimePoints.entrySet()) {
            double speed = entry.getKey();
            List<Integer> timePoints = entry.getValue();
            double cost = calculateArcCost(startTime, timePoints.get(0), timePoints.get(1), timePoints.get(2)
                    , speed, distance, vIdx);
            speedsToCosts.put(speed, cost);
        }
        return speedsToCosts;
    }

    public static double calculateAverageMaxSpeed(int startSailingTime, double distance) throws IndexOutOfBoundsException {
        double sailedDistance = 0.0;
        int currentTime = startSailingTime;
        while (sailedDistance < distance) {
            int ws = Problem.weatherForecastDisc.get(currentTime);
            double adjustedMaxSpeed = Problem.maxSpeed - Problem.getSpeedImpact(ws);
            sailedDistance += adjustedMaxSpeed * Problem.timeUnit;
            currentTime++;
        }
        double overshootTime = calculateOvershootTime(sailedDistance - distance, currentTime);
        return distance / (Problem.discTimeToHour(currentTime - startSailingTime) - overshootTime);
    }

    private static double calculateOvershootTime(double overshootDistance, int sailingEndTime) {
        int ws = Problem.weatherForecastDisc.get(sailingEndTime - 1);
        return overshootDistance / (Problem.maxSpeed - Problem.getSpeedImpact(ws));
    }

    public static List<Integer> createTimePoints(int arrTime, int serviceStartTime, int serviceEndTime) {
        List<Integer> timePoints = new ArrayList<>();
        timePoints.add(arrTime);
        timePoints.add(serviceStartTime);
        timePoints.add(serviceEndTime);
        return timePoints;
    }

    public static int calculateServiceDuration(Order order) {
        if (!order.isMandatory() && !order.isDelivery()) return 1;
        return (int) Math.ceil(order.getSize() * Problem.discServiceTimeUnit);
    }

    public static double calculateArcCost(int startTime, int arrTime, int serviceStartTime, int serviceEndTime,
                                          double speed, double distance, int vIdx) {
        double sailCost = calculateFuelCostSailing(startTime, arrTime, speed, distance, vIdx);
        double idlingCost = calculateFuelCostIdling(arrTime, serviceStartTime);
        double serviceCost = calculateFuelCostServicing(serviceStartTime, serviceEndTime);
        double charterCost = calculateCharterCost(startTime, serviceEndTime, Problem.isSpotVessel(vIdx));
        return sailCost + idlingCost + serviceCost + charterCost;
    }

    public static double calculateFuelCostSailing(int startTime, int arrTime, double speed, double distance, int vIdx) {
        if (distance == 0 || startTime == arrTime) return 0;
        Map<Integer, Double> wsToTimeSpent = mapWSToTimeSpent(startTime, arrTime);
        Map<Integer, Double> wsToDistanceTravelled = mapWSToDistanceTravelled(wsToTimeSpent, speed);
        double distanceInWSOneTwo = wsToDistanceTravelled.get(0) + wsToDistanceTravelled.get(1);

        double consumption = calculateFuelConsumptionSailing(distanceInWSOneTwo, speed, 0, vIdx)
                + calculateFuelConsumptionSailing(wsToDistanceTravelled.get(2), speed, 2, vIdx)
                + calculateFuelConsumptionSailing(wsToDistanceTravelled.get(3), speed, 3, vIdx);

        return consumption * Problem.fuelPrice;
    }

    public static double calculateFuelConsumptionSailing(double distance, double speed, int weatherState, int vIdx) {
        Vessel vessel = Problem.getVessel(vIdx);
        return (distance / (speed - Problem.wsToSpeedImpact.get(weatherState))
                * vessel.getFcDesignSpeed() * Math.pow(speed / Problem.designSpeed, 3));
    }

    public static double calculateFuelCostIdling(int arrTime, int serviceStartTime) {
        Map<Integer, Double> wsToTimeSpent = mapWSToTimeSpent(arrTime, serviceStartTime);
        double cost = 0.0;
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            cost += wsToTimeSpent.get(ws) * Problem.wsToServiceImpact.get(ws) * Problem.fcIdling * Problem.fuelPrice;
        }
        return cost;
    }

    public static double calculateFuelCostServicing(int serviceStartTime, int serviceEndTime) {
        Map<Integer, Double> wsToTimeSpent = mapWSToTimeSpent(serviceStartTime, serviceEndTime);
        double cost = 0.0;
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            cost += wsToTimeSpent.get(ws) * Problem.wsToServiceImpact.get(ws)
                    * Problem.wsToServiceImpact.get(ws) * Problem.fcServicing * Problem.fuelPrice;
        }
        return cost;
    }

    public static double calculateCharterCost(int startTime, int endTime, boolean isSpotVessel) {
        return isSpotVessel ? Problem.spotHourRate * Problem.discTimeToHour(endTime - startTime) : 0.0;
    }

    public static Map<Integer, Double> mapWSToDistanceTravelled(Map<Integer, Double> wsToTimeSpent, double speed) {
        Map<Integer, Double> wsToDistanceTravelled = new HashMap<>();
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            wsToDistanceTravelled.put(ws, wsToTimeSpent.get(ws) * speed);
        }
        return wsToDistanceTravelled;
    }

    public static Map<Integer, Double> mapWSToTimeSpent(int startTime, int arrTime) {
        /* Maps each weather state to the time in hours (decimal, exact) spent in this weather state */
        Map<Integer, Double> wsStateToTimeSpent = new HashMap<>();
        for (int ws = 0; ws <= Problem.worstWeatherState; ws++) {
            wsStateToTimeSpent.put(ws, Problem.discTimeToHour(getTimeInWS(startTime, arrTime, ws)));
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
