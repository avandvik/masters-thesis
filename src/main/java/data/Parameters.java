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

    // Iterations
    public static int totalIter = 1000;
    public static int maxIterSolution = 20;
    public static int setPartitioningIter = 100;
    public static int segmentIter = 50;

    // Toggles
    public static boolean setPartitioning = false;
    public static boolean localSearch = true;
    public static boolean verbose = false;
    public static boolean semiVerbose = true;
    public static boolean saveSolution = false;
    public static boolean parallelHeuristics = true;
    public static boolean cacheSP = false;


    public static void setTemperatureAndCooling(double currentFitness) {
        double candidateFitness = currentFitness * 1.05;
        startTemperature = -(candidateFitness - currentFitness) * (1 / Math.log(0.5));
        double lastTemperature = startTemperature * 0.002;
        coolingRate = Math.exp(Math.log(lastTemperature / startTemperature) / totalIter);
    }
}
