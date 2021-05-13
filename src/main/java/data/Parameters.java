package data;

public class Parameters {

    // Noise in objective
    public static double noiseRate = 0.5;
    public static double maxNoise = Problem.findMaxDistance() * noiseRate;

    // Rewards for heuristics
    public static double newGlobalBest = 33.0;
    public static double newLocalImprovement = 9.0;
    public static double newLocal = 9.0;

    // Weights for heuristics
    public static double initialWeight = 0.2;
    public static double reaction = 0.2;

    // Simulated annealing
    public static double startTemperature;
    public static double coolingRate;
    private static final double lastTempPercentageOfStart = 0.002;  // See Liu et al (2019) for details
    private static final double XPercent = 0.05;
    private static final double probAcceptCandidateXPercentWorse = 0.5;

    // Removal parameters
    public static int nbrOrdersRemove = 3;  // TODO: Implement as percentage
    public static int regretParameter = 4;
    public static double rnRelated = 10;
    public static double rnWorst = 10;

    // K-means
    public static int k = 2;
    public static int kMeansAttempts = 10;

    // Local search
    public static boolean exhaustiveLocalSearch = false;
    public static double localSearchCondition = 0.2;  // TODO: Rename
    public static boolean randomLocalSearch = true;  // TODO: Remove when condition for running local search is added
    public static int numberOfOperators = 3;  // TODO: Remove when condition for running local search is added

    // Iterations
    public static int totalIter = 50;
    public static int maxIterSolution = 20;
    public static int setPartIter = 10;
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
    public static boolean verbose = false;
    public static boolean semiVerbose = true;
    public static boolean saveSolution = true;
    public static boolean saveHistory = true;


    public static void setTemperatureAndCooling(double currentFitness) {
        double candidateFitness = currentFitness * (1 + XPercent);
        startTemperature = -(candidateFitness - currentFitness) * (1 / Math.log(probAcceptCandidateXPercentWorse));
        double lastTemperature = startTemperature * lastTempPercentageOfStart;
        coolingRate = Math.exp(Math.log(lastTemperature / startTemperature) / totalIter);
    }

    public static void setSolstormParameters() {
        cacheSP = true;
        cacheSize = 50000;
        poolSize = 30000;
        parallelHeuristics = true;
        totalIter = 5000;
        verbose = false;
        semiVerbose = true;
        saveSolution = true;
        saveHistory = true;
    }
}
