package com.nibm.pdsa.games.snakeladder.dto;

public class LeaderboardEntry {

    private long playerId;
    private String playerName;
    private long totalAnswers;
    private long correctAnswers;
    private double accuracy;
    private String lastSubmittedAt;

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(long totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public long getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(long correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getLastSubmittedAt() {
        return lastSubmittedAt;
    }

    public void setLastSubmittedAt(String lastSubmittedAt) {
        this.lastSubmittedAt = lastSubmittedAt;
    }
}
