package com.nibm.pdsa.games.sixteenqueens.dto;

public class ResetRecognizedResponse {

    private long gameTypeId;
    private String gameCode;
    private long gameRoundId;
    private int clearedCount;
    private String message;

    public long getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(long gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(long gameRoundId) {
        this.gameRoundId = gameRoundId;
    }

    public int getClearedCount() {
        return clearedCount;
    }

    public void setClearedCount(int clearedCount) {
        this.clearedCount = clearedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
