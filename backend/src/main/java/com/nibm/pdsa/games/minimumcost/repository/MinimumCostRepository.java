package com.nibm.pdsa.games.minimumcost.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MinimumCostRepository {

    private final JdbcTemplate jdbcTemplate;

    public MinimumCostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeTables();
    }

    private void initializeTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS minimum_cost_rounds (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    round_no INTEGER NOT NULL UNIQUE,
                    n INTEGER NOT NULL CHECK (n BETWEEN 50 AND 100),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS minimum_cost_cost_matrices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    round_id INTEGER NOT NULL UNIQUE,
                    matrix_json TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (round_id) REFERENCES minimum_cost_rounds(id) ON DELETE CASCADE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS minimum_cost_algorithm_results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    round_id INTEGER NOT NULL,
                    algorithm_name TEXT NOT NULL,
                    algorithm_variant TEXT NOT NULL DEFAULT '',
                    total_cost INTEGER NOT NULL,
                    execution_time_ms REAL NOT NULL,
                    assignment_json TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (round_id) REFERENCES minimum_cost_rounds(id) ON DELETE CASCADE,
                    UNIQUE (round_id, algorithm_name, algorithm_variant)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS minimum_cost_player_submissions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_name TEXT NOT NULL,
                    round_id INTEGER NOT NULL,
                    submitted_cost INTEGER NOT NULL,
                    assignment_json TEXT NOT NULL,
                    is_optimal INTEGER NOT NULL CHECK (is_optimal IN (0, 1)),
                    submission_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (round_id) REFERENCES minimum_cost_rounds(id) ON DELETE CASCADE,
                    UNIQUE (round_id, player_name, submission_hash)
                )
                """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_min_cost_results_round ON minimum_cost_algorithm_results(round_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_min_cost_submissions_round ON minimum_cost_player_submissions(round_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_min_cost_submissions_player ON minimum_cost_player_submissions(player_name)");
    }

    public long getNextRoundNo() {
        Long current = jdbcTemplate.query(
                "SELECT COALESCE(MAX(round_no), 0) AS max_round FROM minimum_cost_rounds",
                rs -> rs.next() ? rs.getLong("max_round") : 0L
        );
        return (current == null ? 0L : current) + 1L;
    }

    public long createRound(long roundNo, int n) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO minimum_cost_rounds (round_no, n) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, roundNo);
            statement.setInt(2, n);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to create minimum cost round");
        }
        return key.longValue();
    }

    public void saveCostMatrix(long roundId, String matrixJson) {
        jdbcTemplate.update(
                "INSERT OR REPLACE INTO minimum_cost_cost_matrices (round_id, matrix_json, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                roundId, matrixJson
        );
    }

    public void saveAlgorithmResult(long roundId, String algorithmName, String algorithmVariant, int totalCost, long executionTimeMs, String assignmentJson) {
        jdbcTemplate.update(
                "INSERT OR REPLACE INTO minimum_cost_algorithm_results (round_id, algorithm_name, algorithm_variant, total_cost, execution_time_ms, assignment_json, created_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                roundId, algorithmName, algorithmVariant, totalCost, executionTimeMs, assignmentJson
        );
    }

    public RoundRow findRound(long roundId) {
        return jdbcTemplate.query(
                "SELECT r.id, r.round_no, r.n, r.created_at, m.matrix_json FROM minimum_cost_rounds r JOIN minimum_cost_cost_matrices m ON m.round_id = r.id WHERE r.id = ?",
                rs -> rs.next() ? new RoundRow(
                        rs.getLong("id"),
                        rs.getLong("round_no"),
                        rs.getInt("n"),
                        rs.getString("matrix_json"),
                        rs.getString("created_at")
                ) : null,
                roundId
        );
    }

    public Long findLatestRoundId() {
        return jdbcTemplate.query(
                "SELECT id FROM minimum_cost_rounds ORDER BY round_no DESC LIMIT 1",
                rs -> rs.next() ? rs.getLong("id") : null
        );
    }

    public List<AlgorithmResultRow> findAlgorithmResults(long roundId) {
        return jdbcTemplate.query(
                "SELECT algorithm_name, algorithm_variant, total_cost, execution_time_ms, assignment_json, created_at FROM minimum_cost_algorithm_results WHERE round_id = ? ORDER BY id ASC",
                (rs, rowNum) -> new AlgorithmResultRow(
                        roundId,
                        rs.getString("algorithm_name"),
                        rs.getString("algorithm_variant"),
                        rs.getInt("total_cost"),
                        rs.getLong("execution_time_ms"),
                        rs.getString("assignment_json"),
                        rs.getString("created_at")
                ),
                roundId
        );
    }

    public Integer findOptimalCost(long roundId) {
        return jdbcTemplate.query(
                "SELECT total_cost FROM minimum_cost_algorithm_results WHERE round_id = ? AND algorithm_name = 'HUNGARIAN' ORDER BY id ASC LIMIT 1",
                rs -> rs.next() ? rs.getInt("total_cost") : null,
                roundId
        );
    }

    public boolean submissionExists(long roundId, String playerName, String submissionHash) {
        Integer value = jdbcTemplate.query(
                "SELECT 1 AS found FROM minimum_cost_player_submissions WHERE round_id = ? AND player_name = ? AND submission_hash = ? LIMIT 1",
                rs -> rs.next() ? rs.getInt("found") : null,
                roundId, playerName, submissionHash
        );
        return value != null;
    }

    public long insertSubmission(String playerName, long roundId, int submittedCost, String assignmentJson, boolean optimal, String submissionHash) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO minimum_cost_player_submissions (player_name, round_id, submitted_cost, assignment_json, is_optimal, submission_hash) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, playerName);
            statement.setLong(2, roundId);
            statement.setInt(3, submittedCost);
            statement.setString(4, assignmentJson);
            statement.setInt(5, optimal ? 1 : 0);
            statement.setString(6, submissionHash);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to save submission");
        }
        return key.longValue();
    }

    public List<LeaderboardRow> findLeaderboard(int limit, Long roundId) {
        String sql = roundId == null
                ? """
                SELECT player_name, MIN(submitted_cost) AS best_submitted_cost, SUM(CASE WHEN is_optimal = 1 THEN 1 ELSE 0 END) AS optimal_submissions, COUNT(*) AS total_submissions, MAX(created_at) AS last_submitted_at
                FROM minimum_cost_player_submissions
                GROUP BY player_name
                ORDER BY best_submitted_cost ASC, optimal_submissions DESC, total_submissions ASC, last_submitted_at ASC
                LIMIT ?
                """
                : """
                SELECT player_name, MIN(submitted_cost) AS best_submitted_cost, SUM(CASE WHEN is_optimal = 1 THEN 1 ELSE 0 END) AS optimal_submissions, COUNT(*) AS total_submissions, MAX(created_at) AS last_submitted_at
                FROM minimum_cost_player_submissions
                WHERE round_id = ?
                GROUP BY player_name
                ORDER BY best_submitted_cost ASC, optimal_submissions DESC, total_submissions ASC, last_submitted_at ASC
                LIMIT ?
                """;

        return roundId == null
                ? jdbcTemplate.query(sql, (rs, rowNum) -> new LeaderboardRow(
                        rs.getString("player_name"),
                        rs.getInt("best_submitted_cost"),
                        rs.getInt("optimal_submissions"),
                        rs.getInt("total_submissions"),
                        rs.getString("last_submitted_at")
                ), limit)
                : jdbcTemplate.query(sql, (rs, rowNum) -> new LeaderboardRow(
                        rs.getString("player_name"),
                        rs.getInt("best_submitted_cost"),
                        rs.getInt("optimal_submissions"),
                        rs.getInt("total_submissions"),
                        rs.getString("last_submitted_at")
                ), roundId, limit);
    }

    public List<HistoryRoundRow> findRecentRounds(int limit) {
        String sql = """
                SELECT
                    r.id AS round_id,
                    r.round_no AS round_no,
                    r.n AS n,
                    r.created_at AS created_at,
                    COALESCE(h.total_cost, 0) AS hungarian_cost,
                    COALESCE(h.execution_time_ms, 0) AS hungarian_time_ms,
                    COALESCE(a.total_cost, 0) AS alternative_cost,
                    COALESCE(a.execution_time_ms, 0) AS alternative_time_ms
                FROM minimum_cost_rounds r
                LEFT JOIN minimum_cost_algorithm_results h
                    ON h.round_id = r.id AND h.algorithm_name = 'HUNGARIAN'
                LEFT JOIN minimum_cost_algorithm_results a
                    ON a.round_id = r.id AND a.algorithm_name = 'GREEDY_LOCAL_OPTIMIZATION'
                ORDER BY r.round_no DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long hungarianTime = rs.getLong("hungarian_time_ms");
            long alternativeTime = rs.getLong("alternative_time_ms");
            return new HistoryRoundRow(
                    rs.getLong("round_id"),
                    rs.getLong("round_no"),
                    rs.getInt("n"),
                    rs.getInt("hungarian_cost"),
                    hungarianTime,
                    rs.getInt("alternative_cost"),
                    alternativeTime,
                    alternativeTime == 0 ? 0.0 : (double) hungarianTime / alternativeTime,
                    rs.getString("created_at")
            );
        }, limit);
    }

    public List<HistorySubmissionRow> findRecentSubmissions(int limit) {
        String sql = """
                SELECT id, round_id, player_name, submitted_cost, is_optimal, created_at
                FROM minimum_cost_player_submissions
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new HistorySubmissionRow(
                rs.getLong("id"),
                rs.getLong("round_id"),
                rs.getString("player_name"),
                rs.getInt("submitted_cost"),
                rs.getInt("is_optimal") == 1,
                rs.getString("created_at")
        ), limit);
    }

    public SummaryRow findSummary() {
        String roundSql = """
                SELECT
                    COUNT(*) AS total_rounds,
                    COALESCE(AVG(h.execution_time_ms), 0) AS avg_hungarian,
                    COALESCE(AVG(a.execution_time_ms), 0) AS avg_alternative,
                    COALESCE(MIN(h.execution_time_ms), 0) AS best_hungarian,
                    COALESCE(MIN(a.execution_time_ms), 0) AS best_alternative
                FROM minimum_cost_rounds r
                LEFT JOIN minimum_cost_algorithm_results h
                    ON h.round_id = r.id AND h.algorithm_name = 'HUNGARIAN'
                LEFT JOIN minimum_cost_algorithm_results a
                    ON a.round_id = r.id AND a.algorithm_name = 'GREEDY_LOCAL_OPTIMIZATION'
                """;

        String submissionSql = """
                SELECT
                    COUNT(*) AS total_submissions,
                    COALESCE(SUM(CASE WHEN is_optimal = 1 THEN 1 ELSE 0 END), 0) AS optimal_submissions
                FROM minimum_cost_player_submissions
                """;

        return jdbcTemplate.query(roundSql, roundRs -> {
            if (!roundRs.next()) {
                return new SummaryRow(0L, 0.0, 0.0, 0L, 0L, 0L, 0L);
            }
            return jdbcTemplate.query(submissionSql, submissionRs -> {
                if (!submissionRs.next()) {
                    return new SummaryRow(0L, 0.0, 0.0, 0L, 0L, 0L, 0L);
                }
                return new SummaryRow(
                        roundRs.getLong("total_rounds"),
                        roundRs.getDouble("avg_hungarian"),
                        roundRs.getDouble("avg_alternative"),
                        roundRs.getLong("best_hungarian"),
                        roundRs.getLong("best_alternative"),
                        submissionRs.getLong("total_submissions"),
                        submissionRs.getLong("optimal_submissions")
                );
            });
        });
    }

    public record RoundRow(long roundId, long roundNo, int n, String matrixJson, String createdAt) {}

    public record AlgorithmResultRow(long roundId, String algorithmName, String algorithmVariant, int totalCost, long executionTimeMs, String assignmentJson, String createdAt) {}

    public record LeaderboardRow(String playerName, int bestSubmittedCost, int optimalSubmissions, int totalSubmissions, String lastSubmittedAt) {}

    public record HistoryRoundRow(long roundId, long roundNo, int n, int hungarianCost, long hungarianTimeMs, int alternativeCost, long alternativeTimeMs, double speedup, String createdAt) {}

    public record HistorySubmissionRow(long submissionId, long roundId, String playerName, int submittedCost, boolean optimal, String createdAt) {}

    public record SummaryRow(long totalRounds, double averageHungarianTimeMs, double averageAlternativeTimeMs, long bestHungarianTimeMs, long bestAlternativeTimeMs, long totalSubmissions, long optimalSubmissions) {}
}