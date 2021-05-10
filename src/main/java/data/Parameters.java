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

    // Removal parameters
    public static int nbrOrdersRemove = 3;
    public static int regretParameter = 4;
    public static double rnRelated = 10;
    public static double rnWorst = 10;

    // K-means
    public static int k = 2;
    public static int kMeansAttempts = 10;

    // Local search
    public static boolean exhaustiveLocalSearch = false;
    public static boolean randomLocalSearch = true;
    public static int numberOfOperators = 3;
    public static boolean parallelLocalSearch = false;

    // Iterations
    public static int totalIter = 500;
    public static int maxIterSolution = 20;
    public static int setPartitioningIter = 20;
    public static int segmentIter = 50;

    // Cache and order sequence pool size
    public static int cacheSize = 1000;
    public static int poolSize = 200;  // Pool size is per vessel

    // Toggles
    public static boolean setPartitioning = false;
    public static boolean localSearch = true;
    public static boolean parallelHeuristics = true;
    public static boolean cacheSP = true;
    public static boolean verbose = false;
    public static boolean semiVerbose = true;
    public static boolean saveSolution = false;


    public static void setTemperatureAndCooling(double currentFitness) {
        double candidateFitness = currentFitness * 1.05;
        startTemperature = -(candidateFitness - currentFitness) * (1 / Math.log(0.5));
        double lastTemperature = startTemperature * 0.002;
        coolingRate = Math.exp(Math.log(lastTemperature / startTemperature) / totalIter);
    }

    public static void setSolstormParameters() {
        cacheSP = true;
        cacheSize = Integer.MAX_VALUE;
        poolSize = Integer.MAX_VALUE;
        parallelHeuristics = true;
        totalIter = 5000;
        verbose = false;
        semiVerbose = true;
        saveSolution = true;
    }
}
