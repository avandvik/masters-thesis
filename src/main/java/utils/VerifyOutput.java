package utils;

import arcs.ArcGenerator;
import data.Parameters;
import data.Problem;
import objects.Installation;
import objects.Order;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import data.Constants;

import java.util.*;

public class VerifyOutput {

    private void loadSolution() {
        String path = Constants.ROOT_PATH + "/output/new_solstorm/23-27-4-1_critical_422_solution.json";
        JSONObject solution = IO.getJSONObject(path);
        JSONObject voyages = (JSONObject) solution.get("voyages");

        double totalCost = 0.0;

        for (Object psvKey : (voyages.keySet())) {
            JSONObject psv = (JSONObject) voyages.get(psvKey);

            // Get PSV index
            String psvStr = (String) psvKey;
            System.out.println("PSV " + psvStr);
            if (psvStr.equals("SPOT")) continue;
            String[] psvStrArr = psvStr.split("_");
            int psvIdx = Integer.parseInt(psvStrArr[1]) - 1;

            // Create order sequence for the psv
            List<Integer> os = new ArrayList<>();
            for (Object orderIdx : (JSONArray) psv.get("sequence")) {
                os.add((int) (long) orderIdx);
            }
            if (os.isEmpty()) continue;

            // Create easy access dicts
            JSONObject timePoints = (JSONObject) psv.get("time_points");
            Map<Integer, Integer> oToArrTime = new HashMap<>();
            Map<Integer, Integer> oToServiceTime = new HashMap<>();
            Map<Integer, Integer> oToEndTime = new HashMap<>();
            Map<Integer, Double> oToSpeed = new HashMap<>();
            int startDepotEndTime = 0;
            int endDepotEndTime = 0;
            double endDepotSpeed = 0.0;
            for (Object o : timePoints.keySet()) {
                String oStr = (String) o;
                if (oStr.equals("SD")) {
                    startDepotEndTime = (int) (long) ((JSONObject) timePoints.get(o)).get("end_time");
                    continue;
                } else if (oStr.equals("ED")) {
                    endDepotEndTime = (int) (long) ((JSONObject) timePoints.get(o)).get("end_time");
                    endDepotSpeed = (double) ((JSONObject) timePoints.get(o)).get("speed");
                    continue;
                }
                int orderIdx = Integer.parseInt(oStr);
                int arrTime = (int) (long) ((JSONObject) timePoints.get(o)).get("arrival_time");
                int serviceTime = (int) (long) ((JSONObject) timePoints.get(o)).get("service_time");
                int endTime = (int) (long) ((JSONObject) timePoints.get(o)).get("end_time");
                double speed = (double) ((JSONObject) timePoints.get(o)).get("speed");
                oToArrTime.put(orderIdx, arrTime);
                oToServiceTime.put(orderIdx, serviceTime);
                oToEndTime.put(orderIdx, endTime);
                oToSpeed.put(orderIdx, speed);
            }

            // Calculate costs
            double sailCost = 0.0;
            double idleCost = 0.0;
            double serviceCost = 0.0;

            // Costs depot -> first order
            int firstOrderIdx = os.get(0);
            Installation depot = Problem.getDepot();
            Order firstOrder = Problem.getOrder(firstOrderIdx);
            int startTime = startDepotEndTime;
            int arrTime = oToArrTime.get(firstOrderIdx);
            int serviceTime = oToServiceTime.get(firstOrderIdx);
            int endTime = oToEndTime.get(firstOrderIdx);
            double speed = oToSpeed.get(firstOrderIdx);
            double dist = DistanceCalculator.distance(depot, firstOrder, "N");
            sailCost += ArcGenerator.calculateFuelCostSailing(startTime, arrTime, speed, dist, psvIdx);
            idleCost += ArcGenerator.calculateFuelCostIdling(arrTime, serviceTime);
            serviceCost += ArcGenerator.calculateFuelCostServicing(serviceTime, endTime);

            // Costs order -> order
            for (int i = 0; i < os.size() - 1; i++) {
                int fromOrderIdx = os.get(i);
                int toOrderIdx = os.get(i + 1);
                Order fromOrder = Problem.getOrder(fromOrderIdx);
                Order toOrder = Problem.getOrder(toOrderIdx);
                startTime = oToEndTime.get(fromOrderIdx);
                arrTime = oToArrTime.get(toOrderIdx);
                serviceTime = oToServiceTime.get(toOrderIdx);
                endTime = oToEndTime.get(toOrderIdx);
                speed = oToSpeed.get(toOrderIdx);
                dist = DistanceCalculator.distance(fromOrder, toOrder, "N");
                sailCost += ArcGenerator.calculateFuelCostSailing(startTime, arrTime, speed, dist, psvIdx);
                idleCost += ArcGenerator.calculateFuelCostIdling(arrTime, serviceTime);
                serviceCost += ArcGenerator.calculateFuelCostServicing(serviceTime, endTime);
            }

            // Costs last order -> depot
            int lastOrderIdx = os.get(os.size() - 1);
            Order lastOrder = Problem.getOrder(lastOrderIdx);
            startTime = oToEndTime.get(lastOrderIdx);
            arrTime = endDepotEndTime;
            speed = endDepotSpeed;
            dist = DistanceCalculator.distance(depot, lastOrder, "N");
            sailCost += ArcGenerator.calculateFuelCostSailing(startTime, arrTime, speed, dist, psvIdx);

            totalCost += sailCost + idleCost + serviceCost;

        }

        System.out.println(totalCost);
    }

    public static void main(String[] args) {
        Constants.FILE_NAME = "23-27-4-1_critical.json";
        Random rn = new Random();
        int seed = rn.nextInt(Parameters.seedBound);
        Problem.setUpProblem(Constants.FILE_NAME, false, seed);

        VerifyOutput vo = new VerifyOutput();
        vo.loadSolution();
    }
}
