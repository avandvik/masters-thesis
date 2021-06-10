package data;

public class Messages {

    public static String infSolCreated = "Solution expected to be feasible is not feasible!";
    public static String cannotPlaceMDOrder = "Not possible to place MD order!";
    public static String solInfeasible = "Solution is infeasible where it should not!";
    public static String emptySequenceSP = "Empty order sequence passed to SubProblem, skipping!";
    public static String invalidVesselIdx = "Invalid vesselIdx passed to SubProblem!";
    public static String errorInAvgLatLon = "Error in calculation of average latitude or longitude!";
    public static String emptyCluster = "Could not cluster and find only non-empty clusters!";
    public static String errorInSetPartitioning = "Error in set partitioning model, discarding null solution";
}
