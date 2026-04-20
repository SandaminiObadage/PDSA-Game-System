package com.nibm.pdsa.games.minimumcost.dto;

public record AlgorithmRunResponse(
        String algorithmName,
        String algorithmVariant,
        int totalCost,
        long executionTimeMs,
        int[] assignment
) {
}