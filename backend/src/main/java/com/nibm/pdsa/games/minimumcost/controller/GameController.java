package com.nibm.pdsa.games.minimumcost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.pdsa.games.minimumcost.algorithm.AlgorithmsService;
import com.nibm.pdsa.games.minimumcost.model.Player;
import com.nibm.pdsa.games.minimumcost.repository.PlayerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/games/minimum-cost")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class GameController {

    private static final String GAME_CODE = "MINIMUM_COST";
    private static final int MIN_TASKS = 50;
    private static final int MAX_TASKS = 100;
    private static final int MIN_COST = 20;
    private static final int MAX_COST = 200;

    private final Random random = new Random();

    @Autowired
    private AlgorithmsService algorithmsService;
    
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Backend is running!");
        response.put("version", "1.0");
        response.put("game", "Minimum Cost Assignment");
        return response;
    }

    @GetMapping("/game")
    public Map<String, Object> getGameData(@RequestParam(required = false) Integer tasks) {
        int taskCount = resolveTaskCount(tasks);
        int[][] matrix = generateMatrix(taskCount);

        long greedyStart = System.nanoTime();
        int greedyCost = algorithmsService.greedy(matrix);
        double greedyExecutionMs = toMillis(System.nanoTime() - greedyStart);

        long hungarianStart = System.nanoTime();
        int hungarianCost = algorithmsService.hungarian(matrix);
        double hungarianExecutionMs = toMillis(System.nanoTime() - hungarianStart);

        Long roundId = persistRoundAndAlgorithmRuns(matrix, taskCount, greedyCost, greedyExecutionMs, hungarianCost, hungarianExecutionMs);

        Map<String, Object> response = new HashMap<>();
        response.put("matrix", matrix);
        response.put("greedyCost", greedyCost);
        response.put("hungarianCost", hungarianCost);
        response.put("matrixSize", taskCount);
        response.put("taskCount", taskCount);
        response.put("greedyExecutionTimeMs", greedyExecutionMs);
        response.put("hungarianExecutionTimeMs", hungarianExecutionMs);
        response.put("roundId", roundId);

        return response;
    }

    @PostMapping("/score")
    public Map<String, Object> saveGameResult(@RequestBody Player gameResult) {
        // Set timestamp if not already set
        if (gameResult.getTimestamp() == 0) {
            gameResult.setTimestamp(System.currentTimeMillis());
        }
        
        Player savedResult = playerRepository.save(gameResult);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", savedResult.getId());
        response.put("isCorrect", savedResult.isCorrect());
        response.put("message", gameResult.isCorrect() 
            ? "✅ Correct! Result saved! 🎉" 
            : "❌ Wrong! Result saved.");
        response.put("correctCost", savedResult.getCorrectCost());
        response.put("selectedCost", savedResult.getSelectedCost());
        
        return response;
    }

    @GetMapping("/leaderboard")
    public Map<String, Object> getLeaderboard(
            @RequestParam(defaultValue = "20") int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Player> allResults = playerRepository.findAllResults();

        Map<String, LeaderboardStats> statsByPlayer = new HashMap<>();
        for (Player result : allResults) {
            String playerName = result.getPlayerName() == null ? "Unknown" : result.getPlayerName().trim();
            if (playerName.isEmpty()) {
                playerName = "Unknown";
            }

            LeaderboardStats stats = statsByPlayer.computeIfAbsent(playerName, LeaderboardStats::new);
            stats.gamesPlayed++;
            stats.totalTimeRemaining += Math.max(0, result.getTimeRemaining());
            stats.lastTimestamp = Math.max(stats.lastTimestamp, result.getTimestamp());

            if (result.isCorrect()) {
                stats.correctAnswers++;
                stats.totalScore += 10;
            }
        }

        List<LeaderboardStats> sorted = new ArrayList<>(statsByPlayer.values());
        sorted.sort(
                Comparator.comparingInt(LeaderboardStats::getTotalScore).reversed()
                        .thenComparingInt(LeaderboardStats::getCorrectAnswers).reversed()
                        .thenComparingDouble(LeaderboardStats::getAccuracy).reversed()
                        .thenComparingDouble(LeaderboardStats::getAverageTimeRemaining).reversed()
                        .thenComparingLong(LeaderboardStats::getLastTimestamp).reversed()
        );

        List<Map<String, Object>> leaderboardRows = new ArrayList<>();
        for (int i = 0; i < Math.min(safeLimit, sorted.size()); i++) {
            LeaderboardStats stats = sorted.get(i);
            Map<String, Object> row = new HashMap<>();
            row.put("rank", i + 1);
            row.put("playerName", stats.playerName);
            row.put("gamesPlayed", stats.gamesPlayed);
            row.put("correctAnswers", stats.correctAnswers);
            row.put("totalScore", stats.totalScore);
            row.put("accuracy", String.format("%.1f%%", stats.getAccuracy()));
            row.put("averageTimeRemaining", String.format("%.1f", stats.getAverageTimeRemaining()));
            row.put("lastPlayed", stats.lastTimestamp);
            leaderboardRows.add(row);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", leaderboardRows);
        response.put("total", leaderboardRows.size());
        response.put("message", "Top " + safeLimit + " players by score and accuracy");
        
        return response;
    }

    @GetMapping("/all-results")
    public Map<String, Object> getAllResults(
            @RequestParam(defaultValue = "100") int limit) {
        
        List<Player> allResults = playerRepository.findAllResults();
        
        // Limit the results
        if (allResults.size() > limit) {
            allResults = allResults.subList(0, limit);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", allResults);
        response.put("total", allResults.size());
        response.put("message", "Latest " + limit + " game results");
        
        return response;
    }

    @GetMapping("/player-history")
    public Map<String, Object> getPlayerHistory(
            @RequestParam String playerName,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Player> playerResults = playerRepository.findByPlayerNameOrderByTimestampDesc(playerName);
        
        // Limit the results
        if (playerResults.size() > limit) {
            playerResults = playerResults.subList(0, limit);
        }
        
        // Calculate stats
        long correctCount = playerResults.stream().filter(Player::isCorrect).count();
        long totalGames = playerResults.size();
        double accuracy = totalGames > 0 ? (double) correctCount / totalGames * 100 : 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerName", playerName);
        response.put("results", playerResults);
        response.put("totalGames", totalGames);
        response.put("correctAnswers", correctCount);
        response.put("accuracy", String.format("%.1f%%", accuracy));
        response.put("message", "History for player: " + playerName);
        
        return response;
    }

    private int[][] generateMatrix(int n) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = random.nextInt(MAX_COST - MIN_COST + 1) + MIN_COST;
            }
        }
        return matrix;
    }

    private int resolveTaskCount(Integer tasks) {
        if (tasks == null) {
            return random.nextInt(MAX_TASKS - MIN_TASKS + 1) + MIN_TASKS;
        }
        if (tasks < MIN_TASKS || tasks > MAX_TASKS) {
            throw new IllegalArgumentException("tasks must be between 50 and 100");
        }
        return tasks;
    }

    private double toMillis(long nanos) {
        return nanos / 1_000_000.0;
    }

    private Long persistRoundAndAlgorithmRuns(
            int[][] matrix,
            int taskCount,
            int greedyCost,
            double greedyExecutionMs,
            int hungarianCost,
            double hungarianExecutionMs
    ) {
        if (jdbcTemplate == null || objectMapper == null) {
            return null;
        }

        try {
            Long gameTypeId = jdbcTemplate.query(
                    "SELECT id FROM game_types WHERE code = ?",
                    rs -> rs.next() ? rs.getLong("id") : null,
                    GAME_CODE
            );

            if (gameTypeId == null) {
                return null;
            }

            Long nextRoundNo = jdbcTemplate.query(
                    "SELECT COALESCE(MAX(round_no), 0) + 1 AS next_round FROM game_rounds WHERE game_type_id = ?",
                    rs -> rs.next() ? rs.getLong("next_round") : 1L,
                    gameTypeId
            );

            String inputJson = objectMapper.writeValueAsString(Map.of(
                    "taskCount", taskCount,
                    "matrix", matrix
            ));
            String expectedJson = objectMapper.writeValueAsString(Map.of("optimalCost", hungarianCost));

            String roundSql = "INSERT INTO game_rounds (game_type_id, round_no, round_input_json, expected_output_json) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(roundSql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, gameTypeId);
                ps.setLong(2, nextRoundNo == null ? 1L : nextRoundNo);
                ps.setString(3, inputJson);
                ps.setString(4, expectedJson);
                return ps;
            }, keyHolder);

            Number roundIdNumber = keyHolder.getKey();
            if (roundIdNumber == null) {
                return null;
            }
            long roundId = roundIdNumber.longValue();

            String greedyJson = objectMapper.writeValueAsString(Map.of(
                    "totalCost", greedyCost,
                    "taskCount", taskCount
            ));
            String hungarianJson = objectMapper.writeValueAsString(Map.of(
                    "totalCost", hungarianCost,
                    "taskCount", taskCount
            ));

            jdbcTemplate.update(
                    "INSERT OR REPLACE INTO algorithm_runs (game_round_id, algorithm_name, execution_time_ms, result_json) VALUES (?, ?, ?, ?)",
                    roundId, "Greedy Assignment", greedyExecutionMs, greedyJson
            );
            jdbcTemplate.update(
                    "INSERT OR REPLACE INTO algorithm_runs (game_round_id, algorithm_name, execution_time_ms, result_json) VALUES (?, ?, ?, ?)",
                    roundId, "Hungarian Algorithm", hungarianExecutionMs, hungarianJson
            );

            return roundId;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class LeaderboardStats {
        private final String playerName;
        private int gamesPlayed;
        private int correctAnswers;
        private int totalScore;
        private long totalTimeRemaining;
        private long lastTimestamp;

        private LeaderboardStats(String playerName) {
            this.playerName = playerName;
        }

        private int getTotalScore() {
            return totalScore;
        }

        private int getCorrectAnswers() {
            return correctAnswers;
        }

        private long getLastTimestamp() {
            return lastTimestamp;
        }

        private double getAccuracy() {
            if (gamesPlayed == 0) {
                return 0;
            }
            return ((double) correctAnswers / gamesPlayed) * 100.0;
        }

        private double getAverageTimeRemaining() {
            if (gamesPlayed == 0) {
                return 0;
            }
            return (double) totalTimeRemaining / gamesPlayed;
        }
    }
}