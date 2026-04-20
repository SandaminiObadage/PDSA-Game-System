package com.nibm.pdsa.games.traffic.dto;

import java.util.List;

public class TrafficLeaderboardEntry {
    private String playerName;
    private int correctAnswers;
    private double averageExecutionTimeMs;
    private long lastPlayedAt;

    public TrafficLeaderboardEntry(String playerName, int correctAnswers, double averageExecutionTimeMs, long lastPlayedAt) {
        this.playerName = playerName;
        this.correctAnswers = correctAnswers;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.lastPlayedAt = lastPlayedAt;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public double getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(double averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }

    public long getLastPlayedAt() {
        return lastPlayedAt;
    }

    public void setLastPlayedAt(long lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }
}
