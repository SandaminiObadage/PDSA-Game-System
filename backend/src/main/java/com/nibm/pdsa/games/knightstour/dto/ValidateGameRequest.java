package com.nibm.pdsa.games.knightstour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ValidateGameRequest {

    @NotNull(message = "must not be null")
    private Long knightId;

    @NotEmpty(message = "must not be empty")
    private List<String> moves;

    @NotBlank(message = "must not be blank")
    private String playerName;

    private String outcomeOverride;
    private String outcomeMessage;

    public Long getKnightId() {
        return knightId;
    }

    public void setKnightId(Long knightId) {
        this.knightId = knightId;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getOutcomeOverride() {
        return outcomeOverride;
    }

    public void setOutcomeOverride(String outcomeOverride) {
        this.outcomeOverride = outcomeOverride;
    }

    public String getOutcomeMessage() {
        return outcomeMessage;
    }

    public void setOutcomeMessage(String outcomeMessage) {
        this.outcomeMessage = outcomeMessage;
    }
}
