package com.nibm.pdsa.games.knightstour.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.pdsa.games.knightstour.dto.LeaderboardEntryResponse;
import com.nibm.pdsa.games.knightstour.dto.StartGameRequest;
import com.nibm.pdsa.games.knightstour.dto.StartGameResponse;
import com.nibm.pdsa.games.knightstour.dto.ValidateGameRequest;
import com.nibm.pdsa.games.knightstour.dto.ValidateGameResponse;
import com.nibm.pdsa.games.knightstour.entity.AlgorithmType;
import com.nibm.pdsa.games.knightstour.entity.OutcomeStatus;
import com.nibm.pdsa.games.knightstour.exception.BadRequestException;
import com.nibm.pdsa.games.knightstour.exception.ResourceNotFoundException;

@Service
public class KnightGameService {

    private static final String WIN_MESSAGE = "Congratulations! Valid full knight tour.";
    private static final String LOSE_MESSAGE = "No legal moves remain before covering all squares.";
    private static final String DRAW_MESSAGE = "Game ended as a draw.";
    private static final String GAME_CODE = "KNIGHTS_TOUR";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public KnightGameService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StartGameResponse startGame(StartGameRequest request) {
        validateBoardSize(request.getBoardSize());

        int boardSize = request.getBoardSize();
        int startRow = ThreadLocalRandom.current().nextInt(boardSize);
        int startCol = ThreadLocalRandom.current().nextInt(boardSize);
        AlgorithmType algorithmType = resolveAlgorithmType(request.getAlgorithmType());
        String startPosition = toPosition(startRow, startCol);
        AlgorithmRunMetrics runMetrics = measureSolverRuntime(boardSize, startPosition, algorithmType);

        Long roundId = persistRound(boardSize, startPosition, algorithmType);
        persistAlgorithmRun(roundId, algorithmType, boardSize, startPosition, request.getAlgorithmType(), runMetrics);

        StartGameResponse response = new StartGameResponse();
        response.setKnightId(roundId);
        response.setStartPosition(startPosition);
        response.setAlgorithmType(algorithmType.name());
        return response;
    }

    @Transactional
    public ValidateGameResponse validateGame(ValidateGameRequest request) {
        KnightRound round = getKnightRoundById(request.getKnightId());

        Long playerId = getOrCreatePlayer(request.getPlayerName().trim());

        OutcomeStatus status;
        String message;

        if (!round.startPosition().equals(request.getMoves().get(0))) {
            status = OutcomeStatus.LOSE;
            message = "First move must match the stored start position.";
            persistResult(playerId, round.id(), request.getMoves(), status);
            return buildValidationResponse(status, message);
        }

        try {
            EvaluationResult evaluationResult = evaluateMoves(request.getMoves(), round.boardSize());
            status = evaluationResult.status();
            message = status == OutcomeStatus.WIN ? WIN_MESSAGE : LOSE_MESSAGE;
        } catch (BadRequestException ex) {
            status = OutcomeStatus.LOSE;
            message = ex.getMessage();
            persistResult(playerId, round.id(), request.getMoves(), status);
            return buildValidationResponse(status, message);
        }

        OutcomeStatus finalStatus = applyOverride(status, request.getOutcomeOverride());
        String finalMessage = resolveMessage(finalStatus, request.getOutcomeMessage(), message);

        persistResult(playerId, round.id(), request.getMoves(), finalStatus);
        return buildValidationResponse(finalStatus, finalMessage);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        Long gameTypeId = resolveGameTypeId();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                SELECT p.player_name AS player_name,
                       pa.is_correct AS is_correct,
                       pa.answer_json AS answer_json
                FROM player_answers pa
                INNER JOIN players p ON p.id = pa.player_id
                INNER JOIN game_rounds gr ON gr.id = pa.game_round_id
                WHERE gr.game_type_id = ?
                """,
                gameTypeId
        );

        Map<String, Stats> statsByPlayer = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String playerName = String.valueOf(row.get("player_name"));
            Stats stats = statsByPlayer.computeIfAbsent(playerName, key -> new Stats());
            stats.mostMoves = Math.max(stats.mostMoves, parseMoveCountFromAnswer(String.valueOf(row.get("answer_json"))));
            if (isRowWin(row.get("is_correct"))) {
                stats.wins++;
                stats.hasWin = true;
            }
        }

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Stats> entry : statsByPlayer.entrySet()) {
            LeaderboardEntryResponse row = new LeaderboardEntryResponse();
            row.setPlayerName(entry.getKey());
            row.setWins(entry.getValue().wins);
            row.setMostMoves(entry.getValue().mostMoves);
            row.setHasWin(entry.getValue().hasWin);
            leaderboard.add(row);
        }

        leaderboard.sort(
                Comparator.comparingInt(LeaderboardEntryResponse::getMostMoves).reversed()
                        .thenComparing(Comparator.comparingLong(LeaderboardEntryResponse::getWins).reversed())
                        .thenComparing(LeaderboardEntryResponse::getPlayerName)
        );

        return leaderboard;
    }

    private EvaluationResult evaluateMoves(List<String> moves, int boardSize) {
        validateBoardSize(boardSize);

        Set<String> visited = new HashSet<>();
        Position previous = null;

        for (String move : moves) {
            Position current = parsePosition(move, boardSize);
            if (visited.contains(current.toKey())) {
                throw new BadRequestException("Duplicate position detected: " + current.toKey());
            }

            if (previous != null && !isKnightJump(previous, current)) {
                throw new BadRequestException("Invalid knight jump from " + previous.toKey() + " to " + current.toKey());
            }

            visited.add(current.toKey());
            previous = current;
        }

        if (visited.size() == boardSize * boardSize) {
            return new EvaluationResult(OutcomeStatus.WIN);
        }

        return new EvaluationResult(OutcomeStatus.LOSE);
    }

    private Long getOrCreatePlayer(String playerName) {
        Long existingId = findPlayerIdByName(playerName);
        if (existingId != null) {
            return existingId;
        }

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO players (player_name) VALUES (?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, playerName);
                return ps;
            }, keyHolder);

            Number generated = keyHolder.getKey();
            if (generated != null) {
                return generated.longValue();
            }
        } catch (DataIntegrityViolationException ignored) {
            // Player already exists due to concurrent insert.
        }

        Long playerId = findPlayerIdByName(playerName);
        if (playerId == null) {
            throw new IllegalStateException("Unable to resolve player id for " + playerName);
        }
        return playerId;
    }

    private void persistResult(Long playerId, Long gameRoundId, List<String> moves, OutcomeStatus status) {
        Map<String, Object> answer = Map.of(
                "moves", moves,
                "outcome", status.name(),
                "moveCount", moves.size()
        );

        String answerJson;
        try {
            answerJson = objectMapper.writeValueAsString(answer);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize answer payload", ex);
        }

        int isCorrect = status == OutcomeStatus.WIN ? 1 : 0;
        jdbcTemplate.update(
                "INSERT INTO player_answers (game_round_id, player_id, answer_json, is_correct) VALUES (?, ?, ?, ?)",
                gameRoundId,
                playerId,
                answerJson,
                isCorrect
        );
    }

    private Long persistRound(int boardSize, String startPosition, AlgorithmType algorithmType) {
        Long gameTypeId = resolveGameTypeId();
        Long nextRoundNo = jdbcTemplate.query(
                "SELECT COALESCE(MAX(round_no), 0) + 1 AS next_round FROM game_rounds WHERE game_type_id = ?",
                rs -> rs.next() ? rs.getLong("next_round") : 1L,
                gameTypeId
        );

        String inputJson;
        String expectedJson;
        try {
            inputJson = objectMapper.writeValueAsString(Map.of(
                    "boardSize", boardSize,
                    "startPosition", startPosition,
                    "algorithmType", algorithmType.name()
            ));
            expectedJson = objectMapper.writeValueAsString(Map.of("fullCoverageMoves", boardSize * boardSize));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize round payload", ex);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO game_rounds (game_type_id, round_no, round_input_json, expected_output_json) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, gameTypeId);
            ps.setLong(2, nextRoundNo == null ? 1L : nextRoundNo);
            ps.setString(3, inputJson);
            ps.setString(4, expectedJson);
            return ps;
        }, keyHolder);

        Number generated = keyHolder.getKey();
        if (generated == null) {
            throw new IllegalStateException("Unable to create Knight's Tour round");
        }
        return generated.longValue();
    }

    private void persistAlgorithmRun(
            Long roundId,
            AlgorithmType algorithmType,
            int boardSize,
            String startPosition,
            String requestedAlgorithm,
            AlgorithmRunMetrics runMetrics
    ) {
        ComplexityProfile complexity = resolveComplexity(algorithmType);
        String resultJson;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("selectedAlgorithm", algorithmType.name());
            payload.put("requestedAlgorithm", normalizeRequestedAlgorithm(requestedAlgorithm));
            payload.put("timeComplexity", complexity.timeComplexity());
            payload.put("spaceComplexity", complexity.spaceComplexity());
            payload.put("complexityNotes", complexity.notes());
            payload.put("boardSize", boardSize);
            payload.put("startPosition", startPosition);
            payload.put("solved", runMetrics.solved());
            payload.put("generatedMoveCount", runMetrics.generatedMoveCount());
            payload.put("solverTimeLimitMs", runMetrics.timeLimitMs());
            payload.put("failureReason", runMetrics.failureReason());
            resultJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize algorithm run payload", ex);
        }

        jdbcTemplate.update(
                "INSERT OR REPLACE INTO algorithm_runs (game_round_id, algorithm_name, algorithm_variant, execution_time_ms, result_json) VALUES (?, ?, ?, ?, ?)",
                roundId,
                algorithmType.name(),
                "KNIGHTS_TOUR_START",
                runMetrics.executionMs(),
                resultJson
        );
    }

    private AlgorithmRunMetrics measureSolverRuntime(int boardSize, String startPosition, AlgorithmType algorithmType) {
        Position start = parsePosition(startPosition, boardSize);
        long startedAt = System.nanoTime();
        long timeLimitMs = resolveSolverTimeLimitMs(boardSize, algorithmType);
        String failureReason = null;

        try {
            List<String> generatedMoves = algorithmType == AlgorithmType.WARNSDORFF
                    ? solveWithWarnsdorff(boardSize, start)
                    : solveWithBacktracking(boardSize, start, timeLimitMs);

            boolean solved = generatedMoves.size() == boardSize * boardSize;
            if (!solved) {
                failureReason = "Solver could not complete a full tour.";
            }

            return new AlgorithmRunMetrics(
                    toMillis(System.nanoTime() - startedAt),
                    solved,
                    generatedMoves.size(),
                    timeLimitMs,
                    failureReason
            );
        } catch (SolverTimeoutException ex) {
            return new AlgorithmRunMetrics(
                    toMillis(System.nanoTime() - startedAt),
                    false,
                    ex.partialMoveCount,
                    timeLimitMs,
                    "Timed out while searching for a full tour."
            );
        }
    }

    private List<String> solveWithWarnsdorff(int boardSize, Position start) {
        boolean[][] visited = new boolean[boardSize][boardSize];
        List<Position> path = new ArrayList<>(boardSize * boardSize);
        Position current = start;
        visited[current.row][current.col] = true;
        path.add(current);

        while (path.size() < boardSize * boardSize) {
            List<Position> candidates = legalMoves(current, boardSize, visited);
            if (candidates.isEmpty()) {
                break;
            }

            Position next = selectWarnsdorffMove(candidates, boardSize, visited);
            visited[next.row][next.col] = true;
            path.add(next);
            current = next;
        }

        List<String> result = new ArrayList<>(path.size());
        for (Position position : path) {
            result.add(position.toKey());
        }
        return result;
    }

    private List<String> solveWithBacktracking(int boardSize, Position start, long timeLimitMs) {
        boolean[][] visited = new boolean[boardSize][boardSize];
        List<Position> path = new ArrayList<>(boardSize * boardSize);
        visited[start.row][start.col] = true;
        path.add(start);

        long deadlineNanos = System.nanoTime() + (timeLimitMs * 1_000_000L);
        boolean solved = backtrack(boardSize, start, visited, path, deadlineNanos);

        if (!solved) {
            if (System.nanoTime() >= deadlineNanos) {
                throw new SolverTimeoutException(path.size());
            }
        }

        List<String> result = new ArrayList<>(path.size());
        for (Position position : path) {
            result.add(position.toKey());
        }
        return result;
    }

    private boolean backtrack(int boardSize, Position current, boolean[][] visited, List<Position> path, long deadlineNanos) {
        if (path.size() == boardSize * boardSize) {
            return true;
        }

        if (System.nanoTime() >= deadlineNanos) {
            return false;
        }

        List<Position> candidates = legalMoves(current, boardSize, visited);
        candidates.sort(Comparator
                .comparingInt((Position candidate) -> legalMoves(candidate, boardSize, visited).size())
                .thenComparingInt(candidate -> candidate.row)
                .thenComparingInt(candidate -> candidate.col));

        for (Position next : candidates) {
            visited[next.row][next.col] = true;
            path.add(next);

            if (backtrack(boardSize, next, visited, path, deadlineNanos)) {
                return true;
            }

            visited[next.row][next.col] = false;
            path.remove(path.size() - 1);

            if (System.nanoTime() >= deadlineNanos) {
                return false;
            }
        }

        return false;
    }

    private Position selectWarnsdorffMove(List<Position> candidates, int boardSize, boolean[][] visited) {
        Position selected = null;
        int bestDegree = Integer.MAX_VALUE;

        for (Position candidate : candidates) {
            int onwardDegree = legalMoves(candidate, boardSize, visited).size();
            if (onwardDegree < bestDegree) {
                bestDegree = onwardDegree;
                selected = candidate;
            }
        }

        return selected;
    }

    private List<Position> legalMoves(Position from, int boardSize, boolean[][] visited) {
        int[][] deltas = {
                {1, 2},
                {2, 1},
                {2, -1},
                {1, -2},
                {-1, -2},
                {-2, -1},
                {-2, 1},
                {-1, 2}
        };

        List<Position> moves = new ArrayList<>(8);
        for (int[] delta : deltas) {
            int nextRow = from.row + delta[0];
            int nextCol = from.col + delta[1];
            if (nextRow >= 0 && nextCol >= 0 && nextRow < boardSize && nextCol < boardSize && !visited[nextRow][nextCol]) {
                moves.add(new Position(nextRow, nextCol));
            }
        }
        return moves;
    }

    private long resolveSolverTimeLimitMs(int boardSize, AlgorithmType algorithmType) {
        if (algorithmType == AlgorithmType.BACKTRACKING) {
            return boardSize == 8 ? 1200L : 2500L;
        }
        return boardSize == 8 ? 400L : 700L;
    }

    private Long resolveGameTypeId() {
        Long gameTypeId = jdbcTemplate.query(
                "SELECT id FROM game_types WHERE code = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                GAME_CODE
        );
        if (gameTypeId == null) {
            throw new IllegalStateException("Missing game type configuration for " + GAME_CODE);
        }
        return gameTypeId;
    }

    private KnightRound getKnightRoundById(Long roundId) {
        Long gameTypeId = resolveGameTypeId();
        String inputJson = jdbcTemplate.query(
                "SELECT round_input_json FROM game_rounds WHERE id = ? AND game_type_id = ?",
                rs -> rs.next() ? rs.getString("round_input_json") : null,
                roundId,
                gameTypeId
        );

        if (inputJson == null) {
            throw new ResourceNotFoundException("Knight game not found for id " + roundId);
        }

        try {
            JsonNode node = objectMapper.readTree(inputJson);
            int boardSize = node.path("boardSize").asInt(0);
            String startPosition = node.path("startPosition").asText(null);
            if (boardSize == 0 || startPosition == null || startPosition.isBlank()) {
                throw new IllegalStateException("Knight round payload is incomplete for id " + roundId);
            }
            return new KnightRound(roundId, boardSize, startPosition);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse Knight round payload", ex);
        }
    }

    private Long findPlayerIdByName(String playerName) {
        return jdbcTemplate.query(
                "SELECT id FROM players WHERE player_name = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                playerName
        );
    }

    private int parseMoveCountFromAnswer(String answerJson) {
        try {
            JsonNode node = objectMapper.readTree(answerJson);
            if (node.isArray()) {
                return node.size();
            }
            JsonNode movesNode = node.path("moves");
            return movesNode.isArray() ? movesNode.size() : 0;
        } catch (JsonProcessingException ex) {
            return 0;
        }
    }

    private boolean isRowWin(Object value) {
        if (value instanceof Number number) {
            return number.intValue() == 1;
        }
        return "1".equals(String.valueOf(value));
    }

    private ComplexityProfile resolveComplexity(AlgorithmType algorithmType) {
        if (algorithmType == AlgorithmType.WARNSDORFF) {
            return new ComplexityProfile(
                    "O(N^2)",
                    "O(N^2)",
                    "Heuristic approach that typically visits each square once with local degree checks."
            );
        }

        return new ComplexityProfile(
                "O(8^(N^2)) worst-case",
                "O(N^2)",
                "Backtracking explores branching knight moves and can be exponential in worst-case search."
        );
    }

    private String normalizeRequestedAlgorithm(String requestedAlgorithm) {
        if (requestedAlgorithm == null || requestedAlgorithm.isBlank()) {
            return "RANDOM";
        }
        return requestedAlgorithm.trim().toUpperCase(Locale.ROOT);
    }

    private double toMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private OutcomeStatus applyOverride(OutcomeStatus current, String overrideRaw) {
        if (overrideRaw == null || overrideRaw.isBlank()) {
            return current;
        }

        OutcomeStatus override = parseOutcome(overrideRaw);
        if (override == OutcomeStatus.DRAW) {
            return OutcomeStatus.DRAW;
        }

        if (override == OutcomeStatus.LOSE && current == OutcomeStatus.DRAW) {
            return OutcomeStatus.LOSE;
        }

        return current;
    }

    private String resolveMessage(OutcomeStatus status, String overrideMessage, String fallbackMessage) {
        if (overrideMessage != null && !overrideMessage.isBlank()) {
            return overrideMessage;
        }

        if (status == OutcomeStatus.WIN) {
            return WIN_MESSAGE;
        }

        if (status == OutcomeStatus.DRAW) {
            return DRAW_MESSAGE;
        }

        return fallbackMessage == null || fallbackMessage.isBlank() ? LOSE_MESSAGE : fallbackMessage;
    }

    private ValidateGameResponse buildValidationResponse(OutcomeStatus status, String message) {
        ValidateGameResponse response = new ValidateGameResponse();
        response.setStatus(status.name());
        response.setMessage(message);
        return response;
    }

    private void validateBoardSize(Integer boardSize) {
        if (boardSize == null || (boardSize != 8 && boardSize != 16)) {
            throw new BadRequestException("Board size must be exactly 8 or 16.");
        }
    }

    private AlgorithmType resolveAlgorithmType(String input) {
        if (input == null || input.isBlank() || "RANDOM".equalsIgnoreCase(input.trim())) {
            return ThreadLocalRandom.current().nextBoolean() ? AlgorithmType.BACKTRACKING : AlgorithmType.WARNSDORFF;
        }

        String normalized = input.trim().toUpperCase(Locale.ROOT);
        if ("BACKTRACKING".equals(normalized)) {
            return AlgorithmType.BACKTRACKING;
        }
        if ("WARNSDORFF".equals(normalized)) {
            return AlgorithmType.WARNSDORFF;
        }

        throw new BadRequestException("Invalid algorithmType. Allowed values: RANDOM, WARNSDORFF, BACKTRACKING.");
    }

    private OutcomeStatus parseOutcome(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return OutcomeStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid outcomeOverride. Allowed values: WIN, LOSE, DRAW.");
        }
    }

    private Position parsePosition(String move, int boardSize) {
        if (move == null || move.isBlank()) {
            throw new BadRequestException("Invalid move format: blank move value.");
        }

        String[] parts = move.split(",");
        if (parts.length != 2) {
            throw new BadRequestException("Invalid move format: " + move + ". Expected row,col.");
        }

        int row;
        int col;
        try {
            row = Integer.parseInt(parts[0].trim());
            col = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid move format: " + move + ". Expected row,col.");
        }

        if (row < 0 || col < 0 || row >= boardSize || col >= boardSize) {
            throw new BadRequestException("Move out of bounds: " + move);
        }

        return new Position(row, col);
    }

    private boolean isKnightJump(Position from, Position to) {
        int rowDelta = Math.abs(from.row - to.row);
        int colDelta = Math.abs(from.col - to.col);
        return (rowDelta == 1 && colDelta == 2) || (rowDelta == 2 && colDelta == 1);
    }

    private String toPosition(int row, int col) {
        return row + "," + col;
    }

    private record EvaluationResult(OutcomeStatus status) {
    }

    private record KnightRound(Long id, int boardSize, String startPosition) {
    }

    private record ComplexityProfile(String timeComplexity, String spaceComplexity, String notes) {
    }

        private record AlgorithmRunMetrics(
            double executionMs,
            boolean solved,
            int generatedMoveCount,
            long timeLimitMs,
            String failureReason
        ) {
        }

    private static final class Stats {
        private long wins;
        private int mostMoves;
        private boolean hasWin;
    }

    private static final class Position {
        private final int row;
        private final int col;

        private Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        private String toKey() {
            return row + "," + col;
        }
    }

    private static final class SolverTimeoutException extends RuntimeException {
        private final int partialMoveCount;

        private SolverTimeoutException(int partialMoveCount) {
            this.partialMoveCount = partialMoveCount;
        }
    }
}
