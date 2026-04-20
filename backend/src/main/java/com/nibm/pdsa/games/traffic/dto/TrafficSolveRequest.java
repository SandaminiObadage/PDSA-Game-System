package com.nibm.pdsa.games.traffic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class TrafficSolveRequest {
    @NotBlank(message = "Source node cannot be blank")
    private String source;

    @NotBlank(message = "Sink node cannot be blank")
    private String sink;

    @Min(value = 1, message = "Thread count must be at least 1")
    private int threadCount = 1;

    public TrafficSolveRequest() {}

    public TrafficSolveRequest(String source, String sink, int threadCount) {
        this.source = source;
        this.sink = sink;
        this.threadCount = threadCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSink() {
        return sink;
    }

    public void setSink(String sink) {
        this.sink = sink;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
