package com.nibm.pdsa.games.sixteenqueens.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SolveRequest {

    @Min(8)
    @Max(16)
    private int boardSize = 16;

    @Min(1)
    @Max(64)
    private int threadCount = Runtime.getRuntime().availableProcessors();

    @Min(1)
    @Max(500)
    private int solutionSampleLimit = 50;

    @Min(0)
    @Max(5000)
    private int persistSolutionLimit = 200;

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getSolutionSampleLimit() {
        return solutionSampleLimit;
    }

    public void setSolutionSampleLimit(int solutionSampleLimit) {
        this.solutionSampleLimit = solutionSampleLimit;
    }

    public int getPersistSolutionLimit() {
        return persistSolutionLimit;
    }

    public void setPersistSolutionLimit(int persistSolutionLimit) {
        this.persistSolutionLimit = persistSolutionLimit;
    }
}
