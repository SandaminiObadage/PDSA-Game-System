package com.nibm.pdsa.games.traffic.model;

import java.util.*;

/**
 * Represents the result of a maximum flow algorithm.
 */
public class MaxFlowResult {
    private final int maxFlow;
    private final long executionTimeMs;
    private final String algorithmName;
    private final Map<String, Map<String, Integer>> flowMap;

    public MaxFlowResult(int maxFlow, long executionTimeMs, String algorithmName, 
                         Map<String, Map<String, Integer>> flowMap) {
        this.maxFlow = maxFlow;
        this.executionTimeMs = executionTimeMs;
        this.algorithmName = algorithmName;
        this.flowMap = flowMap;
    }

    public int getMaxFlow() {
        return maxFlow;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public Map<String, Map<String, Integer>> getFlowMap() {
        return flowMap;
    }

    @Override
    public String toString() {
        return "MaxFlowResult{" +
                "maxFlow=" + maxFlow +
                ", executionTimeMs=" + executionTimeMs +
                ", algorithmName='" + algorithmName + '\'' +
                '}';
    }
}
