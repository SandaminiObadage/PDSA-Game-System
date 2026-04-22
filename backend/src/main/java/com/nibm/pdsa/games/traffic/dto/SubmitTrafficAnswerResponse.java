package com.nibm.pdsa.games.traffic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubmitTrafficAnswerResponse {
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    
    @JsonProperty("correctAnswer")
    private int correctAnswer;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("algorithmExecutionTimeMs")
    private long algorithmExecutionTimeMs;

    public SubmitTrafficAnswerResponse(boolean isCorrect, int correctAnswer, String message, long algorithmExecutionTimeMs) {
        this.isCorrect = isCorrect;
        this.correctAnswer = correctAnswer;
        this.message = message;
        this.algorithmExecutionTimeMs = algorithmExecutionTimeMs;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getAlgorithmExecutionTimeMs() {
        return algorithmExecutionTimeMs;
    }

    public void setAlgorithmExecutionTimeMs(long algorithmExecutionTimeMs) {
        this.algorithmExecutionTimeMs = algorithmExecutionTimeMs;
    }
}
