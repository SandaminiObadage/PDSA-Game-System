package com.nibm.pdsa.games.minimumcost.dto;

public record MinimumCostHistorySubmission(
        long submissionId,
        long roundId,
        String playerName,
        int submittedCost,
        boolean optimal,
        String createdAt
) {
}