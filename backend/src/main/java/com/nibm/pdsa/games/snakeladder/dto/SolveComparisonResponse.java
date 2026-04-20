package com.nibm.pdsa.games.snakeladder.dto;

import com.nibm.pdsa.games.snakeladder.model.Ladder;
import com.nibm.pdsa.games.snakeladder.model.Snake;

import java.util.List;

public class SolveComparisonResponse {
    private int boardSize;
    private List<Ladder> ladders;
    private List<Snake> snakes;
    private int bfsMinThrows;
    private int dpMinThrows;
    private long bfsTimeMs;
    private long dpTimeMs;
    private Long gameRoundId;
    private List<Integer> choices; // 3 choices for player

    // Getters and setters
    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public List<Ladder> getLadders() {
        return ladders;
    }

    public void setLadders(List<Ladder> ladders) {
        this.ladders = ladders;
    }

    public List<Snake> getSnakes() {
        return snakes;
    }

    public void setSnakes(List<Snake> snakes) {
        this.snakes = snakes;
    }

    public int getBfsMinThrows() {
        return bfsMinThrows;
    }

    public void setBfsMinThrows(int bfsMinThrows) {
        this.bfsMinThrows = bfsMinThrows;
    }

    public int getDpMinThrows() {
        return dpMinThrows;
    }

    public void setDpMinThrows(int dpMinThrows) {
        this.dpMinThrows = dpMinThrows;
    }

    public long getBfsTimeMs() {
        return bfsTimeMs;
    }

    public void setBfsTimeMs(long bfsTimeMs) {
        this.bfsTimeMs = bfsTimeMs;
    }

    public long getDpTimeMs() {
        return dpTimeMs;
    }

    public void setDpTimeMs(long dpTimeMs) {
        this.dpTimeMs = dpTimeMs;
    }

    public Long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(Long gameRoundId) {
        this.gameRoundId = gameRoundId;
    }

    public List<Integer> getChoices() {
        return choices;
    }

    public void setChoices(List<Integer> choices) {
        this.choices = choices;
    }
}