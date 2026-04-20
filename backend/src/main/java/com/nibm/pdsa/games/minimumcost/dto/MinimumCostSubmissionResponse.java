package com.nibm.pdsa.games.minimumcost.dto;

public record MinimumCostSubmissionResponse(
        long roundId,
        String playerName,
        int submittedCost,
        Integer optimalCost,
        boolean correct,
        boolean duplicate,
        String message
) {
}