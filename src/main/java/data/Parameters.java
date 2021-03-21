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

    // Simulated annealing
    public static double startTemperature;
    public static double coolingRate;

    // Random parameter removal methods
    public static double rnRelated = 10;
    public static double rnWorst = 10;


    // Regret parameter for regret insertion
    public static int regretParameter = 4;

    // Concurrency
    public static boolean parallelHeuristics = false;

    // Iterations
    public static int totalIterations = 100;
    public static int maxIterSolution = 20;
    public static int setPartitioningIterations = 50;

    // Verbose
    public static boolean verbose = false;

    // Local or external
    public static boolean local = true;

    public static void setTemperatureAndCooling(double currentFitness) {
        double candidateFitness = currentFitness * 1.05;
        startTemperature = -(candidateFitness - currentFitness) * (1 / Math.log(0.5));
        double lastTemperature = startTemperature * 0.002;
        coolingRate = Math.exp(Math.log(lastTemperature / startTemperature) / totalIterations);
    }
}
