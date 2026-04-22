package com.nibm.pdsa.games.traffic.model;

import java.util.*;

/**
 * Represents the traffic network graph with nodes and edges.
 */
public class TrafficGraph {
    private final Map<String, List<TrafficEdge>> adjacencyList;
    private final Set<String> nodes;

    public TrafficGraph() {
        this.adjacencyList = new HashMap<>();
        this.nodes = new HashSet<>();
    }

    public void addNode(String node) {
        nodes.add(node);
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    public void addEdge(String from, String to, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        addNode(from);
        addNode(to);
        adjacencyList.get(from).add(new TrafficEdge(from, to, capacity));
    }

    public List<TrafficEdge> getOutgoingEdges(String from) {
        return adjacencyList.getOrDefault(from, new ArrayList<>());
    }

    public Set<String> getNodes() {
        return new HashSet<>(nodes);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return adjacencyList.values().stream().mapToInt(List::size).sum();
    }

    public TrafficEdge getEdge(String from, String to) {
        return getOutgoingEdges(from).stream()
                .filter(e -> e.getTo().equals(to))
                .findFirst()
                .orElse(null);
    }

    public List<TrafficEdge> getAllEdges() {
        List<TrafficEdge> edges = new ArrayList<>();
        for (List<TrafficEdge> edgeList : adjacencyList.values()) {
            edges.addAll(edgeList);
        }
        return edges;
    }

    public TrafficGraph copy() {
        TrafficGraph copy = new TrafficGraph();
        for (String node : nodes) {
            copy.addNode(node);
        }
        for (List<TrafficEdge> edges : adjacencyList.values()) {
            for (TrafficEdge edge : edges) {
                copy.addEdge(edge.getFrom(), edge.getTo(), edge.getCapacity());
            }
        }
        return copy;
    }
}
