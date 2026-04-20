package com.nibm.pdsa.games.sixteenqueens.dto;

import java.util.List;

public class SampleSolutionsResponse {

    private long gameRoundId;
    private boolean samplesVisible;
    private List<String> sampleSolutions;
    private String message;

    public long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(long gameRoundId) {
        this.gameRoundId = gameRoundId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}