package com.nibm.pdsa.games.sixteenqueens.dto;

public class SubmitAnswerResponse {

    private String playerName;
    private boolean correct;
    private boolean alreadyRecognized;
    private String message;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isAlreadyRecognized() {
        return alreadyRecognized;
    }

    public void setAlreadyRecognized(boolean alreadyRecognized) {
        this.alreadyRecognized = alreadyRecognized;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
