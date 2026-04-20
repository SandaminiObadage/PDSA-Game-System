package com.nibm.pdsa.games.traffic.model;

import java.util.*;

/**
 * Represents a single round of the Traffic Simulation game.
 */
public class TrafficGameRound {
    private final String source;
    private final String sink;
    private final TrafficGraph graph;
    private final Map<String, Integer> edgeCapacities;
    private int correctMaxFlow;
    private long algorithmExecutionTimeMs;

    public TrafficGameRound(TrafficGraph graph, String source, String sink, 
                            Map<String, Integer> edgeCapacities) {
        this.graph = graph;
        this.source = source;
        this.sink = sink;
        this.edgeCapacities = new HashMap<>(edgeCapacities);
        this.correctMaxFlow = -1;
        this.algorithmExecutionTimeMs = -1;
    }

    public String getSource() {
        return source;
    }

    public String getSink() {
        return sink;
    }

    public TrafficGraph getGraph() {
        return graph;
    }

    public Map<String, Integer> getEdgeCapacities() {
        return new HashMap<>(edgeCapacities);
    }

    public int getCorrectMaxFlow() {
        return correctMaxFlow;
    }

    public void setCorrectMaxFlow(int correctMaxFlow) {
        this.correctMaxFlow = correctMaxFlow;
    }

    public long getAlgorithmExecutionTimeMs() {
        return algorithmExecutionTimeMs;
    }

    public void setAlgorithmExecutionTimeMs(long executionTimeMs) {
        this.algorithmExecutionTimeMs = executionTimeMs;
    }

    public boolean isAnswerCorrect(int playerAnswer) {
        return correctMaxFlow >= 0 && playerAnswer == correctMaxFlow;
    }
}
