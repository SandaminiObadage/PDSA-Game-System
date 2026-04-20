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
}