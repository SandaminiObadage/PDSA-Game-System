package com.nibm.pdsa.games.sixteenqueens.dto;

public class RoundHistoryItem {

    private long gameRoundId;
    private long roundNo;
    private int boardSize;
    private int threadCount;
    private long sequentialTimeMs;
    private long parallelTimeMs;
    private long playerAnswerCount;
    private String createdAt;

    public long getGameRoundId() {
        return gameRoundId;
    }

    public void setGameRoundId(long gameRoundId) {
        this.gameRoundId = gameRoundId;
    }

    public long getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(long roundNo) {
        this.roundNo = roundNo;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public long getSequentialTimeMs() {
        return sequentialTimeMs;
    }

    public void setSequentialTimeMs(long sequentialTimeMs) {
        this.sequentialTimeMs = sequentialTimeMs;
    }

    public long getParallelTimeMs() {
        return parallelTimeMs;
    }

    public void setParallelTimeMs(long parallelTimeMs) {
        this.parallelTimeMs = parallelTimeMs;
    }

    public long getPlayerAnswerCount() {
        return playerAnswerCount;
    }

    public void setPlayerAnswerCount(long playerAnswerCount) {
        this.playerAnswerCount = playerAnswerCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
