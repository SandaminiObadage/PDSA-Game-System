package com.nibm.pdsa.games.minimumcost.model;

public record AssignmentResult(
        String algorithmName,
        String algorithmVariant,
        int totalCost,
        long executionTimeMs,
        int[] assignment
) {
}