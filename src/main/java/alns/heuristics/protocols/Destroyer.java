package alns.heuristics.protocols;

import alns.Solution;

public interface Destroyer {
    Solution destroy(Solution solution, int numberOfOrders);
}
