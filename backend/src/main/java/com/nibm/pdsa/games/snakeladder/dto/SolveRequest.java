package com.nibm.pdsa.games.snakeladder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SolveRequest {

    @Min(6)
    @Max(12)
    private int boardSize = 8;

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}