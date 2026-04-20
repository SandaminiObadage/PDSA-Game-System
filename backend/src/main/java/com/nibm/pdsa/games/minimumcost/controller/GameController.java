package com.nibm.pdsa.games.minimumcost.controller;

import com.nibm.pdsa.games.minimumcost.algorithm.AlgorithmsService;
import com.nibm.pdsa.games.minimumcost.model.Player;
import com.nibm.pdsa.games.minimumcost.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/games/minimum-cost")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class GameController {

    @Autowired
    private AlgorithmsService algorithmsService;
    
    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Backend is running!");
        response.put("version", "1.0");
        response.put("game", "Minimum Cost Assignment");
        return response;
    }

    @GetMapping("/game")
    public Map<String, Object> getGameData() {
        // Generate a 4x4 matrix
        int[][] matrix = generateMatrix(4);

        int greedyCost = algorithmsService.greedy(matrix);
        int hungarianCost = algorithmsService.hungarian(matrix);

        Map<String, Object> response = new HashMap<>();
        response.put("matrix", matrix);
        response.put("greedyCost", greedyCost);
        response.put("hungarianCost", hungarianCost);
        response.put("matrixSize", 4);

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
        
        List<Player> winningResults = playerRepository.findAllWinningResults();
        
        // Limit the results
        if (winningResults.size() > limit) {
            winningResults = winningResults.subList(0, limit);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", winningResults);
        response.put("total", winningResults.size());
        response.put("message", "Top " + limit + " winning results");
        
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
        Random rand = new Random();
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = rand.nextInt(90) + 10; // 10-99
            }
        }
        return matrix;
    }
}