package com.nibm.pdsa.games.sixteenqueens.dto;

import jakarta.validation.constraints.NotBlank;

public class SubmitAnswerRequest {

    private Long gameRoundId;

    @NotBlank
    private String playerName;

    @NotBlank
    private String answer; // Example: 0,4,7,5,2,6,1,3 for 8x8

    private int boardSize = 16;

    public String getPlayerName() {
        return playerName;
    }

    public Long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(Long gameRoundId) {
        this.gameRoundId = gameRoundId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}
