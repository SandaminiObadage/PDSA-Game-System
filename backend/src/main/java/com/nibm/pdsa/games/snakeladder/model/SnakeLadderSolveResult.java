package com.nibm.pdsa.games.snakeladder.model;

public class SnakeLadderSolveResult {
    private int minThrows;
    private long elapsedMs;

    public SnakeLadderSolveResult(int minThrows, long elapsedMs) {
        this.minThrows = minThrows;
        this.elapsedMs = elapsedMs;
    }

    public int getMinThrows() {
        return minThrows;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }
}