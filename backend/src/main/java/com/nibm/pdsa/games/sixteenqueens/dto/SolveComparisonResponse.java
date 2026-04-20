package com.nibm.pdsa.games.sixteenqueens.dto;

import java.util.List;

public class SolveComparisonResponse {

    private long gameRoundId;
    private int boardSize;
    private long sequentialSolutionCount;
    private long parallelSolutionCount;
    private long sequentialTimeMs;
    private long parallelTimeMs;
    private double speedup;
    private int sequentialPersistedSolutionCount;
    private int parallelPersistedSolutionCount;
    private int persistedSolutionCount;
    private boolean samplesVisible;
    private List<String> sampleSolutions;

    public long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(long gameRoundId) {
        this.gameRoundId = gameRoundId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public long getSequentialSolutionCount() {
        return sequentialSolutionCount;
    }

    public void setSequentialSolutionCount(long sequentialSolutionCount) {
        this.sequentialSolutionCount = sequentialSolutionCount;
    }

    public long getParallelSolutionCount() {
        return parallelSolutionCount;
    }

    public void setParallelSolutionCount(long parallelSolutionCount) {
        this.parallelSolutionCount = parallelSolutionCount;
    }

    public long getSequentialTimeMs() {
        return sequentialTimeMs;
    }

    public void setSequentialTimeMs(long sequentialTimeMs) {
        this.sequentialTimeMs = sequentialTimeMs;
    }

    public long getParallelTimeMs() {
        return parallelTimeMs;
    }

    public void setParallelTimeMs(long parallelTimeMs) {
        this.parallelTimeMs = parallelTimeMs;
    }

    public double getSpeedup() {
        return speedup;
    }

    public void setSpeedup(double speedup) {
        this.speedup = speedup;
    }

    public int getSequentialPersistedSolutionCount() {
        return sequentialPersistedSolutionCount;
    }

    public void setSequentialPersistedSolutionCount(int sequentialPersistedSolutionCount) {
        this.sequentialPersistedSolutionCount = sequentialPersistedSolutionCount;
    }

    public int getParallelPersistedSolutionCount() {
        return parallelPersistedSolutionCount;
    }

    public void setParallelPersistedSolutionCount(int parallelPersistedSolutionCount) {
        this.parallelPersistedSolutionCount = parallelPersistedSolutionCount;
    }

    public int getPersistedSolutionCount() {
        return persistedSolutionCount;
    }

    public void setPersistedSolutionCount(int persistedSolutionCount) {
        this.persistedSolutionCount = persistedSolutionCount;
    }

    public boolean isSamplesVisible() {
        return samplesVisible;
    }

    public void setSamplesVisible(boolean samplesVisible) {
        this.samplesVisible = samplesVisible;
    }

    public List<String> getSampleSolutions() {
        return sampleSolutions;
    }

    public void setSampleSolutions(List<String> sampleSolutions) {
        this.sampleSolutions = sampleSolutions;
    }
}
