package com.nibm.pdsa.games.minimumcost.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nibm.pdsa.games.minimumcost.dto.MinimumCostHistoryResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostLeaderboardResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostRoundResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostSubmissionRequest;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostSubmissionResponse;
import com.nibm.pdsa.games.minimumcost.service.MinimumCostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games/minimum-cost")
public class MinimumCostController {

    private final MinimumCostService service;

    public MinimumCostController(MinimumCostService service) {
        this.service = service;
    }

    @PostMapping("/game/start")
    public MinimumCostRoundResponse startRound() {
        return service.startRound();
    }

    @GetMapping("/game/{id}")
    public MinimumCostRoundResponse getRound(@PathVariable("id") long roundId) {
        return service.getRound(roundId);
    }

    @PostMapping("/submit")
    public MinimumCostSubmissionResponse submit(@Valid @RequestBody MinimumCostSubmissionRequest request) {
        return service.submit(request);
    }

    @GetMapping("/leaderboard")
    public MinimumCostLeaderboardResponse leaderboard(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long roundId
    ) {
        return service.getLeaderboard(limit, roundId);
    }

    @GetMapping("/history")
    public MinimumCostHistoryResponse history(@RequestParam(defaultValue = "10") int limit) {
        return service.getHistory(limit);
    }

    @GetMapping("/report")
    public MinimumCostHistoryResponse report() {
        return service.getReport();
    }
}