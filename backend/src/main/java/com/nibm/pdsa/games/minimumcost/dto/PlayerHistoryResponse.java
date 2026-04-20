package com.nibm.pdsa.games.minimumcost.dto;

import java.util.List;

public class PlayerHistoryResponse {

    private String playerName;
    private List<PlayerResultDto> results;
    private long totalGames;
    private long correctAnswers;
    private String accuracy;
    private String message;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<PlayerResultDto> getResults() {
        return results;
    }

    public void setResults(List<PlayerResultDto> results) {
        this.results = results;
    }

    public long getTotalGames() {
        return totalGames;
    }

    public void setTotalGames(long totalGames) {
        this.totalGames = totalGames;
    }

    public long getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(long correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}