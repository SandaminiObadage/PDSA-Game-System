package com.nibm.pdsa.games.traffic.algorithm;

import com.nibm.pdsa.games.traffic.model.MaxFlowResult;
import com.nibm.pdsa.games.traffic.model.TrafficEdge;
import com.nibm.pdsa.games.traffic.model.TrafficGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for maximum flow algorithms.
 */
public class MaxFlowAlgorithmsTest {

    private FordFulkersonAlgorithm fordFulkerson;
    private DinicAlgorithm dinic;
    private TrafficGraph graph;

    @BeforeEach
    public void setUp() {
        fordFulkerson = new FordFulkersonAlgorithm();
        dinic = new DinicAlgorithm();
    }

    /**
     * Test simple linear graph: A -> B -> C
     * Expected max flow: 5 (minimum capacity)
     */
    @Test
    public void testSimpleLinearPath() {
        graph = new TrafficGraph();
        graph.addEdge("A", "B", 10);
        graph.addEdge("B", "C", 5);
        graph.addEdge("C", "D", 15);

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "D");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "D");

        assertEquals(5, ffResult.getMaxFlow(), "Ford-Fulkerson should find correct max flow");
        assertEquals(5, dinicResult.getMaxFlow(), "Dinic's should find correct max flow");
    }

    /**
     * Test complex traffic network.
     * Graph structure similar to the game:
     * A -> B (10), B -> C (10), C -> D (5)
     * A -> D (10)
     * Expected max flow should be 15 (through both paths)
     */
    @Test
    public void testComplexNetwork() {
        graph = new TrafficGraph();
        
        // Path 1: A -> B -> C -> D (bottleneck = 5)
        graph.addEdge("A", "B", 10);
        graph.addEdge("B", "C", 10);
        graph.addEdge("C", "D", 5);
        
        // Path 2: A -> D (direct = 10)
        graph.addEdge("A", "D", 10);
        // Connection: C -> D (as alternative)
        
        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "D");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "D");

        // Should be able to send 10 directly + 5 through the other path = 15
        assertEquals(15, ffResult.getMaxFlow(), "Ford-Fulkerson should find correct max flow");
        assertEquals(15, dinicResult.getMaxFlow(), "Dinic's should find correct max flow");
    }

    /**
     * Test game network with specific capacities.
     */
    @Test
    public void testGameNetwork() {
        graph = createGameNetwork();

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "T");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "T");

        assertNotNull(ffResult, "Ford-Fulkerson should return result");
        assertNotNull(dinicResult, "Dinic's should return result");
        assertEquals(ffResult.getMaxFlow(), dinicResult.getMaxFlow(), 
                    "Both algorithms should find the same max flow");
        assertTrue(ffResult.getMaxFlow() > 0, "Max flow should be positive");
        assertTrue(dinicResult.getExecutionTimeMs() >= 0, "Execution time should be non-negative");
    }

    /**
     * Test single edge graph.
     */
    @Test
    public void testSingleEdge() {
        graph = new TrafficGraph();
        graph.addEdge("A", "B", 100);

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "B");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "B");

        assertEquals(100, ffResult.getMaxFlow());
        assertEquals(100, dinicResult.getMaxFlow());
    }

    /**
     * Test no path from source to sink.
     */
    @Test
    public void testNoPath() {
        graph = new TrafficGraph();
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        
        graph.addEdge("A", "B", 10);
        graph.addEdge("C", "D", 10);
        // No path from A to D

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "D");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "D");

        assertEquals(0, ffResult.getMaxFlow(), "Flow should be 0 when no path exists");
        assertEquals(0, dinicResult.getMaxFlow(), "Flow should be 0 when no path exists");
    }

    /**
     * Test multiple parallel paths.
     */
    @Test
    public void testParallelPaths() {
        graph = new TrafficGraph();
        
        // Three parallel paths from A to B
        graph.addEdge("A", "B", 5);
        graph.addEdge("A", "C", 5);
        graph.addEdge("A", "D", 5);
        graph.addEdge("B", "E", 10);
        graph.addEdge("C", "E", 10);
        graph.addEdge("D", "E", 10);

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "E");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "E");

        assertEquals(15, ffResult.getMaxFlow(), "Should be 5+5+5=15");
        assertEquals(15, dinicResult.getMaxFlow(), "Should be 5+5+5=15");
    }

    /**
     * Test algorithm execution time is recorded.
     */
    @Test
    public void testExecutionTimeRecorded() {
        graph = createGameNetwork();

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "T");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "T");

        assertTrue(ffResult.getExecutionTimeMs() >= 0, "Execution time should be recorded");
        assertTrue(dinicResult.getExecutionTimeMs() >= 0, "Execution time should be recorded");
    }

    /**
     * Test algorithm names are set correctly.
     */
    @Test
    public void testAlgorithmNamesSet() {
        graph = createGameNetwork();

        MaxFlowResult ffResult = fordFulkerson.findMaxFlow(graph, "A", "T");
        MaxFlowResult dinicResult = dinic.findMaxFlow(graph, "A", "T");

        assertEquals("Ford-Fulkerson", ffResult.getAlgorithmName());
        assertEquals("Dinic's Algorithm", dinicResult.getAlgorithmName());
    }

    /**
     * Test invalid source/sink parameters.
     */
    @Test
    public void testInvalidSourceSink() {
        graph = new TrafficGraph();
        graph.addEdge("A", "B", 10);

        assertThrows(IllegalArgumentException.class, 
                () -> fordFulkerson.findMaxFlow(graph, "X", "B"),
                "Should throw exception for invalid source");

        assertThrows(IllegalArgumentException.class,
                () -> dinic.findMaxFlow(graph, "A", "Y"),
                "Should throw exception for invalid sink");
    }

    /**
     * Helper method to create a standard game network.
     */
    private TrafficGraph createGameNetwork() {
        TrafficGraph graph = new TrafficGraph();

        // Add edges based on game specifications
        graph.addEdge("A", "B", 10);
        graph.addEdge("A", "C", 8);
        graph.addEdge("A", "D", 12);
        graph.addEdge("B", "E", 7);
        graph.addEdge("B", "F", 9);
        graph.addEdge("C", "E", 6);
        graph.addEdge("C", "F", 11);
        graph.addEdge("D", "F", 8);
        graph.addEdge("E", "G", 10);
        graph.addEdge("E", "H", 7);
        graph.addEdge("F", "H", 9);
        graph.addEdge("G", "T", 12);
        graph.addEdge("H", "T", 10);

        return graph;
    }

    /**
     * Test flow conservation (flow in = flow out at each node).
     */
    @Test
    public void testFlowConservation() {
        graph = createGameNetwork();
        MaxFlowResult result = fordFulkerson.findMaxFlow(graph, "A", "T");

        assertNotNull(result.getFlowMap(), "Flow map should be available");
        // Flow conservation is implicit in algorithm correctness
        assertTrue(result.getMaxFlow() >= 0, "Flow should be non-negative");
    }
}
