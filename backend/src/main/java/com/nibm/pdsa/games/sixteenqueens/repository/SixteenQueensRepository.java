package com.nibm.pdsa.games.sixteenqueens.repository;

import com.nibm.pdsa.games.sixteenqueens.dto.PlayerAnswerHistoryItem;
import com.nibm.pdsa.games.sixteenqueens.dto.RoundHistoryItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SixteenQueensRepository {

    private final JdbcTemplate jdbcTemplate;

    public SixteenQueensRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long findGameTypeIdByCode(String code) {
        return jdbcTemplate.query(
                "SELECT id FROM game_types WHERE code = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                code
        );
    }

    public long getNextRoundNo(long gameTypeId) {
        Long current = jdbcTemplate.query(
                "SELECT COALESCE(MAX(round_no), 0) AS max_round FROM game_rounds WHERE game_type_id = ?",
                rs -> rs.next() ? rs.getLong("max_round") : 0L,
                gameTypeId
        );
        return (current == null ? 0L : current) + 1L;
    }

    public long createGameRound(long gameTypeId, long roundNo, String roundInputJson, String expectedOutputJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO game_rounds (game_type_id, round_no, round_input_json, expected_output_json) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, gameTypeId);
            ps.setLong(2, roundNo);
            ps.setString(3, roundInputJson);
            ps.setString(4, expectedOutputJson);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create game round");
        }
        return key.longValue();
    }

    public void insertAlgorithmRun(long gameRoundId, String algorithmName, String variant, double executionMs, String resultJson) {
        jdbcTemplate.update(
                "INSERT INTO algorithm_runs (game_round_id, algorithm_name, algorithm_variant, execution_time_ms, result_json) VALUES (?, ?, ?, ?, ?)",
                gameRoundId, algorithmName, variant, executionMs, resultJson
        );
    }

    public Long findLatestRoundId(long gameTypeId) {
        return jdbcTemplate.query(
                "SELECT id FROM game_rounds WHERE game_type_id = ? ORDER BY round_no DESC LIMIT 1",
                rs -> rs.next() ? rs.getLong("id") : null,
                gameTypeId
        );
    }

    public Long getExpectedTotalSolutionsForRound(long gameRoundId) {
        String value = jdbcTemplate.query(
                "SELECT expected_output_json FROM game_rounds WHERE id = ?",
                rs -> rs.next() ? rs.getString("expected_output_json") : null,
                gameRoundId
        );

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public long ensurePlayerAndGetId(String playerName) {
        jdbcTemplate.update("INSERT OR IGNORE INTO players (player_name) VALUES (?)", playerName);
        Long id = jdbcTemplate.query(
                "SELECT id FROM players WHERE player_name = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                playerName
        );
        if (id == null) {
            throw new IllegalStateException("Unable to load player id");
        }
        return id;
    }

    public void insertPlayerAnswer(long gameRoundId, long playerId, String answerJson, boolean isCorrect) {
        jdbcTemplate.update(
                "INSERT INTO player_answers (game_round_id, player_id, answer_json, is_correct) VALUES (?, ?, ?, ?)",
                gameRoundId, playerId, answerJson, isCorrect ? 1 : 0
        );
    }

    public boolean isActiveRecognized(long gameTypeId, String solutionHash) {
        Integer value = jdbcTemplate.query(
                "SELECT is_active FROM recognized_solutions WHERE game_type_id = ? AND solution_hash = ?",
                rs -> rs.next() ? rs.getInt("is_active") : null,
                gameTypeId, solutionHash
        );
        return value != null && value == 1;
    }

    public void upsertActiveRecognizedSolution(long gameTypeId, String solutionHash, String solutionJson, long playerId) {
        int updated = jdbcTemplate.update(
                "UPDATE recognized_solutions SET solution_json = ?, recognized_by_player_id = ?, recognized_at = CURRENT_TIMESTAMP, is_active = 1 WHERE game_type_id = ? AND solution_hash = ?",
                solutionJson, playerId, gameTypeId, solutionHash
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO recognized_solutions (game_type_id, solution_hash, solution_json, recognized_by_player_id, is_active) VALUES (?, ?, ?, ?, 1)",
                    gameTypeId, solutionHash, solutionJson, playerId
            );
        }
    }

    public long countActiveRecognized(long gameTypeId) {
        Long count = jdbcTemplate.query(
                "SELECT COUNT(*) AS cnt FROM recognized_solutions WHERE game_type_id = ? AND is_active = 1",
                rs -> rs.next() ? rs.getLong("cnt") : 0L,
                gameTypeId
        );
        return count == null ? 0L : count;
    }

    public void clearActiveRecognized(long gameTypeId) {
        jdbcTemplate.update(
                "UPDATE recognized_solutions SET is_active = 0 WHERE game_type_id = ?",
                gameTypeId
        );
    }

    public List<RoundHistoryItem> findRecentRoundHistory(long gameTypeId, int limit) {
        String sql = """
                SELECT
                    gr.id AS game_round_id,
                    gr.round_no AS round_no,
                    gr.round_input_json AS round_input_json,
                    gr.created_at AS created_at,
                    COALESCE((SELECT execution_time_ms FROM algorithm_runs ar WHERE ar.game_round_id = gr.id AND ar.algorithm_name = 'SEQUENTIAL_BACKTRACKING' LIMIT 1), 0) AS sequential_time_ms,
                    COALESCE((SELECT execution_time_ms FROM algorithm_runs ar WHERE ar.game_round_id = gr.id AND ar.algorithm_name = 'THREADED_SEARCH' LIMIT 1), 0) AS parallel_time_ms,
                    (SELECT COUNT(*) FROM player_answers pa WHERE pa.game_round_id = gr.id) AS player_answer_count
                FROM game_rounds gr
                WHERE gr.game_type_id = ?
                ORDER BY gr.round_no DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RoundHistoryItem item = new RoundHistoryItem();
            item.setGameRoundId(rs.getLong("game_round_id"));
            item.setRoundNo(rs.getLong("round_no"));
            item.setBoardSize(extractInt(rs.getString("round_input_json"), "boardSize"));
            item.setThreadCount(extractInt(rs.getString("round_input_json"), "threadCount"));
            item.setSequentialTimeMs(rs.getLong("sequential_time_ms"));
            item.setParallelTimeMs(rs.getLong("parallel_time_ms"));
            item.setPlayerAnswerCount(rs.getLong("player_answer_count"));
            item.setCreatedAt(rs.getString("created_at"));
            return item;
        }, gameTypeId, limit);
    }

    public List<PlayerAnswerHistoryItem> findRecentAnswers(long gameTypeId, int limit) {
        String sql = """
                SELECT
                    pa.id AS answer_id,
                    pa.game_round_id AS game_round_id,
                    gr.round_no AS round_no,
                    p.player_name AS player_name,
                    pa.answer_json AS answer_json,
                    pa.is_correct AS is_correct,
                    pa.submitted_at AS submitted_at
                FROM player_answers pa
                JOIN players p ON p.id = pa.player_id
                JOIN game_rounds gr ON gr.id = pa.game_round_id
                WHERE gr.game_type_id = ?
                ORDER BY pa.submitted_at DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PlayerAnswerHistoryItem item = new PlayerAnswerHistoryItem();
            item.setAnswerId(rs.getLong("answer_id"));
            item.setGameRoundId(rs.getLong("game_round_id"));
            item.setRoundNo(rs.getLong("round_no"));
            item.setPlayerName(rs.getString("player_name"));
            item.setAnswerJson(rs.getString("answer_json"));
            item.setCorrect(rs.getInt("is_correct") == 1);
            item.setSubmittedAt(rs.getString("submitted_at"));
            return item;
        }, gameTypeId, limit);
    }

    private int extractInt(String json, String fieldName) {
        if (json == null) {
            return 0;
        }
        String needle = "\"" + fieldName + "\":";
        int start = json.indexOf(needle);
        if (start < 0) {
            return 0;
        }
        start += needle.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
