package alns;

import data.Problem;
import objects.Order;
import subproblem.Node;
import subproblem.SubProblem;

import java.util.*;

public class Objective {

    public static void setObjValAndSchedule(Solution solution) {
        /* Calculates and sets the objective value of the solution and sets the corresponding vessel schedules */
        List<List<Node>> shortestPaths = new ArrayList<>();
        double objectiveValue = 0.0;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            SubProblem subProblem = runSubProblemComplete(solution.getOrderSequence(vesselNumber), vesselNumber);
            shortestPaths.add(subProblem != null ? subProblem.getShortestPath() : new ArrayList<>());
            objectiveValue += subProblem != null ? subProblem.getShortestPathCost() : 0.0;
        }

        double postponementPenalty = penalizePostponement(solution);
        objectiveValue += postponementPenalty;

        solution.setShortestPaths(shortestPaths);
        solution.setFitness(objectiveValue);
    }

    public static double penalizePostponement(Solution solution) {
        return solution.getPostponedOrders().stream()
                .map(o -> o.getSize() * o.getPostponementPenalty())
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public static SubProblem runSubProblemComplete(List<Order> orderSequence, int vesselNumber) {
        /* Runs SubProblem and returns entire object to access all solution info (schedules) */
        try {
            SubProblem subProblem = new SubProblem(orderSequence, vesselNumber);
            subProblem.solve();
            return subProblem;
        } catch (IllegalArgumentException e) {
            // System.out.println(e.getMessage());
        }
        return null;
    }

    public static double runSubProblemLean(List<Order> orderSequence, int vesselNumber) {
        /* Run SubProblem and returns only objective value */
        try {
            SubProblem subProblem = new SubProblem(orderSequence, vesselNumber);
            subProblem.solve();
            return subProblem.getShortestPathCost();
        } catch (IllegalArgumentException e) {
            // System.out.println(e.getMessage());
        }
        return 0.0;
    }

    public static double runSubProblemLean(List<List<Order>> orderSequences) {
        double obj = 0.0;
        for (int vesselNumber = 0; vesselNumber < Problem.getNumberOfVessels(); vesselNumber++) {
            obj += runSubProblemLean(orderSequences.get(vesselNumber), vesselNumber);
        }
        return obj;
    }

    private static Solution createOptimalSolution() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4))));
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(2), Problem.getOrder(5),
                Problem.getOrder(0), Problem.getOrder(1), Problem.getOrder(6),
                Problem.getOrder(7))));
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, true);
    }

    private static Solution createOptimalSolutionSimple() {
        List<List<Order>> orderSequences = new ArrayList<>();
        orderSequences.add(new LinkedList<>(Arrays.asList(Problem.getOrder(3), Problem.getOrder(4),
                Problem.getOrder(0), Problem.getOrder(1), Problem.getOrder(5),
                Problem.getOrder(2))));
        orderSequences.add(new LinkedList<>());
        orderSequences.add(new LinkedList<>());
        Set<Order> postponedOrders = new HashSet<>();
        return new Solution(orderSequences, postponedOrders, true);
    }

    public static void main(String[] args) {
        Problem.setUpProblem("example_simple.json", false, 10);
        // for (Order order : Problem.orders) System.out.println(order + ", " + order.getSize());
        System.out.println(Problem.orders);
        Solution optimum = createOptimalSolutionSimple();
        System.out.println(optimum.getFitness(false));
        optimum.printSchedules();
    }
}
