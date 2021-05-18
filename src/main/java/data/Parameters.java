package data;

public class Parameters {

    // Noise in objective
    public static double noiseRate = 0.25;  // Initial 0.25
    public static double maxNoise = Problem.findMaxDistance() * noiseRate;

    // Rewards for heuristics
    public static double newGlobalBest = 33.0;  // Initial 33.0
    public static double newLocalImprovement = 9.0;  // Initial 9.0
    public static double newLocal = 13.0;  // Initial 13.0

    // Weights for heuristics
    public static double initialWeight = 0.2;  // Initial 0.2
    public static double reaction = 0.1;  // Initial 0.1

    // Simulated annealing
    public static double startTemperature;
    public static double coolingRate;
    private static final double lastTempPercentageOfStart = 0.002;  // See Liu et al (2019) for details
    private static final double XPercent = 0.05;
    private static final double probAcceptCandidateXPercentWorse = 0.5;

    // Removal parameters
    public static int minOrdersRemove = 2;  // No tuning
    public static double minPercentage = 0.05;  // Initial 0.05
    public static double maxPercentage = 0.15;  // Initial 0.30
    public static int regretParameter = 3;  // No tuning
    public static double p = 5;  // Initial 5

    // K-means
    public static int k = 2;  // No tuning
    public static int kMeansAttempts = 10;  // No tuning

    // Local search
    public static double lsThresh = 0.2;  // No tuning

    // Iterations
    public static int totalIter = 100;
    public static int maxIterSolution = 20;
    public static int setPartIter = 500;
    public static int segmentIter = 50;
    public static int searchHistoryIter = 10;

    // Cache and order sequence pool size
    public static int cacheSize = 1000;
    public static int poolSize = 200;  // Pool size is per vessel

    // Toggles
    public static boolean localSearch = true;
    public static boolean setPartitioning = true;
    public static boolean parallelHeuristics = false;
    public static boolean cacheSP = true;
    public static boolean verbose = true;
    public static boolean semiVerbose = true;
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
        cacheSize = 200000;
        poolSize = 30000;
        parallelHeuristics = true;
        totalIter = 5000;
        verbose = false;
        semiVerbose = true;
        saveSolution = true;
        saveHistory = true;
    }
}
