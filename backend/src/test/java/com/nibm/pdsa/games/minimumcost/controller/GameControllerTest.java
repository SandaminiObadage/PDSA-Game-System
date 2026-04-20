package com.nibm.pdsa.games.minimumcost.controller;

import com.nibm.pdsa.games.minimumcost.algorithm.AlgorithmsService;
import com.nibm.pdsa.games.minimumcost.model.Player;
import com.nibm.pdsa.games.minimumcost.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("GameController Tests")
class GameControllerTest {

    private GameController gameController;
    private AlgorithmsService algorithmsService;
    private PlayerRepository playerRepository;

    private int[][] testMatrix;

    @BeforeEach
    void setUp() {
        algorithmsService = mock(AlgorithmsService.class);
        playerRepository = mock(PlayerRepository.class);
        
        gameController = new GameController();
            ReflectionTestUtils.setField(gameController, "algorithmsService", algorithmsService);
            ReflectionTestUtils.setField(gameController, "playerRepository", playerRepository);
        
        testMatrix = new int[][] {
            {10, 20, 30, 40},
            {15, 25, 35, 45},
            {20, 30, 40, 50},
            {25, 35, 45, 55}
        };
    }


    @Test
    @DisplayName("health() should return health status")
    void testHealthCheck() {
        Map<String, Object> response = gameController.health();
        
        assertNotNull(response);
        assertEquals("Backend is running!", response.get("status"));
        assertEquals("1.0", response.get("version"));
        assertEquals("Minimum Cost Assignment", response.get("game"));
    }

    @Test
    @DisplayName("getGameData() should return valid game data")
    void testGetGameData() {
        int greedyCost = 50;
        int hungarianCost = 40;

        when(algorithmsService.greedy(any(int[][].class))).thenReturn(greedyCost);
        when(algorithmsService.hungarian(any(int[][].class))).thenReturn(hungarianCost);

        Map<String, Object> response = gameController.getGameData();
        
        assertNotNull(response);
        assertTrue(response.containsKey("matrix"));
        assertTrue(response.containsKey("greedyCost"));
        assertTrue(response.containsKey("hungarianCost"));
        assertTrue(response.containsKey("matrixSize"));
        
        assertEquals(greedyCost, response.get("greedyCost"));
        assertEquals(hungarianCost, response.get("hungarianCost"));
        assertEquals(4, response.get("matrixSize"));

        verify(algorithmsService, times(1)).greedy(any(int[][].class));
        verify(algorithmsService, times(1)).hungarian(any(int[][].class));
    }

    @Test
    @DisplayName("getGameData() should return 4x4 matrix")
    void testGameMatrixDimensions() {
        when(algorithmsService.greedy(any(int[][].class))).thenReturn(50);
        when(algorithmsService.hungarian(any(int[][].class))).thenReturn(40);

        Map<String, Object> response = gameController.getGameData();
        int[][] matrix = (int[][]) response.get("matrix");
        
        assertNotNull(matrix);
        assertEquals(4, matrix.length);
        for (int[] row : matrix) {
            assertEquals(4, row.length);
        }
    }

    @Test
    @DisplayName("getGameData() matrix values should be within 10-99 range")
    void testGameMatrixValuesInRange() {
        when(algorithmsService.greedy(any(int[][].class))).thenReturn(50);
        when(algorithmsService.hungarian(any(int[][].class))).thenReturn(40);

        Map<String, Object> response = gameController.getGameData();
        int[][] matrix = (int[][]) response.get("matrix");
        
        assertNotNull(matrix);
        for (int[] row : matrix) {
            for (int value : row) {
                assertTrue(value >= 10 && value <= 99, 
                    "Matrix value " + value + " is out of range [10, 99]");
            }
        }
    }

    @Test
    @DisplayName("saveGameResult() should save correct answer")
    void testSaveCorrectGameResult() {
        Player inputPlayer = new Player("TestPlayer", 40, 40, 120, true);
        Player savedPlayer = new Player("TestPlayer", 40, 40, 120, true);
        savedPlayer.setId(1L);

        when(playerRepository.save(any())).thenReturn(savedPlayer);

        Map<String, Object> response = gameController.saveGameResult(inputPlayer);
        
        assertNotNull(response);
        assertTrue((boolean) response.get("success"));
        assertEquals(1L, response.get("id"));
        assertTrue((boolean) response.get("isCorrect"));
        assertTrue(response.get("message").toString().contains("Correct"));
        assertEquals(40, response.get("correctCost"));
        assertEquals(40, response.get("selectedCost"));

        verify(playerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("saveGameResult() should save incorrect answer")
    void testSaveIncorrectGameResult() {
        Player inputPlayer = new Player("TestPlayer", 40, 50, 120, false);
        Player savedPlayer = new Player("TestPlayer", 40, 50, 120, false);
        savedPlayer.setId(2L);

        when(playerRepository.save(any())).thenReturn(savedPlayer);

        Map<String, Object> response = gameController.saveGameResult(inputPlayer);
        
        assertNotNull(response);
        assertTrue((boolean) response.get("success"));
        assertEquals(2L, response.get("id"));
        assertFalse((boolean) response.get("isCorrect"));
        assertTrue(response.get("message").toString().contains("Wrong"));
        assertEquals(40, response.get("correctCost"));
        assertEquals(50, response.get("selectedCost"));

        verify(playerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("saveGameResult() should set timestamp if not provided")
    void testSaveGameResultSetsTimestamp() {
        Player inputPlayer = new Player("TestPlayer", 40, 40, 120, true);
        inputPlayer.setTimestamp(0);

        Player savedPlayer = new Player("TestPlayer", 40, 40, 120, true);
        savedPlayer.setId(1L);
        savedPlayer.setTimestamp(System.currentTimeMillis());

        when(playerRepository.save(any())).thenReturn(savedPlayer);

        Map<String, Object> response = gameController.saveGameResult(inputPlayer);
        
        assertTrue((boolean) response.get("success"));
        verify(playerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("getLeaderboard() should return top winning results")
    void testGetLeaderboard() {
        List<Player> winners = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Player player = new Player("Winner" + i, 40, 40, 100 + i * 10, true);
            player.setId((long) i);
            winners.add(player);
        }

        when(playerRepository.findAllWinningResults()).thenReturn(winners);

        Map<String, Object> response = gameController.getLeaderboard(20);
        
        assertNotNull(response);
        assertTrue(response.containsKey("results"));
        assertTrue(response.containsKey("total"));
        assertTrue(response.containsKey("message"));
        
        assertEquals(5, response.get("total"));
        List<?> results = (List<?>) response.get("results");
        assertEquals(5, results.size());

        verify(playerRepository, times(1)).findAllWinningResults();
    }

    @Test
    @DisplayName("getLeaderboard() should respect limit parameter")
    void testGetLeaderboardWithLimit() {
        List<Player> winners = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Player player = new Player("Winner" + i, 40, 40, 100, true);
            player.setId((long) i);
            winners.add(player);
        }

        when(playerRepository.findAllWinningResults()).thenReturn(winners);

        Map<String, Object> response = gameController.getLeaderboard(10);
        
        assertNotNull(response);
        List<?> results = (List<?>) response.get("results");
        assertEquals(10, results.size());
        assertEquals(10, response.get("total"));

        verify(playerRepository, times(1)).findAllWinningResults();
    }

    @Test
    @DisplayName("getLeaderboard() should return empty list when no results")
    void testGetLeaderboardEmpty() {
        when(playerRepository.findAllWinningResults()).thenReturn(new ArrayList<>());

        Map<String, Object> response = gameController.getLeaderboard(20);
        
        assertNotNull(response);
        List<?> results = (List<?>) response.get("results");
        assertEquals(0, results.size());
        assertEquals(0, response.get("total"));

        verify(playerRepository, times(1)).findAllWinningResults();
    }

    @Test
    @DisplayName("getAllResults() should return all game results")
    void testGetAllResults() {
        List<Player> allResults = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Player player = new Player("Player" + i, 40, 40 + i * 5, 100, i % 2 == 0);
            player.setId((long) i);
            allResults.add(player);
        }

        when(playerRepository.findAllResults()).thenReturn(allResults);

        Map<String, Object> response = gameController.getAllResults(100);
        
        assertNotNull(response);
        assertTrue(response.containsKey("results"));
        assertTrue(response.containsKey("total"));
        
        assertEquals(3, response.get("total"));
        List<?> results = (List<?>) response.get("results");
        assertEquals(3, results.size());

        verify(playerRepository, times(1)).findAllResults();
    }

    @Test
    @DisplayName("getAllResults() should respect limit parameter")
    void testGetAllResultsWithLimit() {
        List<Player> allResults = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Player player = new Player("Player" + i, 40, 40, 100, true);
            player.setId((long) i);
            allResults.add(player);
        }

        when(playerRepository.findAllResults()).thenReturn(allResults);

        Map<String, Object> response = gameController.getAllResults(25);
        
        assertNotNull(response);
        List<?> results = (List<?>) response.get("results");
        assertEquals(25, results.size());
        assertEquals(25, response.get("total"));

        verify(playerRepository, times(1)).findAllResults();
    }

    @Test
    @DisplayName("getPlayerHistory() should return player history with stats")
    void testGetPlayerHistory() {
        String playerName = "TestPlayer";
        List<Player> results = new ArrayList<>();
        
        for (int i = 1; i <= 7; i++) {
            Player player = new Player(playerName, 40, 40, 100, true);
            player.setId((long) i);
            results.add(player);
        }
        for (int i = 8; i <= 10; i++) {
            Player player = new Player(playerName, 40, 50, 100, false);
            player.setId((long) i);
            results.add(player);
        }

        when(playerRepository.findByPlayerNameOrderByTimestampDesc(playerName)).thenReturn(results);

        Map<String, Object> response = gameController.getPlayerHistory(playerName, 10);
        
        assertNotNull(response);
        assertEquals(playerName, response.get("playerName"));
        
        List<?> playerResults = (List<?>) response.get("results");
        assertEquals(10, playerResults.size());
        assertEquals(10L, response.get("totalGames"));
        assertEquals(7L, response.get("correctAnswers"));
        
        String accuracy = (String) response.get("accuracy");
        assertTrue(accuracy.contains("70"));

        verify(playerRepository, times(1)).findByPlayerNameOrderByTimestampDesc(playerName);
    }

    @Test
    @DisplayName("getPlayerHistory() should respect limit parameter")
    void testGetPlayerHistoryWithLimit() {
        String playerName = "TestPlayer";
        List<Player> results = new ArrayList<>();
        
        for (int i = 1; i <= 25; i++) {
            Player player = new Player(playerName, 40, 40, 100, true);
            player.setId((long) i);
            results.add(player);
        }

        when(playerRepository.findByPlayerNameOrderByTimestampDesc(playerName)).thenReturn(results);

        Map<String, Object> response = gameController.getPlayerHistory(playerName, 5);
        
        assertNotNull(response);
        List<?> playerResults = (List<?>) response.get("results");
        assertEquals(5, playerResults.size());
        assertEquals(5L, response.get("totalGames"));
        assertEquals(5L, response.get("correctAnswers"));
        
        String accuracy = (String) response.get("accuracy");
        assertEquals("100.0%", accuracy);

        verify(playerRepository, times(1)).findByPlayerNameOrderByTimestampDesc(playerName);
    }

    @Test
    @DisplayName("getPlayerHistory() should calculate 0% accuracy when all wrong")
    void testGetPlayerHistoryAllWrong() {
        String playerName = "BadPlayer";
        List<Player> results = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Player player = new Player(playerName, 40, 50, 100, false);
            player.setId((long) i);
            results.add(player);
        }

        when(playerRepository.findByPlayerNameOrderByTimestampDesc(playerName)).thenReturn(results);

        Map<String, Object> response = gameController.getPlayerHistory(playerName, 10);
        
        assertNotNull(response);
        assertEquals(0L, response.get("correctAnswers"));
        
        String accuracy = (String) response.get("accuracy");
        assertEquals("0.0%", accuracy);

        verify(playerRepository, times(1)).findByPlayerNameOrderByTimestampDesc(playerName);
    }

    @Test
    @DisplayName("getPlayerHistory() should handle empty history")
    void testGetPlayerHistoryEmpty() {
        String playerName = "NewPlayer";

        when(playerRepository.findByPlayerNameOrderByTimestampDesc(playerName)).thenReturn(new ArrayList<>());

        Map<String, Object> response = gameController.getPlayerHistory(playerName, 10);
        
        assertNotNull(response);
        assertEquals(playerName, response.get("playerName"));
        
        List<?> results = (List<?>) response.get("results");
        assertEquals(0, results.size());
        assertEquals(0L, response.get("totalGames"));
        assertEquals(0L, response.get("correctAnswers"));
        
        String accuracy = (String) response.get("accuracy");
            assertEquals("0.0%", accuracy);

        verify(playerRepository, times(1)).findByPlayerNameOrderByTimestampDesc(playerName);
    }
}