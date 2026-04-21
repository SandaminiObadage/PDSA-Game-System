package com.nibm.pdsa.games.knightstour.dto;

import jakarta.validation.constraints.NotNull;

public class StartGameRequest {

    @NotNull(message = "must not be null")
    private Integer boardSize;

    private String algorithmType;

    public Integer getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(Integer boardSize) {
        this.boardSize = boardSize;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
}
