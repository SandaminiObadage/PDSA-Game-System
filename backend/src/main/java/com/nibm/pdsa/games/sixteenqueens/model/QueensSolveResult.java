package com.nibm.pdsa.games.sixteenqueens.model;

import java.util.List;

public class QueensSolveResult {

    private final long solutionCount;
    private final long elapsedMs;
    private final List<String> sampleSolutions;

    public QueensSolveResult(long solutionCount, long elapsedMs, List<String> sampleSolutions) {
        this.solutionCount = solutionCount;
        this.elapsedMs = elapsedMs;
        this.sampleSolutions = sampleSolutions;
    }

    public long getSolutionCount() {
        return solutionCount;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public List<String> getSampleSolutions() {
        return sampleSolutions;
    }
}
