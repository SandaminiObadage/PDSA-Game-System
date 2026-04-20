package com.nibm.pdsa.games.minimumcost.dto;

public class SaveScoreResponse {

    private boolean success;
    private Long id;
    private boolean isCorrect;
    private String message;
    private int correctCost;
    private int selectedCost;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}