package com.nibm.pdsa.games.minimumcost.dto;

public class HealthResponse {

    private String status;
    private String version;
    private String game;

    public HealthResponse() {}

    public HealthResponse(String status, String version, String game) {
        this.status = status;
        this.version = version;
        this.game = game;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }
}