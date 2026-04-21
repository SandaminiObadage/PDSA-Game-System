package com.nibm.pdsa.games.knightstour.controller;

import com.nibm.pdsa.games.knightstour.dto.LeaderboardEntryResponse;
import com.nibm.pdsa.games.knightstour.dto.StartGameRequest;
import com.nibm.pdsa.games.knightstour.dto.StartGameResponse;
import com.nibm.pdsa.games.knightstour.dto.ValidateGameRequest;
import com.nibm.pdsa.games.knightstour.dto.ValidateGameResponse;
import com.nibm.pdsa.games.knightstour.service.KnightGameService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game")
public class KnightGameController {

    private final KnightGameService knightGameService;

    public KnightGameController(KnightGameService knightGameService) {
        this.knightGameService = knightGameService;
    }

    @PostMapping("/start")
    public StartGameResponse startGame(@Valid @RequestBody StartGameRequest request) {
        return knightGameService.startGame(request);
    }

    @PostMapping("/validate")
    public ValidateGameResponse validate(@Valid @RequestBody ValidateGameRequest request) {
        return knightGameService.validateGame(request);
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryResponse> leaderboard() {
        return knightGameService.getLeaderboard();
    }
}
