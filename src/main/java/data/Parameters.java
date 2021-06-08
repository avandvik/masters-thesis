package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parameters {

    // Noise in objective
    public static double noiseRate = 0.025;  // Initial 0.25  |  Final 0.025
    public static double maxNoise = Problem.findMaxDistance() * noiseRate;

    // Heuristics
    public static double newGlobalBest = 33.0;  // Initial 33.0  |  Final 33.0
    public static double newLocalImprovement = 9.0;  // Initial 9.0  |  Final 9.0
    public static double newLocal = 1.0;  // Initial 13.0  |  Final 1.0
    public static double initialWeight = 0.2;  // No tuning
    public static double reaction = 0.1;  // Initial 0.1  |  Final 0.1

    // Simulated annealing
    public static double startTemperature;
    public static double coolingRate;
    private static final double lastTempPercentageOfStart = 0.002;  // See Liu et al (2019) for details
    private static final double XPercent = 0.05;
    private static final double probAcceptCandidateXPercentWorse = 0.5;

    // Removal parameters
    public static int minOrdersRemove = 2;  // No tuning
    public static double minPercentage = 0.15;  // Initial 0.05  |  Final 0.15
    public static double maxPercentage = 0.50;  // Initial 0.15  |  Final 0.50
    public static int regretParameter = 3;  // No tuning
    public static double p = 7.0;  // Initial 5.0  |  Final 7.0

    // K-means
    public static int k = 2;  // No tuning
    public static int kMeansAttempts = 10;  // No tuning

    // Local search
    public static double lsMaxGap = 0.2;  // No tuning
    public static double lsOperatorRunLimit = 0.5;  // No tuning

    // Iterations
    public static int totalIter = 1000;
    public static int maxIterSolution = 20;
    public static int segmentIter = 50;
    public static int searchHistoryIter = 1;
    public static List<Integer> setPartIterations = new ArrayList<>(Arrays.asList(200, 400, 600, 800, 1000, 1500,
            2000, 3000, 4000, 5000));

    // Cache and order sequence pool size
    public static int cacheSize = 1000;
    public static int vesselCacheSize;
    public static int totalPoolSize = 1000;
    public static int vesselPoolSize;

    // Toggles
    public static boolean localSearch = true;
    public static boolean setPartitioning = true;
    public static boolean roulette = true;
    public static boolean parallelHeuristics = false;
    public static boolean cacheSP = true;
    public static boolean verbose = false;
    public static boolean semiVerbose = true;
    public static boolean printSolution = true;
    public static boolean saveSolution = true;
    public static boolean saveHistory = true;

    // Other
    public static final int seedBound = 1000;
    public static final int maxRunTime = 600;

    public static void setTemperatureAndCooling(double currentFitness) {
        double candidateFitness = currentFitness * (1 + XPercent);
        startTemperature = -(candidateFitness - currentFitness) * (1 / Math.log(probAcceptCandidateXPercentWorse));
        double lastTemperature = startTemperature * lastTempPercentageOfStart;
        coolingRate = Math.exp(Math.log(lastTemperature / startTemperature) / totalIter);
    }

    public static void setSolstormParameters() {
        cacheSP = true;
        cacheSize = 1000000;
        totalPoolSize = 100000;
        parallelHeuristics = true;
        totalIter = 5000;
        verbose = false;
        semiVerbose = true;
        printSolution = true;
        saveSolution = true;
        saveHistory = true;
    }
}
