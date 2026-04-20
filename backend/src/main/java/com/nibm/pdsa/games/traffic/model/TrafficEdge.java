package com.nibm.pdsa.games.traffic.model;

/**
 * Represents an edge (road segment) in the traffic network.
 */
public class TrafficEdge {
    private final String from;
    private final String to;
    private int capacity;
    private int flow;

    public TrafficEdge(String from, String to, int capacity) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
    }

    // Copy constructor for residual graph
    public TrafficEdge(TrafficEdge edge) {
        this.from = edge.from;
        this.to = edge.to;
        this.capacity = edge.capacity;
        this.flow = 0;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        if (flow < 0 || flow > capacity) {
            throw new IllegalArgumentException("Flow must be between 0 and capacity");
        }
        this.flow = flow;
    }

    public int getResidualCapacity() {
        return capacity - flow;
    }

    public void addFlow(int amount) {
        if (flow + amount > capacity || flow + amount < 0) {
            throw new IllegalArgumentException("Invalid flow amount");
        }
        this.flow += amount;
    }

    public void updateCapacity(int newCapacity) {
        if (newCapacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        this.capacity = newCapacity;
    }

    @Override
    public String toString() {
        return from + "->" + to + " (cap=" + capacity + ", flow=" + flow + ")";
    }
}
