package utils;

import data.Problem;
import objects.Installation;
import objects.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InstanceGenerator {

    /*
        - Number of installations (as input)
        - Get random spatial structure (sample installations randomly from pool of 27)
        - Apply stochastic rules for assigning order types
        - Apply stochastic rules for determining order sizes
        - Choose weather scenario (as input)
     */

    private final static int seed = 11;  // TODO: Include in instance
    private final static Random rn = new Random(seed);
    private final static double planningPeriodHours = 80.0;
    private final static double discretizationParameter = 4.0;
    private final static String weatherScenario = "perfect";
    private final static String installationOrdering = "random";

    private final static int numberOfInstallations = 5;

    private final static double MDLower = 0.4;
    private final static double MDUpper = 0.6;
    private final static double ODLower = 0.2;
    private final static double ODUpper = 0.5;
    private final static double OPLower = 0.2;
    private final static double OPUpper = 0.5;

    private final static double maxOrderSizeDeviation = 0.2;

    public static void generateInstance() {

        List<Integer> installationIds = drawInstallations();
        System.out.println(installationIds);
        assignOrderTypes(installationIds);

    }

    private static List<Integer> drawInstallations() {
        List<Integer> installationIds = new ArrayList<>();

        // TODO: Change to installationIdPool
        List<Installation> installationPool = Helpers.deepCopyList(Problem.installations, false);

        while (installationIds.size() < numberOfInstallations) {
            int instIdx = rn.nextInt(installationPool.size());
            installationIds.add(installationPool.remove(instIdx).getId());
        }

        return installationIds;
    }

    private static void assignOrderTypes(List<Integer> installationIds) {
        int minMD = (int) Math.ceil(numberOfInstallations * MDLower);
        int maxMD = (int) Math.floor(numberOfInstallations * MDUpper);
        List<Integer> range = IntStream.rangeClosed(minMD, maxMD).boxed().collect(Collectors.toList());
        int numberOfMDs = range.get(rn.nextInt(range.size()));

        int MDOrdersAdded = 0;
        while (MDOrdersAdded < numberOfMDs) {
            String mandatory = "True";
            String transport = "delivery";
            int installationId = installationIds.remove(rn.nextInt(installationIds.size()));

            // TODO: Assign size to order by using installationSizePool

            // TODO: Add order to JSON

            MDOrdersAdded++;
        }

    }


    public static void main(String[] args) {
        // TODO: Change such that only installations.json is used
        Problem.setUpProblem("example_6.json", false, 10);
        InstanceGenerator.generateInstance();
    }

}
