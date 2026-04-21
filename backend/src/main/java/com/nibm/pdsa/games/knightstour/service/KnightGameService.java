package com.nibm.pdsa.games.knightstour.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.pdsa.games.knightstour.dto.LeaderboardEntryResponse;
import com.nibm.pdsa.games.knightstour.dto.StartGameRequest;
import com.nibm.pdsa.games.knightstour.dto.StartGameResponse;
import com.nibm.pdsa.games.knightstour.dto.ValidateGameRequest;
import com.nibm.pdsa.games.knightstour.dto.ValidateGameResponse;
import com.nibm.pdsa.games.knightstour.entity.AlgorithmType;
import com.nibm.pdsa.games.knightstour.entity.GameResult;
import com.nibm.pdsa.games.knightstour.entity.Knight;
import com.nibm.pdsa.games.knightstour.entity.OutcomeStatus;
import com.nibm.pdsa.games.knightstour.entity.Player;
import com.nibm.pdsa.games.knightstour.exception.BadRequestException;
import com.nibm.pdsa.games.knightstour.exception.ResourceNotFoundException;
import com.nibm.pdsa.games.knightstour.repository.GameResultRepository;
import com.nibm.pdsa.games.knightstour.repository.KnightRepository;
import com.nibm.pdsa.games.knightstour.repository.PlayerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class KnightGameService {

    private static final String WIN_MESSAGE = "Congratulations! Valid full knight tour.";
    private static final String LOSE_MESSAGE = "No legal moves remain before covering all squares.";
    private static final String DRAW_MESSAGE = "Game ended as a draw.";

    private final KnightRepository knightRepository;
    private final PlayerRepository playerRepository;
    private final GameResultRepository gameResultRepository;
    private final ObjectMapper objectMapper;

    public KnightGameService(
            KnightRepository knightRepository,
            PlayerRepository playerRepository,
            GameResultRepository gameResultRepository,
            ObjectMapper objectMapper
    ) {
        this.knightRepository = knightRepository;
        this.playerRepository = playerRepository;
        this.gameResultRepository = gameResultRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StartGameResponse startGame(StartGameRequest request) {
        validateBoardSize(request.getBoardSize());

        int boardSize = request.getBoardSize();
        int startRow = ThreadLocalRandom.current().nextInt(boardSize);
        int startCol = ThreadLocalRandom.current().nextInt(boardSize);
        AlgorithmType algorithmType = resolveAlgorithmType(request.getAlgorithmType());

        Knight knight = new Knight();
        knight.setBoardSize(boardSize);
        knight.setStartPosition(toPosition(startRow, startCol));
        knight.setAlgorithmType(algorithmType);
        Knight saved = knightRepository.save(knight);

        StartGameResponse response = new StartGameResponse();
        response.setKnightId(saved.getId());
        response.setStartPosition(saved.getStartPosition());
        response.setAlgorithmType(saved.getAlgorithmType().name());
        return response;
    }

    @Transactional
    public ValidateGameResponse validateGame(ValidateGameRequest request) {
        Knight knight = knightRepository.findById(request.getKnightId())
                .orElseThrow(() -> new ResourceNotFoundException("Knight not found for id " + request.getKnightId()));

        Player player = getOrCreatePlayer(request.getPlayerName().trim());

        OutcomeStatus status;
        String message;

        if (!knight.getStartPosition().equals(request.getMoves().get(0))) {
            status = OutcomeStatus.LOSE;
            message = "First move must match the stored start position.";
            persistResult(player, knight, request.getMoves(), status);
            return buildValidationResponse(status, message);
        }

        try {
            EvaluationResult evaluationResult = evaluateMoves(request.getMoves(), knight.getBoardSize());
            status = evaluationResult.status();
            message = status == OutcomeStatus.WIN ? WIN_MESSAGE : LOSE_MESSAGE;
        } catch (BadRequestException ex) {
            status = OutcomeStatus.LOSE;
            message = ex.getMessage();
            persistResult(player, knight, request.getMoves(), status);
            return buildValidationResponse(status, message);
        }

        OutcomeStatus finalStatus = applyOverride(status, request.getOutcomeOverride());
        String finalMessage = resolveMessage(finalStatus, request.getOutcomeMessage(), message);

        persistResult(player, knight, request.getMoves(), finalStatus);
        return buildValidationResponse(finalStatus, finalMessage);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<GameResult> results = gameResultRepository.findAllByOrderByCreatedAtDesc();

        Map<String, Stats> statsByPlayer = new HashMap<>();
        for (GameResult result : results) {
            String playerName = result.getPlayer().getName();
            Stats stats = statsByPlayer.computeIfAbsent(playerName, key -> new Stats());
            stats.mostMoves = Math.max(stats.mostMoves, parseMoveCount(result.getMoves()));
            if (result.isWin()) {
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

    private Player getOrCreatePlayer(String playerName) {
        Optional<Player> existing = playerRepository.findByName(playerName);
        if (existing.isPresent()) {
            return existing.get();
        }

        Player player = new Player();
        player.setName(playerName);
        return playerRepository.save(player);
    }

    private void persistResult(Player player, Knight knight, List<String> moves, OutcomeStatus status) {
        GameResult result = new GameResult();
        result.setPlayer(player);
        result.setKnight(knight);
        result.setMoves(toMovesJson(moves));
        result.setWin(status == OutcomeStatus.WIN);
        gameResultRepository.save(result);
    }

    private String toMovesJson(List<String> moves) {
        try {
            return objectMapper.writeValueAsString(moves);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize moves", ex);
        }
    }

    private int parseMoveCount(String movesJson) {
        try {
            List<?> values = objectMapper.readValue(movesJson, List.class);
            return values.size();
        } catch (JsonProcessingException ex) {
            return 0;
        }
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
