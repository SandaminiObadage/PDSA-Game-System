package com.nibm.pdsa.games.sixteenqueens.controller;

import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensLeaderboardResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SolveComparisonResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensHistoryResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensReportResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SolveRequest;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.sixteenqueens.service.SixteenQueensService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sixteen-queens")
public class SixteenQueensController {

    private final SixteenQueensService sixteenQueensService;

    public SixteenQueensController(SixteenQueensService sixteenQueensService) {
        this.sixteenQueensService = sixteenQueensService;
    }

    @PostMapping("/solve")
    public SolveComparisonResponse solve(@Valid @RequestBody SolveRequest request) {
        return sixteenQueensService.runComparison(
                request.getBoardSize(),
                request.getThreadCount(),
                request.getSolutionSampleLimit(),
                request.getPersistSolutionLimit()
        );
    }

    @PostMapping("/submit")
    public SubmitAnswerResponse submit(@Valid @RequestBody SubmitAnswerRequest request) {
        return sixteenQueensService.submitAnswer(request);
    }

    @GetMapping("/history")
    public SixteenQueensHistoryResponse history(@RequestParam(defaultValue = "10") int limit) {
        return sixteenQueensService.getHistory(limit);
    }

    @GetMapping("/leaderboard")
    public SixteenQueensLeaderboardResponse leaderboard(@RequestParam(defaultValue = "10") int limit) {
        return sixteenQueensService.getLeaderboard(limit);
    }

    @GetMapping("/report")
    public SixteenQueensReportResponse report() {
        return sixteenQueensService.getReport();
    }
}
