package com.nibm.pdsa.games.minimumcost.dto;

import java.util.List;

public record MinimumCostHistoryResponse(
        List<MinimumCostHistoryRound> rounds,
        List<MinimumCostHistorySubmission> recentSubmissions,
        long totalRounds,
        long totalSubmissions,
        long optimalSubmissions,
        double averageHungarianTimeMs,
        double averageAlternativeTimeMs,
        double averageSpeedup,
        long bestHungarianTimeMs,
        long bestAlternativeTimeMs
) {
}