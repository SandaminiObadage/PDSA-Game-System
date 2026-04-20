package com.nibm.pdsa.games.sixteenqueens.service;

import com.nibm.pdsa.common.exception.BadRequestException;
import com.nibm.pdsa.games.sixteenqueens.algorithm.BitmaskBacktrackingSolver;
import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensLeaderboardResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.ResetRecognizedResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.RoundCloseResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SampleSolutionsResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SolveComparisonResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensHistoryResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensReportResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.sixteenqueens.model.QueensSolveResult;
import com.nibm.pdsa.games.sixteenqueens.repository.SixteenQueensRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SixteenQueensService {

    private static final String GAME_CODE = "SIXTEEN_QUEENS";
    private static final int FIXED_BOARD_SIZE = 16;

    private final BitmaskBacktrackingSolver solver = new BitmaskBacktrackingSolver();
    private final SixteenQueensRepository repository;

    // Tracks already-recognized solutions for current active game cycle.
    private final Set<String> recognizedSolutions = ConcurrentHashMap.newKeySet();

    public SixteenQueensService(SixteenQueensRepository repository) {
        this.repository = repository;
    }

    public SolveComparisonResponse runComparison(int boardSize, int threadCount, int sampleLimit, int persistSolutionLimit, String viewerRole) {
        if (boardSize != FIXED_BOARD_SIZE) {
            throw new BadRequestException("Board size is fixed to 16 for this game.");
        }

        String normalizedViewerRole = normalizeViewerRole(viewerRole);

        int effectiveCollectionLimit = Math.max(sampleLimit, persistSolutionLimit);
        QueensSolveResult sequential = solver.solveSequential(boardSize, effectiveCollectionLimit);
        QueensSolveResult parallel = solver.solveParallel(boardSize, threadCount, effectiveCollectionLimit);

        SolveComparisonResponse response = new SolveComparisonResponse();
        response.setBoardSize(boardSize);
        response.setSequentialSolutionCount(sequential.getSolutionCount());
        response.setParallelSolutionCount(parallel.getSolutionCount());
        response.setSequentialTimeMs(sequential.getElapsedMs());
        response.setParallelTimeMs(parallel.getElapsedMs());
        List<String> collectedSolutions = sequential.getSampleSolutions();
        List<String> sampleSolutions = collectedSolutions.stream().limit(sampleLimit).toList();
        response.setSampleSolutions(sampleSolutions);
        response.setSamplesVisible(isAdminRole(normalizedViewerRole));

        if (repository != null) {
            long gameTypeId = getGameTypeId();
            long roundNo = repository.getNextRoundNo(gameTypeId);
            String roundInputJson = "{\"boardSize\":" + boardSize + ",\"threadCount\":" + threadCount + "}";
            long gameRoundId = repository.createGameRound(gameTypeId, roundNo, roundInputJson, Long.toString(sequential.getSolutionCount()));

            repository.insertAlgorithmRun(
                gameRoundId,
                "SEQUENTIAL_BACKTRACKING",
                "BITMASK_DFS",
                sequential.getElapsedMs(),
                "{\"solutionCount\":" + sequential.getSolutionCount() + "}"
            );

            repository.insertAlgorithmRun(
                gameRoundId,
                "THREADED_SEARCH",
                "PARALLEL_BITMASK_DFS",
                parallel.getElapsedMs(),
                "{\"solutionCount\":" + parallel.getSolutionCount() + ",\"threadCount\":" + threadCount + "}"
            );

            int sequentialPersisted = persistKnownSolutions(gameTypeId, gameRoundId, collectedSolutions, persistSolutionLimit, "SEQUENTIAL_BACKTRACKING");
            int parallelPersisted = persistKnownSolutions(gameTypeId, gameRoundId, parallel.getSampleSolutions(), persistSolutionLimit, "THREADED_SEARCH");
            response.setSequentialPersistedSolutionCount(sequentialPersisted);
            response.setParallelPersistedSolutionCount(parallelPersisted);
            response.setPersistedSolutionCount(sequentialPersisted + parallelPersisted);

            boolean samplesVisible = canViewSamplesForRound(gameRoundId, normalizedViewerRole);
            response.setSamplesVisible(samplesVisible);
            response.setSampleSolutions(samplesVisible ? sampleSolutions : List.of());
            response.setGameRoundId(gameRoundId);
        } else if (!isAdminRole(normalizedViewerRole)) {
            response.setSampleSolutions(List.of());
            response.setSamplesVisible(false);
        }

        double speedup = parallel.getElapsedMs() == 0 ? 0.0 : (double) sequential.getElapsedMs() / parallel.getElapsedMs();
        response.setSpeedup(speedup);

        // If all solutions have been recognized in previous rounds and solver confirms count,
        // reset so future players can answer again.
        if (!recognizedSolutions.isEmpty() && recognizedSolutions.size() >= sequential.getSolutionCount()) {
            recognizedSolutions.clear();
        }

        return response;
    }

    public RoundCloseResponse closeRound(Long gameRoundId) {
        if (repository == null) {
            throw new BadRequestException("Database round close is not available in in-memory mode.");
        }

        long gameTypeId = getGameTypeId();
        Long effectiveRoundId = gameRoundId != null ? gameRoundId : repository.findLatestRoundId(gameTypeId);
        if (effectiveRoundId == null) {
            throw new BadRequestException("No game round exists yet. Run solve first.");
        }

        int updated = repository.closeRound(effectiveRoundId);
        boolean closed = repository.isRoundClosed(effectiveRoundId);
        RoundCloseResponse response = new RoundCloseResponse();
        response.setGameTypeId(gameTypeId);
        response.setGameCode(GAME_CODE);
        response.setGameRoundId(effectiveRoundId);
        response.setClosed(closed);
        if (updated > 0) {
            response.setMessage("Round " + effectiveRoundId + " is now closed. Samples are visible to players.");
        } else {
            response.setMessage("Round " + effectiveRoundId + " was already closed.");
        }
        return response;
    }

    public SampleSolutionsResponse getRoundSamples(Long gameRoundId, int limit, String viewerRole) {
        if (repository == null) {
            throw new BadRequestException("Database samples are not available in in-memory mode.");
        }

        if (limit <= 0) {
            throw new BadRequestException("Sample limit must be greater than 0.");
        }

        long gameTypeId = getGameTypeId();
        Long effectiveRoundId = gameRoundId != null ? gameRoundId : repository.findLatestRoundId(gameTypeId);
        if (effectiveRoundId == null) {
            throw new BadRequestException("No game round exists yet. Run solve first.");
        }

        String normalizedViewerRole = normalizeViewerRole(viewerRole);
        boolean visible = canViewSamplesForRound(effectiveRoundId, normalizedViewerRole);

        SampleSolutionsResponse response = new SampleSolutionsResponse();
        response.setGameRoundId(effectiveRoundId);
        response.setSamplesVisible(visible);

        if (!visible) {
            response.setSampleSolutions(List.of());
            response.setMessage("Samples are hidden for players while the round is active.");
            return response;
        }

        List<String> samples = repository.findKnownSolutionsForRound(gameTypeId, effectiveRoundId, limit);
        response.setSampleSolutions(samples);
        response.setMessage(samples.isEmpty() ? "No saved sample solutions found for this round." : "Sample solutions loaded.");
        return response;
    }

    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        int boardSize = request.getBoardSize();
        if (boardSize != FIXED_BOARD_SIZE) {
            throw new BadRequestException("Board size is fixed to 16 for this game.");
        }

        int[] positions;
        try {
            positions = solver.parseAnswer(request.getAnswer(), boardSize);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        boolean correct = solver.isValidSolution(boardSize, positions);
        String canonical = request.getAnswer().replaceAll("\\s+", "");

        SubmitAnswerResponse response = new SubmitAnswerResponse();
        response.setPlayerName(request.getPlayerName());
        response.setCorrect(correct);

        if (repository != null) {
            long gameTypeId = getGameTypeId();
            Long gameRoundId = request.getGameRoundId() != null
                    ? request.getGameRoundId()
                    : repository.findLatestRoundId(gameTypeId);

            if (gameRoundId == null) {
                throw new BadRequestException("No game round exists yet. Run solve first.");
            }

            long playerId = repository.ensurePlayerAndGetId(request.getPlayerName());
            repository.insertPlayerAnswer(gameRoundId, playerId, canonical, correct);

            if (!correct) {
                response.setAlreadyRecognized(false);
                response.setMessage("Incorrect answer. Try again.");
                return response;
            }

            String solutionHash = scopeSolutionHash(gameRoundId, canonical);

            if (repository.isActiveRecognized(gameTypeId, solutionHash)) {
                response.setAlreadyRecognized(true);
                response.setMessage("Correct, but this solution has already been recognized. Try a new one.");
                return response;
            }

            repository.upsertActiveRecognizedSolution(gameTypeId, solutionHash, canonical, playerId);

            Long expectedTotal = repository.getExpectedTotalSolutionsForRound(gameRoundId);
            if (expectedTotal != null && expectedTotal > 0) {
                long activeCount = repository.countActiveRecognizedForRound(gameTypeId, gameRoundId);
                if (activeCount >= expectedTotal) {
                    repository.clearActiveRecognizedForRound(gameTypeId, gameRoundId);
                }
            }

            response.setAlreadyRecognized(false);
            response.setMessage("Correct answer saved for player " + request.getPlayerName() + ".");
            return response;
        }

        if (!correct) {
            response.setAlreadyRecognized(false);
            response.setMessage("Incorrect answer. Try again.");
            return response;
        }

        if (recognizedSolutions.contains(canonical)) {
            response.setAlreadyRecognized(true);
            response.setMessage("Correct, but this solution has already been recognized. Try a new one.");
            return response;
        }

        recognizedSolutions.add(canonical);
        response.setAlreadyRecognized(false);
        response.setMessage("Correct answer saved for player " + request.getPlayerName() + ".");
        return response;
    }

    public SixteenQueensHistoryResponse getHistory(int limit) {
        if (repository == null) {
            throw new BadRequestException("Database history is not available in in-memory mode.");
        }

        long gameTypeId = getGameTypeId();
        SixteenQueensHistoryResponse response = new SixteenQueensHistoryResponse();
        response.setGameTypeId(gameTypeId);
        response.setGameCode(GAME_CODE);
        response.setRounds(repository.findRecentRoundHistory(gameTypeId, limit));
        response.setRecentAnswers(repository.findRecentAnswers(gameTypeId, limit));
        return response;
    }

    public SixteenQueensLeaderboardResponse getLeaderboard(int limit, Long gameRoundId) {
        if (repository == null) {
            throw new BadRequestException("Database leaderboard is not available in in-memory mode.");
        }

        long gameTypeId = getGameTypeId();
        Long effectiveRoundId = gameRoundId != null ? gameRoundId : repository.findLatestRoundId(gameTypeId);
        SixteenQueensLeaderboardResponse response = new SixteenQueensLeaderboardResponse();
        response.setGameTypeId(gameTypeId);
        response.setGameCode(GAME_CODE);
        response.setRoundId(effectiveRoundId);
        if (effectiveRoundId == null) {
            response.setLeaderboard(List.of());
            return response;
        }

        response.setLeaderboard(repository.findLeaderboard(gameTypeId, limit, effectiveRoundId));
        return response;
    }

    public ResetRecognizedResponse resetRecognizedSolutions(Long gameRoundId) {
        if (repository == null) {
            throw new BadRequestException("Database reset is not available in in-memory mode.");
        }

        long gameTypeId = getGameTypeId();
        Long effectiveRoundId = gameRoundId != null ? gameRoundId : repository.findLatestRoundId(gameTypeId);
        if (effectiveRoundId == null) {
            throw new BadRequestException("No game round exists yet. Run solve first.");
        }

        int cleared = repository.clearActiveRecognizedForRound(gameTypeId, effectiveRoundId);
        ResetRecognizedResponse response = new ResetRecognizedResponse();
        response.setGameTypeId(gameTypeId);
        response.setGameCode(GAME_CODE);
        response.setGameRoundId(effectiveRoundId);
        response.setClearedCount(cleared);
        response.setMessage("Recognized solutions reset for round " + effectiveRoundId + ".");
        return response;
    }

    public SixteenQueensReportResponse getReport() {
        if (repository == null) {
            throw new BadRequestException("Database report is not available in in-memory mode.");
        }

        long gameTypeId = getGameTypeId();
        SixteenQueensReportResponse response = repository.getReport(gameTypeId);
        response.setGameTypeId(gameTypeId);
        response.setGameCode(GAME_CODE);
        return response;
    }

    private long getGameTypeId() {
        Long id = repository.findGameTypeIdByCode(GAME_CODE);
        if (id == null) {
            throw new BadRequestException("Game type SIXTEEN_QUEENS is not initialized in the database.");
        }
        return id;
    }

    private int persistKnownSolutions(long gameTypeId, long gameRoundId, List<String> solutions, int persistSolutionLimit, String algorithmName) {
        if (persistSolutionLimit <= 0 || solutions.isEmpty()) {
            return 0;
        }

        int target = Math.min(persistSolutionLimit, solutions.size());
        int insertedAnswerRows = 0;
        for (int i = 0; i < target; i++) {
            String solution = solutions.get(i);
            String roundScopedHash = scopeSolutionHash(gameRoundId, solution);
            repository.insertKnownSolutionIfMissing(gameTypeId, roundScopedHash, solution);
            insertedAnswerRows += repository.insertAlgorithmSolutionAnswerIfMissing(gameRoundId, algorithmName, sha256(solution), solution);
        }
        return insertedAnswerRows;
    }

    private String scopeSolutionHash(long gameRoundId, String solution) {
        return gameRoundId + ":" + sha256(solution);
    }

    private boolean canViewSamplesForRound(long gameRoundId, String viewerRole) {
        return isAdminRole(viewerRole) || repository.isRoundClosed(gameRoundId);
    }

    private String normalizeViewerRole(String viewerRole) {
        if (viewerRole == null || viewerRole.isBlank()) {
            return "PLAYER";
        }
        return viewerRole.trim().toUpperCase();
    }

    private boolean isAdminRole(String viewerRole) {
        return "ADMIN".equals(viewerRole);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
