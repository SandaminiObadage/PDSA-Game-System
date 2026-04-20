package com.nibm.pdsa.games.minimumcost.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.pdsa.common.exception.BadRequestException;
import com.nibm.pdsa.games.minimumcost.algorithm.GreedyLocalOptimizationSolver;
import com.nibm.pdsa.games.minimumcost.algorithm.HungarianSolver;
import com.nibm.pdsa.games.minimumcost.dto.AlgorithmRunResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostHistoryResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostHistoryRound;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostHistorySubmission;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostLeaderboardEntry;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostLeaderboardResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostRoundResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostSubmissionRequest;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostSubmissionResponse;
import com.nibm.pdsa.games.minimumcost.model.AssignmentResult;
import com.nibm.pdsa.games.minimumcost.repository.MinimumCostRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MinimumCostService {

    private static final int MIN_N = 50;
    private static final int MAX_N = 100;
    private static final int MIN_COST = 20;
    private static final int MAX_COST = 200;

    private final MinimumCostRepository repository;
    private final HungarianSolver hungarianSolver = new HungarianSolver();
    private final GreedyLocalOptimizationSolver alternativeSolver = new GreedyLocalOptimizationSolver();
    private final ObjectMapper objectMapper;

    public MinimumCostService(MinimumCostRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public MinimumCostRoundResponse startRound() {
        int n = ThreadLocalRandom.current().nextInt(MIN_N, MAX_N + 1);
        int[][] matrix = generateMatrix(n);

        CompletableFuture<AssignmentResult> hungarianFuture = CompletableFuture.supplyAsync(() -> hungarianSolver.solve(matrix));
        CompletableFuture<AssignmentResult> alternativeFuture = CompletableFuture.supplyAsync(() -> alternativeSolver.solve(matrix));

        AssignmentResult hungarian = hungarianFuture.join();
        AssignmentResult alternative = alternativeFuture.join();

        long roundNo = repository.getNextRoundNo();
        long roundId = repository.createRound(roundNo, n);
        repository.saveCostMatrix(roundId, toJson(matrix));
        repository.saveAlgorithmResult(roundId, hungarian.algorithmName(), hungarian.algorithmVariant(), hungarian.totalCost(), hungarian.executionTimeMs(), toJson(hungarian.assignment()));
        repository.saveAlgorithmResult(roundId, alternative.algorithmName(), alternative.algorithmVariant(), alternative.totalCost(), alternative.executionTimeMs(), toJson(alternative.assignment()));

        return new MinimumCostRoundResponse(
                roundId,
                roundNo,
                n,
                matrix,
                nowUtc(),
                toAlgorithmRunResponse(hungarian),
                toAlgorithmRunResponse(alternative)
        );
    }

    public MinimumCostRoundResponse getRound(long roundId) {
        MinimumCostRepository.RoundRow roundRow = requireRound(roundId);
        int[][] matrix = fromJson(roundRow.matrixJson(), new TypeReference<int[][]>() {});
        List<MinimumCostRepository.AlgorithmResultRow> resultRows = repository.findAlgorithmResults(roundId);

        AlgorithmRunResponse hungarian = null;
        AlgorithmRunResponse alternative = null;
        for (MinimumCostRepository.AlgorithmResultRow row : resultRows) {
            AlgorithmRunResponse response = new AlgorithmRunResponse(
                    row.algorithmName(),
                    row.algorithmVariant(),
                    row.totalCost(),
                    row.executionTimeMs(),
                        fromJson(row.assignmentJson(), new TypeReference<int[]>() {})
            );
            if ("HUNGARIAN".equals(row.algorithmName())) {
                hungarian = response;
            } else if ("GREEDY_LOCAL_OPTIMIZATION".equals(row.algorithmName())) {
                alternative = response;
            }
        }

        return new MinimumCostRoundResponse(roundRow.roundId(), roundRow.roundNo(), roundRow.n(), matrix, roundRow.createdAt(), hungarian, alternative);
    }

    public MinimumCostSubmissionResponse submit(MinimumCostSubmissionRequest request) {
        Long requestedRoundId = request.roundId();
        Long roundId = requestedRoundId != null ? requestedRoundId : resolveLatestRoundId();
        MinimumCostRepository.RoundRow roundRow = requireRound(roundId);
        int[][] matrix = fromJson(roundRow.matrixJson(), new TypeReference<int[][]>() {});
        Integer optimalCost = repository.findOptimalCost(roundId);
        if (optimalCost == null) {
            throw new BadRequestException("No Hungarian result exists for this round.");
        }

        List<Integer> assignment = request.assignment() == null ? List.of() : normalizeAssignment(request.assignment(), matrix.length);
        int resolvedCost = request.submittedCost();
        if (!assignment.isEmpty()) {
            int calculatedCost = calculateAssignmentCost(matrix, assignment);
            if (calculatedCost != resolvedCost) {
                throw new BadRequestException("Submitted cost does not match the provided assignment.");
            }
        }

        String assignmentJson = toJson(assignment);
        String submissionHash = sha256(roundId + ":" + request.playerName().trim().toLowerCase() + ":" + resolvedCost + ":" + assignmentJson);
        if (repository.submissionExists(roundId, request.playerName().trim(), submissionHash)) {
            return new MinimumCostSubmissionResponse(roundId, request.playerName().trim(), resolvedCost, optimalCost, resolvedCost == optimalCost, true, "Duplicate submission ignored.");
        }

        boolean correct = resolvedCost == optimalCost;
        repository.insertSubmission(request.playerName().trim(), roundId, resolvedCost, assignmentJson, correct, submissionHash);
        String message = correct ? "Optimal submission saved." : "Submission saved, but the cost is not optimal.";
        return new MinimumCostSubmissionResponse(roundId, request.playerName().trim(), resolvedCost, optimalCost, correct, false, message);
    }

    public MinimumCostLeaderboardResponse getLeaderboard(Integer limit, Long roundId) {
        Long effectiveRoundId = roundId != null ? roundId : repository.findLatestRoundId();
        return new MinimumCostLeaderboardResponse(
                effectiveRoundId,
                repository.findLeaderboard(limit == null ? 10 : limit, effectiveRoundId == null ? null : effectiveRoundId)
                        .stream()
                        .map(row -> new MinimumCostLeaderboardEntry(row.playerName(), row.bestSubmittedCost(), row.optimalSubmissions(), row.totalSubmissions(), row.lastSubmittedAt()))
                        .toList()
        );
    }

    public MinimumCostHistoryResponse getHistory(int limit) {
        MinimumCostRepository.SummaryRow summary = repository.findSummary();
        List<MinimumCostHistoryRound> rounds = repository.findRecentRounds(limit).stream()
                .map(row -> new MinimumCostHistoryRound(
                        row.roundId(),
                        row.roundNo(),
                        row.n(),
                        row.hungarianCost(),
                        row.hungarianTimeMs(),
                        row.alternativeCost(),
                        row.alternativeTimeMs(),
                        row.speedup(),
                        row.createdAt()))
                .toList();

        List<MinimumCostHistorySubmission> submissions = repository.findRecentSubmissions(limit).stream()
                .map(row -> new MinimumCostHistorySubmission(
                        row.submissionId(),
                        row.roundId(),
                        row.playerName(),
                        row.submittedCost(),
                        row.optimal(),
                        row.createdAt()))
                .toList();

        return new MinimumCostHistoryResponse(
                rounds,
                submissions,
                summary.totalRounds(),
                summary.totalSubmissions(),
                summary.optimalSubmissions(),
                summary.averageHungarianTimeMs(),
                summary.averageAlternativeTimeMs(),
                summary.averageAlternativeTimeMs() == 0.0 ? 0.0 : summary.averageHungarianTimeMs() / summary.averageAlternativeTimeMs(),
                summary.bestHungarianTimeMs(),
                summary.bestAlternativeTimeMs()
        );
    }

    public MinimumCostHistoryResponse getReport() {
        return getHistory(10);
    }

    private AlgorithmRunResponse toAlgorithmRunResponse(AssignmentResult result) {
        return new AlgorithmRunResponse(result.algorithmName(), result.algorithmVariant(), result.totalCost(), result.executionTimeMs(), result.assignment());
    }

    private MinimumCostRepository.RoundRow requireRound(Long roundId) {
        if (roundId == null) {
            throw new BadRequestException("Round not found.");
        }

        MinimumCostRepository.RoundRow roundRow = repository.findRound(roundId);
        if (roundRow == null) {
            throw new BadRequestException("Round not found.");
        }
        return roundRow;
    }

    private Long resolveLatestRoundId() {
        Long roundId = repository.findLatestRoundId();
        if (roundId == null) {
            throw new BadRequestException("No round exists yet. Start a round first.");
        }
        return roundId;
    }

    private int[][] generateMatrix(int n) {
        int[][] matrix = new int[n][n];
        for (int row = 0; row < n; row += 1) {
            for (int col = 0; col < n; col += 1) {
                matrix[row][col] = ThreadLocalRandom.current().nextInt(MIN_COST, MAX_COST + 1);
            }
        }
        return matrix;
    }

    private List<Integer> normalizeAssignment(List<Integer> assignment, int expectedSize) {
        if (assignment.size() != expectedSize) {
            throw new BadRequestException("Assignment size must match N.");
        }

        boolean[] seen = new boolean[expectedSize];
        for (Integer task : assignment) {
            if (task == null || task < 0 || task >= expectedSize) {
                throw new BadRequestException("Assignment contains an invalid task index.");
            }
            if (seen[task]) {
                throw new BadRequestException("Assignment must use each task exactly once.");
            }
            seen[task] = true;
        }

        return List.copyOf(assignment);
    }

    private int calculateAssignmentCost(int[][] matrix, List<Integer> assignment) {
        int totalCost = 0;
        for (int employee = 0; employee < assignment.size(); employee += 1) {
            totalCost += matrix[employee][assignment.get(employee)];
        }
        return totalCost;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize JSON", ex);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse JSON", ex);
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String nowUtc() {
        return java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString();
    }
}