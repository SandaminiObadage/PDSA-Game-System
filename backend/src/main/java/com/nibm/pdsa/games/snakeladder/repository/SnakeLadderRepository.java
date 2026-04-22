package com.nibm.pdsa.games.snakeladder.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class SnakeLadderRepository {

    private final JdbcTemplate jdbcTemplate;

    public SnakeLadderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long findGameTypeIdByCode(String code) {
        return jdbcTemplate.query(
                "SELECT id FROM game_types WHERE code = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                code);
    }

    public long getNextRoundNo(long gameTypeId) {
        Long current = jdbcTemplate.query(
                "SELECT COALESCE(MAX(round_no), 0) AS max_round FROM game_rounds WHERE game_type_id = ?",
                rs -> rs.next() ? rs.getLong("max_round") : 0L,
                gameTypeId);
        return (current == null ? 0L : current) + 1L;
    }

    public long createGameRound(long gameTypeId, long roundNo, String roundInputJson, String expectedOutputJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO game_rounds (game_type_id, round_no, round_input_json, expected_output_json) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
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

    public void insertAlgorithmRun(long gameRoundId, String algorithmName, String variant, double executionMs,
            String resultJson) {
        jdbcTemplate.update(
                "INSERT INTO algorithm_runs (game_round_id, algorithm_name, algorithm_variant, execution_time_ms, result_json) VALUES (?, ?, ?, ?, ?)",
                gameRoundId, algorithmName, variant, executionMs, resultJson);
    }

    public long ensurePlayerAndGetId(String playerName) {
        jdbcTemplate.update("INSERT OR IGNORE INTO players (player_name) VALUES (?)", playerName);
        Long id = jdbcTemplate.query(
                "SELECT id FROM players WHERE player_name = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                playerName);
        if (id == null) {
            throw new IllegalStateException("Unable to load player id");
        }
        return id;
    }

    public void insertPlayerAnswer(long gameRoundId, long playerId, String answerJson, boolean isCorrect) {
        jdbcTemplate.update(
                "INSERT INTO player_answers (game_round_id, player_id, answer_json, is_correct) VALUES (?, ?, ?, ?)",
                gameRoundId, playerId, answerJson, isCorrect ? 1 : 0);
    }

    public String getExpectedAnswer(long gameRoundId) {
        return jdbcTemplate.query(
                "SELECT expected_output_json FROM game_rounds WHERE id = ?",
                rs -> rs.next() ? rs.getString("expected_output_json") : null,
                gameRoundId);
    }

    public Long findLatestRoundId(long gameTypeId) {
        return jdbcTemplate.query(
                "SELECT id FROM game_rounds WHERE game_type_id = ? ORDER BY id DESC LIMIT 1",
                rs -> rs.next() ? rs.getLong("id") : null,
                gameTypeId);
    }

    public java.util.List<com.nibm.pdsa.games.snakeladder.dto.LeaderboardEntry> findLeaderboard(long gameTypeId,
            int limit, Long gameRoundId) {
        String sql = """
                SELECT
                    p.id AS player_id,
                    p.player_name AS player_name,
                    COUNT(pa.id) AS total_answers,
                    SUM(CASE WHEN pa.is_correct = 1 THEN 1 ELSE 0 END) AS correct_answers,
                    MAX(pa.submitted_at) AS last_submitted_at
                FROM player_answers pa
                JOIN players p ON p.id = pa.player_id
                JOIN game_rounds gr ON gr.id = pa.game_round_id
                WHERE gr.game_type_id = ?
                  AND (? IS NULL OR gr.id = ?)
                GROUP BY p.id, p.player_name
                                ORDER BY
                                        correct_answers DESC,
                                        (CAST(correct_answers AS REAL) / NULLIF(total_answers, 0)) DESC,
                                        total_answers ASC,
                                        last_submitted_at DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            com.nibm.pdsa.games.snakeladder.dto.LeaderboardEntry item = new com.nibm.pdsa.games.snakeladder.dto.LeaderboardEntry();
            long totalAnswers = rs.getLong("total_answers");
            long correctAnswers = rs.getLong("correct_answers");
            double accuracy = totalAnswers > 0 ? (correctAnswers * 100.0) / totalAnswers : 0.0;

            item.setPlayerId(rs.getLong("player_id"));
            item.setPlayerName(rs.getString("player_name"));
            item.setTotalAnswers(totalAnswers);
            item.setCorrectAnswers(correctAnswers);
            item.setAccuracy(accuracy);
            item.setLastSubmittedAt(rs.getString("last_submitted_at"));
            return item;
        }, gameTypeId, gameRoundId, gameRoundId, limit);
    }
}