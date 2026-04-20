
package com.nibm.pdsa.games.minimumcost.dto;

public class PlayerResultDto {

    private Long id;
    private String playerName;
    private int correctCost;
    private int selectedCost;
    private int timeRemaining;
    private boolean isCorrect;
    private long timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCorrectCost() {
        return correctCost;
    }

    public void setCorrectCost(int correctCost) {
        this.correctCost = correctCost;
    }

    public int getSelectedCost() {
        return selectedCost;
    }

    public void setSelectedCost(int selectedCost) {
        this.selectedCost = selectedCost;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}