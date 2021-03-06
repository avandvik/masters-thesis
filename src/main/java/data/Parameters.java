package data;

public class Parameters {

    // Noise in objective
    public static double noiseRate = 0.5;
    public static double maxNoise = Problem.findMaxDistance() * noiseRate;

    // Rewards for heuristics
    public static double newGlobalBest = 33.0;
    public static double newLocalImprovement = 9.0;
    public static double newLocal = 9.0;

    // Simulated annealing
    public static double startTemperature;
    public static double coolingRate;

    // Iterations
    public static int totalIterations = 10;
    public static int maxIterationsCurrentSolution = 50;


    public static void setTemperatureAndCooling(double currentFitness) {
        double candidateFitness = currentFitness * 1.05;
        startTemperature = -(candidateFitness - currentFitness) * (1 / Math.log(0.5));
        double lastTemperature = startTemperature * 0.002;
        coolingRate = Math.exp(Math.log(lastTemperature / startTemperature) / totalIterations);
    }
}
