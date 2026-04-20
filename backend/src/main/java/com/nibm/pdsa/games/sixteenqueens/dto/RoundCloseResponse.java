package com.nibm.pdsa.games.sixteenqueens.dto;

public class RoundCloseResponse {

    private long gameTypeId;
    private String gameCode;
    private long gameRoundId;
    private boolean closed;
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

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}