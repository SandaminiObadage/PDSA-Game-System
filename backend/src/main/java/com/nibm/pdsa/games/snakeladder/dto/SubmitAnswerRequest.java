package com.nibm.pdsa.games.snakeladder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmitAnswerRequest {

    @NotNull
    private Long gameRoundId;

    @NotBlank
    private String playerName;

    @NotNull
    private Integer answer; // The chosen minimum throws

    @Min(6)
    @Max(12)
    private int boardSize = 8;

    public Long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(Long gameRoundId) {
        this.gameRoundId = gameRoundId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getAnswer() {
        return answer;
    }

    public void setAnswer(Integer answer) {
        this.answer = answer;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}