package com.nibm.pdsa.games.snakeladder.controller;

import com.nibm.pdsa.games.snakeladder.dto.SolveComparisonResponse;
import com.nibm.pdsa.games.snakeladder.dto.SnakeLadderLeaderboardResponse;
import com.nibm.pdsa.games.snakeladder.dto.SolveRequest;
import com.nibm.pdsa.games.snakeladder.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.snakeladder.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.snakeladder.service.SnakeLadderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/snake-ladder")
public class SnakeLadderController {

    private final SnakeLadderService snakeLadderService;

    public SnakeLadderController(SnakeLadderService snakeLadderService) {
        this.snakeLadderService = snakeLadderService;
    }

    @PostMapping("/solve")
    public SolveComparisonResponse solve(@Valid @RequestBody SolveRequest request) {
        return snakeLadderService.runComparison(request.getBoardSize());
    }

    @PostMapping("/submit")
    public SubmitAnswerResponse submit(@Valid @RequestBody SubmitAnswerRequest request) {
        return snakeLadderService.submitAnswer(request);
    }

    @GetMapping("/leaderboard")
    public SnakeLadderLeaderboardResponse leaderboard(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long roundId) {
        return snakeLadderService.getLeaderboard(limit, roundId);
    }
}