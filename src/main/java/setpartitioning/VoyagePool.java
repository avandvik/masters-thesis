package setpartitioning;

import alns.Objective;
import alns.Solution;
import data.Parameters;
import data.Problem;
import objects.Order;

import java.util.*;

public class VoyagePool {

    public static Map<Integer, Map<List<Order>, Double>> vesselToSequenceToCost;

    public static void initializeSequenceSaving() {
        vesselToSequenceToCost = new HashMap<>();
        for (int vesselIdx = 0; vesselIdx < Problem.getNumberOfVessels(); vesselIdx++) {
            Map<List<Order>, Double> emptySequence = new HashMap<>();
            emptySequence.put(new LinkedList<>(), 0.0);
            vesselToSequenceToCost.put(vesselIdx, emptySequence);
        }
    }

    public static void saveOrderSequences(Solution candidateSolution) {
        /* Save order sequences in candidateSolution while not exceeding storage limits */
        for (int vIdx = 0; vIdx < Problem.getNumberOfVessels(); vIdx++) {
            List<Order> orderSequence = candidateSolution.getOrderSequence(vIdx);
            saveOrderSequence(vIdx, orderSequence);
        }
    }

    public static void saveOrderSequence(int vIdx, List<Order> orderSequence) {
        /* Save order sequence to set partitioning pool */
        deleteExceedingSequences(vIdx);
        double cost = Objective.getOrderSequenceCost(orderSequence, vIdx);
        vesselToSequenceToCost.get(vIdx).put(orderSequence, cost);  // Okay if overwrite
    }

    private static void deleteExceedingSequences(int vIdx) {
        if (vesselToSequenceToCost.get(vIdx).keySet().size() > Parameters.poolSize) {
            List<List<Order>> sequences = new ArrayList<>(vesselToSequenceToCost.get(vIdx).keySet());
            Collections.shuffle(sequences, Problem.random);
            int mappingsToDelete = Problem.getNumberOfVessels();
            sequences.subList(0, mappingsToDelete).forEach(vesselToSequenceToCost.get(vIdx).keySet()::remove);
        }
    }
}
