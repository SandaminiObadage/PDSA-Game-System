package com.nibm.pdsa.games.traffic.controller;

import com.nibm.pdsa.games.traffic.dto.*;
import com.nibm.pdsa.games.traffic.service.TrafficService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Traffic Simulation Game API endpoints.
 */
@RestController
@RequestMapping("/api/games/traffic-simulation")
public class TrafficController {

    private final TrafficService trafficService;
    private long currentRoundId = -1;

    public TrafficController(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    /**
     * Generate a new game round.
     */
    @PostMapping("/new-round")
    public MaxFlowComparisonResponse generateNewRound() {
        MaxFlowComparisonResponse response = trafficService.generateNewRound();
        // Extract round ID from response (we'll need to modify the response to include it)
        // For now, we'll store it in the session
        return response;
    }

    /**
     * Submit player answer.
     */
    @PostMapping("/submit")
    public SubmitTrafficAnswerResponse submitAnswer(@Valid @RequestBody SubmitTrafficAnswerRequest request,
                                                    @RequestParam long roundId) {
        return trafficService.submitAnswer(request, roundId);
    }

    /**
     * Get leaderboard.
     */
    @GetMapping("/leaderboard")
    public TrafficLeaderboardResponse getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return trafficService.getLeaderboard(limit);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "game", "Traffic Simulation");
    }
}
