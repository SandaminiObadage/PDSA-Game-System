package com.nibm.pdsa.games.knightstour.dto;

public class StartGameResponse {

    private Long knightId;
    private String startPosition;
    private String algorithmType;

    public Long getKnightId() {
        return knightId;
    }

    public void setKnightId(Long knightId) {
        this.knightId = knightId;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(String startPosition) {
        this.startPosition = startPosition;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
}
