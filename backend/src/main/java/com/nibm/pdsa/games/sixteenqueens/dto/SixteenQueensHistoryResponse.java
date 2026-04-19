package com.nibm.pdsa.games.sixteenqueens.dto;

import java.util.List;

public class SixteenQueensHistoryResponse {

    private long gameTypeId;
    private String gameCode;
    private List<RoundHistoryItem> rounds;
    private List<PlayerAnswerHistoryItem> recentAnswers;

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

    public List<RoundHistoryItem> getRounds() {
        return rounds;
    }

    public void setRounds(List<RoundHistoryItem> rounds) {
        this.rounds = rounds;
    }

    public List<PlayerAnswerHistoryItem> getRecentAnswers() {
        return recentAnswers;
    }

    public void setRecentAnswers(List<PlayerAnswerHistoryItem> recentAnswers) {
        this.recentAnswers = recentAnswers;
    }
}
