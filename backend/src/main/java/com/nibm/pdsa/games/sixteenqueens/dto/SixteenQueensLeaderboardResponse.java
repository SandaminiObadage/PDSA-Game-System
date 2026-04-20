package com.nibm.pdsa.games.sixteenqueens.dto;

import java.util.List;

public class SixteenQueensLeaderboardResponse {

    private long gameTypeId;
    private String gameCode;
    private List<LeaderboardEntry> leaderboard;

    public long getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(long gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderboardEntry> leaderboard) {
        this.leaderboard = leaderboard;
    }
}
