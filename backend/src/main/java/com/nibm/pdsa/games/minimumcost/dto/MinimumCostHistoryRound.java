package com.nibm.pdsa.games.minimumcost.dto;

public record MinimumCostHistoryRound(
        long roundId,
        long roundNo,
        int n,
        int hungarianCost,
        long hungarianTimeMs,
        int alternativeCost,
        long alternativeTimeMs,
        double speedup,
        String createdAt
) {
}