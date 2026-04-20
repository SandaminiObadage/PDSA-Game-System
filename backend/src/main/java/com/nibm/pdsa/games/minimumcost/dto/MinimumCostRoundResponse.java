package com.nibm.pdsa.games.minimumcost.dto;

public record MinimumCostRoundResponse(
        long roundId,
        long roundNo,
        int n,
        int[][] matrix,
        String createdAt,
        AlgorithmRunResponse hungarian,
        AlgorithmRunResponse alternative
) {
}