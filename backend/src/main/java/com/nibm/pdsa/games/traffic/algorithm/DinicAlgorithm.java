package com.nibm.pdsa.games.traffic.algorithm;

import com.nibm.pdsa.games.traffic.model.TrafficEdge;
import com.nibm.pdsa.games.traffic.model.TrafficGraph;
import com.nibm.pdsa.games.traffic.model.MaxFlowResult;

import java.util.*;

/**
 * Dinic's algorithm for finding maximum flow.
 * More efficient than Ford-Fulkerson, especially for dense graphs.
 */
public class DinicAlgorithm {

    public MaxFlowResult findMaxFlow(TrafficGraph graph, String source, String sink) {
        if (source == null || sink == null || !graph.getNodes().contains(source) || !graph.getNodes().contains(sink)) {
            throw new IllegalArgumentException("Invalid source or sink node");
        }

        long startTime = System.nanoTime();

        // Create residual graph
        Map<String, List<DinicEdge>> residualGraph = createResidualGraph(graph);
        
        int maxFlow = 0;
        Map<String, Map<String, Integer>> flowMap = initializeFlowMap(graph);

        while (true) {
            // Build level graph
            Map<String, Integer> levels = buildLevelGraph(residualGraph, source, sink);
            
            if (levels.get(sink) == -1) {
                break;  // No augmenting path exists
            }

            // Find blocking flows
            Map<String, Integer> iterators = new HashMap<>();
            for (String node : graph.getNodes()) {
                iterators.put(node, 0);
            }

            int blockingFlow;
            while ((blockingFlow = sendFlow(residualGraph, source, sink, Integer.MAX_VALUE, levels, 
                                           iterators, new HashSet<>(), flowMap, graph)) > 0) {
                maxFlow += blockingFlow;
            }
        }

        long executionTimeMs = (System.nanoTime() - startTime) / 1_000_000;

        return new MaxFlowResult(maxFlow, executionTimeMs, "Dinic's Algorithm", flowMap);
    }

    /**
     * Create residual graph representation using adjacency list of DinicEdges.
     */
    private Map<String, List<DinicEdge>> createResidualGraph(TrafficGraph graph) {
        Map<String, List<DinicEdge>> residual = new HashMap<>();
        
        // Initialize adjacency list
        for (String node : graph.getNodes()) {
            residual.put(node, new ArrayList<>());
        }

        // Add edges
        for (TrafficEdge edge : graph.getAllEdges()) {
            DinicEdge forward = new DinicEdge(edge.getTo(), edge.getCapacity());
            DinicEdge reverse = new DinicEdge(edge.getFrom(), 0);
            forward.reverse = reverse;
            reverse.reverse = forward;
            
            residual.get(edge.getFrom()).add(forward);
            residual.get(edge.getTo()).add(reverse);
        }

        return residual;
    }

    /**
     * Build level graph using BFS.
     */
    private Map<String, Integer> buildLevelGraph(Map<String, List<DinicEdge>> graph, String source, String sink) {
        Map<String, Integer> levels = new HashMap<>();
        
        for (String node : graph.keySet()) {
            levels.put(node, -1);
        }

        levels.put(source, 0);
        Queue<String> queue = new LinkedList<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            for (DinicEdge edge : graph.get(current)) {
                if (levels.get(edge.to) == -1 && edge.capacity > 0) {
                    levels.put(edge.to, levels.get(current) + 1);
                    queue.add(edge.to);
                }
            }
        }

        return levels;
    }

    /**
     * Send flow using DFS with level graph.
     */
    private int sendFlow(Map<String, List<DinicEdge>> graph, String current, String sink, 
                        int flow, Map<String, Integer> levels, Map<String, Integer> iterators,
                        Set<String> inRecursion, Map<String, Map<String, Integer>> flowMap, TrafficGraph originalGraph) {
        
        if (current.equals(sink)) {
            return flow;
        }

        if (inRecursion.contains(current)) {
            return 0;
        }

        inRecursion.add(current);
        List<DinicEdge> edges = graph.get(current);
        
        for (int i = iterators.get(current); i < edges.size(); i++) {
            DinicEdge edge = edges.get(i);
            
            if (levels.get(edge.to) == levels.get(current) + 1 && edge.capacity > 0) {
                int minFlow = Math.min(flow, edge.capacity);
                int pushed = sendFlow(graph, edge.to, sink, minFlow, levels, iterators, inRecursion, flowMap, originalGraph);
                
                if (pushed > 0) {
                    edge.capacity -= pushed;
                    if (edge.reverse != null) {
                        edge.reverse.capacity += pushed;
                    }
                    
                    // Update flow map if this is an original edge
                    if (originalGraph.getEdge(current, edge.to) != null) {
                        int currentFlow = flowMap.get(current).getOrDefault(edge.to, 0);
                        flowMap.get(current).put(edge.to, currentFlow + pushed);
                    }
                    
                    inRecursion.remove(current);
                    return pushed;
                }
            }
            
            iterators.put(current, i + 1);
        }

        inRecursion.remove(current);
        return 0;
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

    /**
     * Inner class to represent edges in Dinic's algorithm.
     */
    private static class DinicEdge {
        String to;
        int capacity;
        DinicEdge reverse;

        DinicEdge(String to, int capacity) {
            this.to = to;
            this.capacity = capacity;
        }
    }
}
