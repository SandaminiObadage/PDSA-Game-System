package com.nibm.pdsa.games.traffic.service;

import com.nibm.pdsa.games.traffic.algorithm.DinicAlgorithm;
import com.nibm.pdsa.games.traffic.algorithm.FordFulkersonAlgorithm;
import com.nibm.pdsa.games.traffic.dto.*;
import com.nibm.pdsa.games.traffic.model.MaxFlowResult;
import com.nibm.pdsa.games.traffic.model.TrafficEdge;
import com.nibm.pdsa.games.traffic.model.TrafficGameRound;
import com.nibm.pdsa.games.traffic.model.TrafficGraph;
import com.nibm.pdsa.games.traffic.repository.TrafficRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for Traffic Simulation Game.
 */
@Service
public class TrafficService {

    private final TrafficRepository trafficRepository;
    private final FordFulkersonAlgorithm fordFulkerson;
    private final DinicAlgorithm dinic;
    private final Random random;

    // Static nodes for the traffic network
    private static final String[] NODES = {"A", "B", "C", "D", "E", "F", "G", "H", "T"};
    private static final String SOURCE = "A";
    private static final String SINK = "T";
    private static final int MIN_CAPACITY = 5;
    private static final int MAX_CAPACITY = 15;

    // Edge definitions: from -> [to nodes]
    private static final Map<String, String[]> EDGES = Map.ofEntries(
            Map.entry("A", new String[]{"B", "C", "D"}),
            Map.entry("B", new String[]{"E", "F"}),
            Map.entry("C", new String[]{"E", "F"}),
            Map.entry("D", new String[]{"F"}),
            Map.entry("E", new String[]{"G", "H"}),
            Map.entry("F", new String[]{"H"}),
            Map.entry("G", new String[]{"T"}),
            Map.entry("H", new String[]{"T"})
    );

    public TrafficService(TrafficRepository trafficRepository) {
        this.trafficRepository = trafficRepository;
        this.fordFulkerson = new FordFulkersonAlgorithm();
        this.dinic = new DinicAlgorithm();
        this.random = new Random();
        trafficRepository.initializeGameType();
    }

    /**
     * Generate a new game round with random capacities.
     */
    public MaxFlowComparisonResponse generateNewRound() {
        TrafficGraph graph = generateRandomGraph();
        Map<String, Integer> edgeCapacities = captureEdgeCapacities(graph);
        
        // Save round to database
        long roundId = trafficRepository.saveGameRound(SOURCE, SINK, edgeCapacities);
        System.out.println("DEBUG: Created game round with ID: " + roundId);

        // Run algorithms
        MaxFlowResult fordResult = fordFulkerson.findMaxFlow(graph, SOURCE, SINK);
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, SOURCE, SINK);
        
        System.out.println("DEBUG: Ford-Fulkerson result: maxFlow=" + fordResult.getMaxFlow());
        System.out.println("DEBUG: Dinic result: maxFlow=" + dinicResult.getMaxFlow());

        // Save algorithm results
        trafficRepository.saveAlgorithmResult(roundId, "Ford-Fulkerson", fordResult.getExecutionTimeMs(), 
                                            fordResult.getMaxFlow(), fordResult.getFlowMap());
        trafficRepository.saveAlgorithmResult(roundId, "Dinic's Algorithm", dinicResult.getExecutionTimeMs(), 
                                            dinicResult.getMaxFlow(), dinicResult.getFlowMap());

        // Build response
        Map<String, Object> networkData = buildNetworkData(graph, edgeCapacities);
        Map<String, Object> fordResponse = buildAlgorithmResponse(fordResult);
        Map<String, Object> dinicResponse = buildAlgorithmResponse(dinicResult);

        return new MaxFlowComparisonResponse(
                String.valueOf(roundId),
                networkData,
                fordResponse,
                dinicResponse
        );
    }

    /**
     * Submit player answer for current round.
     */
    public SubmitTrafficAnswerResponse submitAnswer(SubmitTrafficAnswerRequest request, long roundId) {
        System.out.println("DEBUG: submitAnswer called - roundId=" + roundId + ", playerName=" + request.getPlayerName() + ", playerAnswer=" + request.getPlayerAnswer());
        
        // Get the correct answer from database
        Integer correctAnswer = trafficRepository.getCorrectMaxFlow(roundId);
        
        if (correctAnswer == null) {
            System.err.println("ERROR: correctAnswer is null for roundId: " + roundId);
            return new SubmitTrafficAnswerResponse(false, -1, "Round not found or not solved yet", -1);
        }

        // Get player answer (already an int from request)
        int playerAnswer = request.getPlayerAnswer();
        
        System.out.println("DEBUG: Comparing playerAnswer=" + playerAnswer + " with correctAnswer=" + correctAnswer);
        
        // Direct integer comparison
        boolean isCorrect = (playerAnswer == correctAnswer);
        
        System.out.println("DEBUG: Comparison result - isCorrect=" + isCorrect);
        
        // Save player answer with correct flag
        trafficRepository.savePlayerAnswer(roundId, request.getPlayerName(), playerAnswer, isCorrect);

        // Get algorithm execution time
        long executionTime = trafficRepository.getAlgorithmExecutionTime(roundId, "Ford-Fulkerson");

        String message = isCorrect ? "Correct! 🎉" : "Incorrect. The correct answer is " + correctAnswer;
        
        System.out.println("DEBUG: Returning response - isCorrect=" + isCorrect + ", correctAnswer=" + correctAnswer + ", message=" + message);
        
        return new SubmitTrafficAnswerResponse(isCorrect, correctAnswer, message, executionTime);
    }

    /**
     * Get leaderboard.
     */
    public TrafficLeaderboardResponse getLeaderboard(int limit) {
        List<TrafficLeaderboardEntry> entries = trafficRepository.getLeaderboard(limit);
        return new TrafficLeaderboardResponse(entries);
    }

    /**
     * Generate random traffic graph with random capacities.
     */
    private TrafficGraph generateRandomGraph() {
        TrafficGraph graph = new TrafficGraph();

        // Add all nodes
        for (String node : NODES) {
            graph.addNode(node);
        }

        // Add all edges with random capacities
        for (Map.Entry<String, String[]> entry : EDGES.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                int capacity = MIN_CAPACITY + random.nextInt(MAX_CAPACITY - MIN_CAPACITY + 1);
                graph.addEdge(from, to, capacity);
            }
        }

        return graph;
    }

    /**
     * Capture edge capacities from graph.
     */
    private Map<String, Integer> captureEdgeCapacities(TrafficGraph graph) {
        Map<String, Integer> capacities = new HashMap<>();
        for (TrafficEdge edge : graph.getAllEdges()) {
            String key = edge.getFrom() + "->" + edge.getTo();
            capacities.put(key, edge.getCapacity());
        }
        return capacities;
    }

    /**
     * Build network data for response.
     */
    private Map<String, Object> buildNetworkData(TrafficGraph graph, Map<String, Integer> capacities) {
        Map<String, Object> data = new HashMap<>();
        data.put("nodes", NODES);
        data.put("source", SOURCE);
        data.put("sink", SINK);
        data.put("edges", buildEdgeList(graph, capacities));
        return data;
    }

    /**
     * Build edge list for network visualization.
     */
    private List<Map<String, Object>> buildEdgeList(TrafficGraph graph, Map<String, Integer> capacities) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (TrafficEdge edge : graph.getAllEdges()) {
            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("from", edge.getFrom());
            edgeData.put("to", edge.getTo());
            edgeData.put("capacity", edge.getCapacity());
            edges.add(edgeData);
        }
        return edges;
    }

    /**
     * Build algorithm response.
     */
    private Map<String, Object> buildAlgorithmResponse(MaxFlowResult result) {
        Map<String, Object> response = new HashMap<>();
        response.put("maxFlow", result.getMaxFlow());
        response.put("executionTimeMs", result.getExecutionTimeMs());
        response.put("algorithmName", result.getAlgorithmName());
        return response;
    }
}
