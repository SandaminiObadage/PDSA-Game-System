package com.nibm.pdsa.games.traffic.dto;

import java.util.Map;

public class MaxFlowComparisonResponse {
    private String roundId;
    private Map<String, Object> networkData;
    private Map<String, Object> fordFulkersonResult;
    private Map<String, Object> dinicResult;
    private long createdAt;

    public MaxFlowComparisonResponse(String roundId, Map<String, Object> networkData,
                                     Map<String, Object> fordFulkersonResult,
                                     Map<String, Object> dinicResult) {
        this.roundId = roundId;
        this.networkData = networkData;
        this.fordFulkersonResult = fordFulkersonResult;
        this.dinicResult = dinicResult;
        this.createdAt = System.currentTimeMillis();
    }

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }

    public Map<String, Object> getNetworkData() {
        return networkData;
    }

    public void setNetworkData(Map<String, Object> networkData) {
        this.networkData = networkData;
    }

    public Map<String, Object> getFordFulkersonResult() {
        return fordFulkersonResult;
    }

    public void setFordFulkersonResult(Map<String, Object> fordFulkersonResult) {
        this.fordFulkersonResult = fordFulkersonResult;
    }

    public Map<String, Object> getDinicResult() {
        return dinicResult;
    }

    public void setDinicResult(Map<String, Object> dinicResult) {
        this.dinicResult = dinicResult;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
