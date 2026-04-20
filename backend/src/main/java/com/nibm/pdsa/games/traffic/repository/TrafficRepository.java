package com.nibm.pdsa.games.traffic.repository;

import com.nibm.pdsa.games.traffic.dto.TrafficLeaderboardEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * Repository for Traffic Simulation Game data access.
 */
@Repository
public class TrafficRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private static final String GAME_CODE = "TRAFFIC_SIMULATION";
    private static final String GAME_NAME = "Traffic Simulation";

    public TrafficRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
        initializeGameType();
    }

    /**
     * Initialize game type if not exists.
     */
    public void initializeGameType() {
        Long gameTypeId = findGameTypeIdByCode(GAME_CODE);
        if (gameTypeId == null) {
            String sql = "INSERT INTO game_types (code, display_name) VALUES (?, ?)";
            try {
                jdbcTemplate.update(sql, GAME_CODE, GAME_NAME);
            } catch (Exception e) {
                // Game type might already exist, ignore
            }
        }
    }

    /**
     * Find game type ID by code.
     */
    private Long findGameTypeIdByCode(String code) {
        return jdbcTemplate.query(
                "SELECT id FROM game_types WHERE code = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                code
        );
    }

    /**
     * Get next round number.
     */
    private long getNextRoundNo(long gameTypeId) {
        Long current = jdbcTemplate.query(
                "SELECT COALESCE(MAX(round_no), 0) AS max_round FROM game_rounds WHERE game_type_id = ?",
                rs -> rs.next() ? rs.getLong("max_round") : 0L,
                gameTypeId
        );
        return (current != null ? current : 0) + 1;
    }

    /**
     * Save a game round.
     */
    public long saveGameRound(String source, String sink, Map<String, Integer> edgeCapacities) {
        Long gameTypeId = findGameTypeIdByCode(GAME_CODE);
        if (gameTypeId == null) {
            throw new RuntimeException("Game type not found");
        }

        long roundNo = getNextRoundNo(gameTypeId);

        Map<String, Object> inputJson = new HashMap<>();
        inputJson.put("source", source);
        inputJson.put("sink", sink);
        inputJson.put("edges", edgeCapacities);

        try {
            String inputJsonStr = objectMapper.writeValueAsString(inputJson);
            String sql = "INSERT INTO game_rounds (game_type_id, round_no, round_input_json) VALUES (?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, gameTypeId);
                ps.setLong(2, roundNo);
                ps.setString(3, inputJsonStr);
                return ps;
            }, keyHolder);

            return keyHolder.getKey().longValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save game round", e);
        }
    }

    /**
     * Save algorithm result.
     */
    public void saveAlgorithmResult(long roundId, String algorithmName, long executionTimeMs, 
                                    int maxFlow, Map<String, Map<String, Integer>> flowMap) {
        try {
            System.out.println("DEBUG saveAlgorithmResult: roundId=" + roundId + ", algorithmName=" + algorithmName + ", maxFlow=" + maxFlow);
            
            String resultJson = objectMapper.writeValueAsString(Map.of(
                    "maxFlow", maxFlow,
                    "flowMap", flowMap
            ));

            String sql = "INSERT OR IGNORE INTO algorithm_runs (game_round_id, algorithm_name, execution_time_ms, result_json) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, roundId, algorithmName, executionTimeMs, resultJson);

            // Update expected output with the first result (all should be the same)
            if ("Ford-Fulkerson".equals(algorithmName)) {
                String updateSql = "UPDATE game_rounds SET expected_output_json = ? WHERE id = ? AND expected_output_json IS NULL";
                String outputJson = objectMapper.writeValueAsString(Map.of("maxFlow", maxFlow));
                System.out.println("DEBUG: Updating expected_output_json for roundId=" + roundId + " with: " + outputJson);
                int updatedRows = jdbcTemplate.update(updateSql, outputJson, roundId);
                System.out.println("DEBUG: Updated " + updatedRows + " rows");
            }
        } catch (Exception e) {
            System.err.println("ERROR in saveAlgorithmResult: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save algorithm result", e);
        }
    }

    /**
     * Get correct maximum flow for a round.
     */
    public Integer getCorrectMaxFlow(long roundId) {
        try {
            String sql = "SELECT expected_output_json FROM game_rounds WHERE id = ?";
            String json = jdbcTemplate.query(sql, 
                    rs -> rs.next() ? rs.getString("expected_output_json") : null,
                    roundId
            );

            if (json == null || json.trim().isEmpty()) {
                System.err.println("ERROR: No expected_output_json found for roundId: " + roundId);
                return null;
            }
            
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            Object maxFlowObj = data.get("maxFlow");
            
            if (maxFlowObj == null) {
                System.err.println("ERROR: maxFlow key not found in JSON: " + json);
                return null;
            }
            
            // Handle all numeric types that JSON might deserialize to
            int maxFlowValue;
            if (maxFlowObj instanceof Integer) {
                maxFlowValue = (Integer) maxFlowObj;
            } else if (maxFlowObj instanceof Double) {
                maxFlowValue = ((Double) maxFlowObj).intValue();
            } else if (maxFlowObj instanceof Long) {
                maxFlowValue = ((Long) maxFlowObj).intValue();
            } else if (maxFlowObj instanceof Number) {
                maxFlowValue = ((Number) maxFlowObj).intValue();
            } else if (maxFlowObj instanceof String) {
                try {
                    maxFlowValue = Integer.parseInt((String) maxFlowObj);
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Cannot parse maxFlow as integer: " + maxFlowObj);
                    return null;
                }
            } else {
                System.err.println("ERROR: Unknown type for maxFlow: " + maxFlowObj.getClass().getName());
                return null;
            }
            
            System.out.println("DEBUG: Retrieved correct max flow = " + maxFlowValue + " (type: " + maxFlowObj.getClass().getSimpleName() + ")");
            return maxFlowValue;
        } catch (Exception e) {
            System.err.println("ERROR in getCorrectMaxFlow: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get correct answer: " + e.getMessage(), e);
        }
    }

    /**
     * Save player answer.
     */
    public void savePlayerAnswer(long roundId, String playerName, int playerAnswer, boolean isCorrect) {
        try {
            // Get or create player
            Long playerId = getOrCreatePlayer(playerName);

            // Save answer
            String answerJson = objectMapper.writeValueAsString(Map.of("maxFlow", playerAnswer));
            String sql = "INSERT INTO player_answers (game_round_id, player_id, answer_json, is_correct) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, roundId, playerId, answerJson, isCorrect ? 1 : 0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save player answer", e);
        }
    }

    /**
     * Get or create player.
     */
    private Long getOrCreatePlayer(String playerName) {
        Long playerId = findPlayerIdByName(playerName);
        if (playerId == null) {
            String sql = "INSERT INTO players (player_name) VALUES (?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, playerName);
                return ps;
            }, keyHolder);
            playerId = keyHolder.getKey().longValue();
        }
        return playerId;
    }

    /**
     * Find player ID by name.
     */
    private Long findPlayerIdByName(String playerName) {
        return jdbcTemplate.query(
                "SELECT id FROM players WHERE player_name = ?",
                rs -> rs.next() ? rs.getLong("id") : null,
                playerName
        );
    }

    /**
     * Get algorithm execution time for a round.
     */
    public long getAlgorithmExecutionTime(long roundId, String algorithmName) {
        Long time = jdbcTemplate.query(
                "SELECT execution_time_ms FROM algorithm_runs WHERE game_round_id = ? AND algorithm_name = ?",
                rs -> rs.next() ? rs.getLong("execution_time_ms") : null,
                roundId, algorithmName
        );
        return time != null ? time : -1;
    }

    /**
     * Get leaderboard.
     */
    public List<TrafficLeaderboardEntry> getLeaderboard(int limit) {
        String sql = """
                SELECT 
                    p.player_name,
                    COUNT(CASE WHEN pa.is_correct = 1 THEN 1 END) AS correct_answers,
                    AVG(ar.execution_time_ms) AS avg_execution_time,
                    MAX(pa.submitted_at) AS last_played
                FROM players p
                LEFT JOIN player_answers pa ON p.id = pa.player_id
                LEFT JOIN game_rounds gr ON pa.game_round_id = gr.id
                LEFT JOIN algorithm_runs ar ON gr.id = ar.game_round_id
                WHERE gr.game_type_id = (SELECT id FROM game_types WHERE code = ?)
                GROUP BY p.id, p.player_name
                ORDER BY correct_answers DESC, avg_execution_time ASC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String playerName = rs.getString("player_name");
            int correctAnswers = rs.getInt("correct_answers");
            double avgTime = rs.getDouble("avg_execution_time");
            long lastPlayed = rs.getTimestamp("last_played") != null ? 
                    rs.getTimestamp("last_played").getTime() : System.currentTimeMillis();

            return new TrafficLeaderboardEntry(playerName, correctAnswers, avgTime, lastPlayed);
        }, GAME_CODE, limit);
    }

    /**
     * Close game round.
     */
    public void closeRound(long roundId) {
        String sql = "UPDATE game_rounds SET expected_output_json = ? WHERE id = ? AND expected_output_json IS NULL";
        // Expected output should already be set after algorithms run
        jdbcTemplate.update(sql, "", roundId);
    }
}
