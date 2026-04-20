package com.nibm.pdsa.games.traffic.dto;

import java.util.List;

public class TrafficLeaderboardResponse {
    private List<TrafficLeaderboardEntry> entries;
    private long generatedAt;

    public TrafficLeaderboardResponse(List<TrafficLeaderboardEntry> entries) {
        this.entries = entries;
        this.generatedAt = System.currentTimeMillis();
    }

    public List<TrafficLeaderboardEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<TrafficLeaderboardEntry> entries) {
        this.entries = entries;
    }

    public long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(long generatedAt) {
        this.generatedAt = generatedAt;
    }
}
