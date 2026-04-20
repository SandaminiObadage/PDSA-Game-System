package com.nibm.pdsa.games.sixteenqueens.dto;

public class SixteenQueensReportResponse {

    private long gameTypeId;
    private String gameCode;
    private long totalRounds;
    private long totalPlayerAnswers;
    private long totalCorrectAnswers;
    private long totalKnownSolutionsPersisted;
    private long activeRecognizedSolutions;
    private double averageSequentialTimeMs;
    private double averageParallelTimeMs;
    private double averageSpeedup;
    private long bestSequentialTimeMs;
    private long bestParallelTimeMs;

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

    public long getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(long totalRounds) {
        this.totalRounds = totalRounds;
    }

    public long getTotalPlayerAnswers() {
        return totalPlayerAnswers;
    }

    public void setTotalPlayerAnswers(long totalPlayerAnswers) {
        this.totalPlayerAnswers = totalPlayerAnswers;
    }

    public long getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(long totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public long getTotalKnownSolutionsPersisted() {
        return totalKnownSolutionsPersisted;
    }

    public void setTotalKnownSolutionsPersisted(long totalKnownSolutionsPersisted) {
        this.totalKnownSolutionsPersisted = totalKnownSolutionsPersisted;
    }

    public long getActiveRecognizedSolutions() {
        return activeRecognizedSolutions;
    }

    public void setActiveRecognizedSolutions(long activeRecognizedSolutions) {
        this.activeRecognizedSolutions = activeRecognizedSolutions;
    }

    public double getAverageSequentialTimeMs() {
        return averageSequentialTimeMs;
    }

    public void setAverageSequentialTimeMs(double averageSequentialTimeMs) {
        this.averageSequentialTimeMs = averageSequentialTimeMs;
    }

    public double getAverageParallelTimeMs() {
        return averageParallelTimeMs;
    }

    public void setAverageParallelTimeMs(double averageParallelTimeMs) {
        this.averageParallelTimeMs = averageParallelTimeMs;
    }

    public double getAverageSpeedup() {
        return averageSpeedup;
    }

    public void setAverageSpeedup(double averageSpeedup) {
        this.averageSpeedup = averageSpeedup;
    }

    public long getBestSequentialTimeMs() {
        return bestSequentialTimeMs;
    }

    public void setBestSequentialTimeMs(long bestSequentialTimeMs) {
        this.bestSequentialTimeMs = bestSequentialTimeMs;
    }

    public long getBestParallelTimeMs() {
        return bestParallelTimeMs;
    }

    public void setBestParallelTimeMs(long bestParallelTimeMs) {
        this.bestParallelTimeMs = bestParallelTimeMs;
    }
}
