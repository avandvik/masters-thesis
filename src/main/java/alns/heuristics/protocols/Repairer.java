package alns.heuristics.protocols;

import alns.Solution;

public interface Repairer {
    Solution repair(Solution partialSolution);
}
