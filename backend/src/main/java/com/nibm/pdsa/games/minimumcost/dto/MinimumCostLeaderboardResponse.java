package com.nibm.pdsa.games.minimumcost.dto;

import java.util.List;

public record MinimumCostLeaderboardResponse(
        Long roundId,
        List<MinimumCostLeaderboardEntry> leaderboard
) {
}