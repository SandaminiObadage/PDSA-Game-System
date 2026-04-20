package com.nibm.pdsa.games.traffic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SubmitTrafficAnswerRequest {
    @NotBlank(message = "Player name cannot be blank")
    private String playerName;

    @Min(value = 0, message = "Maximum flow must be non-negative")
    private int playerAnswer;

    public SubmitTrafficAnswerRequest() {}

    public SubmitTrafficAnswerRequest(String playerName, int playerAnswer) {
        this.playerName = playerName;
        this.playerAnswer = playerAnswer;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getPlayerAnswer() {
        return playerAnswer;
    }

    public void setPlayerAnswer(int playerAnswer) {
        this.playerAnswer = playerAnswer;
    }
}
