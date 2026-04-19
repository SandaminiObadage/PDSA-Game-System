package com.nibm.pdsa.games.sixteenqueens.service;

import com.nibm.pdsa.common.exception.BadRequestException;
import com.nibm.pdsa.games.sixteenqueens.algorithm.BitmaskBacktrackingSolver;
import com.nibm.pdsa.games.sixteenqueens.dto.SolveComparisonResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SixteenQueensHistoryResponse;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.sixteenqueens.model.QueensSolveResult;
import com.nibm.pdsa.games.sixteenqueens.repository.SixteenQueensRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SixteenQueensService {

    private static final String GAME_CODE = "SIXTEEN_QUEENS";

    private final BitmaskBacktrackingSolver solver = new BitmaskBacktrackingSolver();
    private final SixteenQueensRepository repository;

    // Tracks already-recognized solutions for current active game cycle.
    private final Set<String> recognizedSolutions = ConcurrentHashMap.newKeySet();

    public SixteenQueensService(SixteenQueensRepository repository) {
        this.repository = repository;
    }

    public SolveComparisonResponse runComparison(int boardSize, int threadCount, int sampleLimit) {
        QueensSolveResult sequential = solver.solveSequential(boardSize, sampleLimit);
        QueensSolveResult parallel = solver.solveParallel(boardSize, threadCount, sampleLimit);

        SolveComparisonResponse response = new SolveComparisonResponse();
        response.setBoardSize(boardSize);
        response.setSequentialSolutionCount(sequential.getSolutionCount());
        response.setParallelSolutionCount(parallel.getSolutionCount());
        response.setSequentialTimeMs(sequential.getElapsedMs());
        response.setParallelTimeMs(parallel.getElapsedMs());
        response.setSampleSolutions(sequential.getSampleSolutions());

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

            response.setGameRoundId(gameRoundId);
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

    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        int boardSize = request.getBoardSize();
        int[] positions;
        try {
            positions = solver.parseAnswer(request.getAnswer(), boardSize);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        boolean correct = solver.isValidSolution(boardSize, positions);
        String canonical = request.getAnswer().replaceAll("\\s+", "");
        String solutionHash = sha256(canonical);

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

            if (repository.isActiveRecognized(gameTypeId, solutionHash)) {
                response.setAlreadyRecognized(true);
                response.setMessage("Correct, but this solution has already been recognized. Try a new one.");
                return response;
            }

            repository.upsertActiveRecognizedSolution(gameTypeId, solutionHash, canonical, playerId);

            Long expectedTotal = repository.getExpectedTotalSolutionsForRound(gameRoundId);
            if (expectedTotal != null && expectedTotal > 0) {
                long activeCount = repository.countActiveRecognized(gameTypeId);
                if (activeCount >= expectedTotal) {
                    repository.clearActiveRecognized(gameTypeId);
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

    private long getGameTypeId() {
        Long id = repository.findGameTypeIdByCode(GAME_CODE);
        if (id == null) {
            throw new BadRequestException("Game type SIXTEEN_QUEENS is not initialized in the database.");
        }
        return id;
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
