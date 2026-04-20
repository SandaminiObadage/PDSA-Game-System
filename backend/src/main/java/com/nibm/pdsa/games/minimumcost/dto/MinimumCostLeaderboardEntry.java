package com.nibm.pdsa.games.minimumcost.dto;

public record MinimumCostLeaderboardEntry(
        String playerName,
        int bestSubmittedCost,
        int optimalSubmissions,
        int totalSubmissions,
        String lastSubmittedAt
) {
}