package com.nibm.pdsa.games.knightstour.dto;

public class LeaderboardEntryResponse {

    private String playerName;
    private long wins;
    private int mostMoves;
    private boolean hasWin;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public long getWins() {
        return wins;
    }

    public void setWins(long wins) {
        this.wins = wins;
    }

    public int getMostMoves() {
        return mostMoves;
    }

    public void setMostMoves(int mostMoves) {
        this.mostMoves = mostMoves;
    }

    public boolean isHasWin() {
        return hasWin;
    }

    public void setHasWin(boolean hasWin) {
        this.hasWin = hasWin;
    }
}
