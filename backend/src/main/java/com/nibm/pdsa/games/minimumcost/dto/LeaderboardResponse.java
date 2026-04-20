package com.nibm.pdsa.games.minimumcost.dto;

import java.util.List;

public class LeaderboardResponse {

    private List<PlayerResultDto> results;
    private int total;
    private String message;

    public List<PlayerResultDto> getResults() {
        return results;
    }

    public void setResults(List<PlayerResultDto> results) {
        this.results = results;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}