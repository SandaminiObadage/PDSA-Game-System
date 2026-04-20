package com.nibm.pdsa.games.traffic.algorithm;

import com.nibm.pdsa.games.traffic.model.TrafficEdge;
import com.nibm.pdsa.games.traffic.model.TrafficGraph;
import com.nibm.pdsa.games.traffic.model.MaxFlowResult;

import java.util.*;

/**
 * Ford-Fulkerson algorithm for finding maximum flow.
 * Uses DFS to find augmenting paths.
 */
public class FordFulkersonAlgorithm {

    public MaxFlowResult findMaxFlow(TrafficGraph graph, String source, String sink) {
        if (source == null || sink == null || !graph.getNodes().contains(source) || !graph.getNodes().contains(sink)) {
            throw new IllegalArgumentException("Invalid source or sink node");
        }

        long startTime = System.nanoTime();

        // Create residual graph
        TrafficGraph residualGraph = createResidualGraph(graph);
        
        int maxFlow = 0;
        Map<String, Map<String, Integer>> flowMap = initializeFlowMap(graph);

        // Find augmenting paths until no more paths exist
        while (true) {
            List<String> path = findAugmentingPath(residualGraph, source, sink);
            if (path == null || path.isEmpty()) {
                break;
            }

            // Find bottleneck capacity
            int pathFlow = findBottleneckCapacity(residualGraph, path);

            // Update residual graph and flow
            updateResidualGraph(residualGraph, path, pathFlow, graph, flowMap);
            maxFlow += pathFlow;
        }

        long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;

        return new MaxFlowResult(maxFlow, executionTimeMs, "Ford-Fulkerson", flowMap);
    }

    /**
     * Create residual graph with reverse edges having 0 capacity.
     */
    private TrafficGraph createResidualGraph(TrafficGraph graph) {
        TrafficGraph residual = new TrafficGraph();
        
        // Add all nodes
        for (String node : graph.getNodes()) {
            residual.addNode(node);
        }

        // Add forward edges with original capacity
        for (TrafficEdge edge : graph.getAllEdges()) {
            residual.addEdge(edge.getFrom(), edge.getTo(), edge.getCapacity());
        }

        // Add reverse edges with 0 capacity
        for (TrafficEdge edge : graph.getAllEdges()) {
            residual.addEdge(edge.getTo(), edge.getFrom(), 0);
        }

        return residual;
    }

    /**
     * Find augmenting path using DFS.
     */
    private List<String> findAugmentingPath(TrafficGraph residualGraph, String source, String sink) {
        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();
        
        if (dfs(residualGraph, source, sink, visited, path)) {
            return path;
        }
        return null;
    }

    /**
     * DFS helper to find path.
     */
    private boolean dfs(TrafficGraph graph, String current, String sink, Set<String> visited, List<String> path) {
        if (current.equals(sink)) {
            path.add(current);
            return true;
        }

        visited.add(current);
        
        for (TrafficEdge edge : graph.getOutgoingEdges(current)) {
            if (!visited.contains(edge.getTo()) && edge.getCapacity() > 0) {
                if (dfs(graph, edge.getTo(), sink, visited, path)) {
                    path.add(0, current);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Find minimum capacity along the path.
     */
    private int findBottleneckCapacity(TrafficGraph graph, List<String> path) {
        int minCapacity = Integer.MAX_VALUE;
        
        for (int i = 0; i < path.size() - 1; i++) {
            TrafficEdge edge = graph.getEdge(path.get(i), path.get(i + 1));
            if (edge != null) {
                minCapacity = Math.min(minCapacity, edge.getCapacity());
            }
        }

        return minCapacity;
    }

    /**
     * Update residual graph and flow map with the path flow.
     */
    private void updateResidualGraph(TrafficGraph residual, List<String> path, int flow,
                                    TrafficGraph originalGraph, Map<String, Map<String, Integer>> flowMap) {
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);

            // Update forward edge in residual graph
            TrafficEdge forwardEdge = residual.getEdge(from, to);
            if (forwardEdge != null) {
                // Reduce forward capacity by the flow amount
                int newCapacity = forwardEdge.getCapacity() - flow;
                forwardEdge.updateCapacity(newCapacity);
            }

            // Update reverse edge in residual graph
            TrafficEdge reverseEdge = residual.getEdge(to, from);
            if (reverseEdge != null) {
                // Increase reverse capacity by the flow amount
                int newCapacity = reverseEdge.getCapacity() + flow;
                reverseEdge.updateCapacity(newCapacity);
            }

            // Update flow map
            if (originalGraph.getEdge(from, to) != null) {
                int currentFlow = flowMap.get(from).getOrDefault(to, 0);
                flowMap.get(from).put(to, currentFlow + flow);
            }
        }
    }

    /**
     * Initialize flow map with zeros.
     */
    private Map<String, Map<String, Integer>> initializeFlowMap(TrafficGraph graph) {
        Map<String, Map<String, Integer>> flowMap = new HashMap<>();
        
        for (String node : graph.getNodes()) {
            flowMap.put(node, new HashMap<>());
        }

        for (TrafficEdge edge : graph.getAllEdges()) {
            flowMap.get(edge.getFrom()).put(edge.getTo(), 0);
        }

        return flowMap;
    }
}
