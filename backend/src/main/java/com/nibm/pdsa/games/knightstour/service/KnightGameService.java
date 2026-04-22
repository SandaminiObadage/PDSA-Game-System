package com.nibm.pdsa.games.knightstour.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class KnightGameService {

    private static final String WIN_MESSAGE = "Congratulations! Valid full knight tour.";
    private static final String LOSE_MESSAGE = "No legal moves remain before covering all squares.";
    private static final String DRAW_MESSAGE = "Game ended as a draw.";
    private static final String GAME_CODE = "KNIGHTS_TOUR";
    private static final String START_POSITION_KEY = "startPosition";
    private static final String KNIGHTS_TOUR_VARIANT = "DEFAULT";

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

        long algorithmStartNanos = System.nanoTime();
        int boardSize = request.getBoardSize();
        int startRow = ThreadLocalRandom.current().nextInt(boardSize);
        int startCol = ThreadLocalRandom.current().nextInt(boardSize);
        AlgorithmType algorithmType = resolveAlgorithmType(request.getAlgorithmType());
        String startPosition = toPosition(startRow, startCol);

        long roundId = createRound(boardSize, startPosition);
        double executionTimeMs = toExecutionTimeMs(System.nanoTime() - algorithmStartNanos);
        saveAlgorithmRun(roundId, algorithmType.name(), startPosition, executionTimeMs);

        StartGameResponse response = new StartGameResponse();
        response.setKnightId(roundId);
        response.setStartPosition(startPosition);
        response.setAlgorithmType(algorithmType.name());
        return response;
    }

    @Transactional
    public ValidateGameResponse validateGame(ValidateGameRequest request) {
        RoundContext round = getRoundContext(request.getKnightId());
        long playerId = ensurePlayerAndGetId(request.getPlayerName().trim());

        OutcomeStatus status;
        String message;

        if (!round.startPosition().equals(request.getMoves().get(0))) {
            status = OutcomeStatus.LOSE;
            message = "First move must match the stored start position.";
            savePlayerAnswer(request.getKnightId(), playerId, request.getMoves(), status, message);
            return buildValidationResponse(status, message);
        }

        try {
            EvaluationResult evaluationResult = evaluateMoves(request.getMoves(), round.boardSize());
            status = evaluationResult.status();
            message = status == OutcomeStatus.WIN ? WIN_MESSAGE : LOSE_MESSAGE;
        } catch (BadRequestException ex) {
            status = OutcomeStatus.LOSE;
            message = ex.getMessage();
            savePlayerAnswer(request.getKnightId(), playerId, request.getMoves(), status, message);
            return buildValidationResponse(status, message);
        }

        OutcomeStatus finalStatus = applyOverride(status, request.getOutcomeOverride());
        String finalMessage = resolveMessage(finalStatus, request.getOutcomeMessage(), message);

        savePlayerAnswer(request.getKnightId(), playerId, request.getMoves(), finalStatus, finalMessage);
        return buildValidationResponse(finalStatus, finalMessage);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        Long gameTypeId = findGameTypeIdByCode(GAME_CODE);
        if (gameTypeId == null) {
            return List.of();
        }

        List<AnswerRow> results = jdbcTemplate.query(
                """
                SELECT p.player_name AS player_name, pa.answer_json AS answer_json, pa.is_correct AS is_correct
                FROM player_answers pa
                JOIN players p ON p.id = pa.player_id
                JOIN game_rounds gr ON gr.id = pa.game_round_id
                WHERE gr.game_type_id = ?
                ORDER BY pa.submitted_at DESC
                """,
                (rs, rowNum) -> new AnswerRow(
                        rs.getString("player_name"),
                        rs.getString("answer_json"),
                        rs.getInt("is_correct") == 1
                ),
                gameTypeId
        );

        Map<String, Stats> statsByPlayer = new HashMap<>();
        for (AnswerRow result : results) {
            String playerName = result.playerName();
            Stats stats = statsByPlayer.computeIfAbsent(playerName, key -> new Stats());
            stats.mostMoves = Math.max(stats.mostMoves, parseMoveCountFromAnswerJson(result.answerJson()));
            if (result.correct()) {
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

    private int parseMoveCountFromAnswerJson(String answerJson) {
        try {
            Map<?, ?> payload = objectMapper.readValue(answerJson, Map.class);
            Object moveCount = payload.get("moveCount");
            if (moveCount instanceof Number number) {
                return number.intValue();
            }

            Object moves = payload.get("moves");
            if (moves instanceof List<?> list) {
                return list.size();
            }

            return 0;
        } catch (JsonProcessingException ex) {
            return 0;
        }
    }

    private Long findGameTypeIdByCode(String code) {
        return jdbcTemplate.query(
                "SELECT id FROM game_types WHERE code = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                code
        );
    }

    private long createRound(int boardSize, String startPosition) {
        Long gameTypeId = findGameTypeIdByCode(GAME_CODE);
        if (gameTypeId == null) {
            throw new IllegalStateException("Game type KNIGHTS_TOUR not found");
        }

        Long nextRoundNo = jdbcTemplate.query(
                "SELECT COALESCE(MAX(round_no), 0) AS max_round FROM game_rounds WHERE game_type_id = ?",
            rs -> rs.next() ? rs.getLong("max_round") + 1 : null,
                gameTypeId
        );
        final long nextRoundNoValue = nextRoundNo == null ? 1L : nextRoundNo;

        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(Map.of(
                    "boardSize", boardSize,
                    START_POSITION_KEY, startPosition
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize round input", ex);
        }

        String expectedJson = String.valueOf(boardSize * boardSize);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO game_rounds (game_type_id, round_no, round_input_json, expected_output_json) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, gameTypeId);
                ps.setLong(2, nextRoundNoValue);
            ps.setString(3, inputJson);
            ps.setString(4, expectedJson);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("Failed to create knights tour round");
        }

        return id.longValue();
    }

    private void saveAlgorithmRun(long roundId, String algorithmName, String startPosition, double executionTimeMs) {
        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(Map.of(
                    START_POSITION_KEY, startPosition
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize algorithm result", ex);
        }

        int changed = jdbcTemplate.update(
                "INSERT OR REPLACE INTO algorithm_runs (game_round_id, algorithm_name, algorithm_variant, execution_time_ms, result_json) VALUES (?, ?, ?, ?, ?)",
                roundId,
                algorithmName,
                KNIGHTS_TOUR_VARIANT,
                executionTimeMs,
                resultJson
        );

        if (changed <= 0) {
            throw new IllegalStateException("Failed to persist algorithm run for round " + roundId);
        }
    }

    private RoundContext getRoundContext(Long roundId) {
        Long gameTypeId = findGameTypeIdByCode(GAME_CODE);
        if (gameTypeId == null) {
            throw new ResourceNotFoundException("Game type KNIGHTS_TOUR not found");
        }

        String inputJson = jdbcTemplate.query(
                "SELECT round_input_json FROM game_rounds WHERE id = ? AND game_type_id = ?",
                rs -> rs.next() ? rs.getString("round_input_json") : null,
                roundId,
                gameTypeId
        );

        if (inputJson == null) {
            throw new ResourceNotFoundException("Knight round not found for id " + roundId);
        }

        try {
            Map<?, ?> payload = objectMapper.readValue(inputJson, Map.class);
            Object boardSizeValue = payload.get("boardSize");
            Object startPositionValue = payload.get(START_POSITION_KEY);

            if (!(boardSizeValue instanceof Number) || !(startPositionValue instanceof String)) {
                throw new IllegalStateException("Invalid round input data");
            }

            return new RoundContext(((Number) boardSizeValue).intValue(), (String) startPositionValue);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to parse round input data", ex);
        }
    }

    private long ensurePlayerAndGetId(String playerName) {
        jdbcTemplate.update("INSERT OR IGNORE INTO players (player_name) VALUES (?)", playerName);
        Long id = jdbcTemplate.query(
                "SELECT id FROM players WHERE player_name = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                playerName
        );

        if (id == null) {
            throw new IllegalStateException("Unable to resolve player id");
        }

        return id;
    }

    private void savePlayerAnswer(long roundId, long playerId, List<String> moves, OutcomeStatus status, String message) {
        String answerJson;
        try {
            answerJson = objectMapper.writeValueAsString(Map.of(
                    "moves", moves,
                    "moveCount", moves.size(),
                    "status", status.name(),
                    "message", message
            ));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize player answer", ex);
        }

        jdbcTemplate.update(
                "INSERT INTO player_answers (game_round_id, player_id, answer_json, is_correct) VALUES (?, ?, ?, ?)",
                roundId,
                playerId,
                answerJson,
                status == OutcomeStatus.WIN ? 1 : 0
        );
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

    private double toExecutionTimeMs(long elapsedNanos) {
        // The start flow is very fast; keep a small non-zero floor so stored metrics are visible.
        return Math.max(0.1d, elapsedNanos / 1_000_000.0d);
    }

    private record EvaluationResult(OutcomeStatus status) {
    }

    private record RoundContext(int boardSize, String startPosition) {
    }

    private record AnswerRow(String playerName, String answerJson, boolean correct) {
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
}
