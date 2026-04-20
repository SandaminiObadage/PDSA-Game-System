package com.nibm.pdsa.games.minimumcost.service;

import com.nibm.pdsa.games.minimumcost.algorithm.AlgorithmsService;
import com.nibm.pdsa.games.minimumcost.model.Player;
import com.nibm.pdsa.games.minimumcost.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MinimumCostService {

    private final AlgorithmsService algorithmsService;
    private final PlayerRepository playerRepository;

    public MinimumCostService(
            AlgorithmsService algorithmsService,
            PlayerRepository playerRepository
    ) {
        this.algorithmsService = algorithmsService;
        this.playerRepository = playerRepository;
    }

    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Backend is running!");
        response.put("version", "1.0");
        response.put("game", "Minimum Cost Assignment");
        return response;
    }

    public Map<String, Object> getGameData() {
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

    public Map<String, Object> saveGameResult(Player gameResult) {
        if (gameResult.getTimestamp() == 0) {
            gameResult.setTimestamp(System.currentTimeMillis());
        }

        Player saved = playerRepository.save(gameResult);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", saved.getId());
        response.put("isCorrect", saved.isCorrect());
        response.put("correctCost", saved.getCorrectCost());
        response.put("selectedCost", saved.getSelectedCost());
        response.put("message",
                saved.isCorrect()
                        ? "Correct! Result saved."
                        : "Wrong answer. Result saved.");

        return response;
    }

    public Map<String, Object> getLeaderboard(int limit) {
        List<Player> results = playerRepository.findAllWinningResults();

        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("total", results.size());
        return response;
    }

    public Map<String, Object> getAllResults(int limit) {
        List<Player> results = playerRepository.findAllResults();

        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("total", results.size());
        return response;
    }

    public Map<String, Object> getPlayerHistory(String playerName, int limit) {
        List<Player> results = playerRepository.findByPlayerNameOrderByTimestampDesc(playerName);

        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        long correctCount = results.stream().filter(Player::isCorrect).count();
        long totalGames = results.size();

        Map<String, Object> response = new HashMap<>();
        response.put("playerName", playerName);
        response.put("results", results);
        response.put("totalGames", totalGames);
        response.put("correctAnswers", correctCount);
        response.put("accuracy",
                totalGames == 0 ? "0%" :
                        String.format("%.1f%%", (correctCount * 100.0) / totalGames));

        return response;
    }

    private int[][] generateMatrix(int n) {
        Random random = new Random();
        int[][] matrix = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = random.nextInt(90) + 10;
            }
        }

        return matrix;
    }
}