package com.nibm.pdsa.games.sixteenqueens.controller;

import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensLeaderboardResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.ResetRecognizedResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.RoundCloseResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SampleSolutionsResponse;
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
                request.getPersistSolutionLimit(),
                request.getViewerRole()
        );
    }

    @PostMapping("/close-round")
    public RoundCloseResponse closeRound(@RequestParam(required = false) Long roundId) {
        return sixteenQueensService.closeRound(roundId);
    }

    @GetMapping("/samples")
    public SampleSolutionsResponse samples(
            @RequestParam(required = false) Long roundId,
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "PLAYER") String viewerRole
    ) {
        return sixteenQueensService.getRoundSamples(roundId, limit, viewerRole);
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
    public SixteenQueensLeaderboardResponse leaderboard(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long roundId,
            @RequestParam(defaultValue = "CURRENT") String scope
    ) {
        return sixteenQueensService.getLeaderboard(limit, roundId, scope);
    }

    @PostMapping("/reset-recognized")
    public ResetRecognizedResponse resetRecognized(@RequestParam(required = false) Long roundId) {
        return sixteenQueensService.resetRecognizedSolutions(roundId);
    }

    @GetMapping("/report")
    public SixteenQueensReportResponse report() {
        return sixteenQueensService.getReport();
    }
}
