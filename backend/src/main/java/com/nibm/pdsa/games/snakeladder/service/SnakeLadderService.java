package com.nibm.pdsa.games.snakeladder.service;

import com.nibm.pdsa.common.exception.BadRequestException;
import com.nibm.pdsa.games.snakeladder.algorithm.BfsShortestPathSolver;
import com.nibm.pdsa.games.snakeladder.algorithm.DpRelaxationSolver;
import com.nibm.pdsa.games.snakeladder.dto.SolveComparisonResponse;
import com.nibm.pdsa.games.snakeladder.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.snakeladder.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.snakeladder.model.Board;
import com.nibm.pdsa.games.snakeladder.model.Ladder;
import com.nibm.pdsa.games.snakeladder.model.Snake;
import com.nibm.pdsa.games.snakeladder.model.SnakeLadderSolveResult;
import com.nibm.pdsa.games.snakeladder.repository.SnakeLadderRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SnakeLadderService {

    private static final String GAME_CODE = "SNAKE_LADDER";

    private final BfsShortestPathSolver bfsSolver = new BfsShortestPathSolver();
    private final DpRelaxationSolver dpSolver = new DpRelaxationSolver();
    private final SnakeLadderRepository repository;

    public SnakeLadderService(SnakeLadderRepository repository) {
        this.repository = repository;
    }

    public SolveComparisonResponse runComparison(int boardSize) {
        if (boardSize < 6 || boardSize > 12) {
            throw new BadRequestException("Board size must be between 6 and 12");
        }

        // Generate random ladders and snakes
        List<Ladder> ladders = generateLadders(boardSize);
        List<Snake> snakes = generateSnakes(boardSize);

        Board board = new Board(boardSize, ladders, snakes);

        // Run BFS
        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);

        // Run DP
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        // Both should give same result
        if (bfsResult.getMinThrows() != dpResult.getMinThrows()) {
            throw new IllegalStateException("Algorithms gave different results");
        }

        SolveComparisonResponse response = new SolveComparisonResponse();
        response.setBoardSize(boardSize);
        response.setLadders(ladders);
        response.setSnakes(snakes);
        response.setBfsMinThrows(bfsResult.getMinThrows());
        response.setDpMinThrows(dpResult.getMinThrows());
        response.setBfsTimeMs(bfsResult.getElapsedMs());
        response.setDpTimeMs(dpResult.getElapsedMs());

        // Generate 3 choices: correct + 2 random nearby
        List<Integer> choices = generateChoices(bfsResult.getMinThrows());
        response.setChoices(choices);

        if (repository != null) {
            long gameTypeId = getGameTypeId();
            long roundNo = repository.getNextRoundNo(gameTypeId);
            String roundInputJson = String.format("{\"boardSize\":%d,\"ladders\":%s,\"snakes\":%s}",
                    boardSize, laddersToJson(ladders), snakesToJson(snakes));
            String expectedOutputJson = String.valueOf(bfsResult.getMinThrows());
            long gameRoundId = repository.createGameRound(gameTypeId, roundNo, roundInputJson, expectedOutputJson);

            repository.insertAlgorithmRun(
                    gameRoundId,
                    "BFS_SHORTEST_PATH",
                    "GRAPH_BFS",
                    bfsResult.getElapsedMs(),
                    String.format("{\"minThrows\":%d}", bfsResult.getMinThrows()));

            repository.insertAlgorithmRun(
                    gameRoundId,
                    "DP_RELAXATION",
                    "ITERATIVE_RELAXATION",
                    dpResult.getElapsedMs(),
                    String.format("{\"minThrows\":%d}", dpResult.getMinThrows()));

            response.setGameRoundId(gameRoundId);
        }

        return response;
    }

    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        // Get the correct answer from DB
        String expected = repository != null ? getExpectedAnswer(request.getGameRoundId()) : null;
        if (expected == null) {
            throw new BadRequestException("Invalid game round");
        }
        int correctAnswer = Integer.parseInt(expected);

        boolean isCorrect = request.getAnswer().equals(correctAnswer);

        SubmitAnswerResponse response = new SubmitAnswerResponse();
        response.setCorrect(isCorrect);
        response.setCorrectAnswer(correctAnswer);
        response.setMessage(isCorrect ? "Correct! Well done." : "Incorrect. Try again.");

        if (repository != null && isCorrect) {
            long playerId = repository.ensurePlayerAndGetId(request.getPlayerName());
            repository.insertPlayerAnswer(request.getGameRoundId(), playerId, request.getAnswer().toString(), true);
        }

        return response;
    }

    private long getGameTypeId() {
        Long id = repository.findGameTypeIdByCode(GAME_CODE);
        if (id == null) {
            throw new IllegalStateException("Game type not found: " + GAME_CODE);
        }
        return id;
    }

    private String getExpectedAnswer(Long gameRoundId) {
        if (repository != null) {
            return repository.getExpectedAnswer(gameRoundId);
        }
        // For testing without DB
        return null;
    }

    private List<Ladder> generateLadders(int n) {
        int numLadders = n - 2;
        List<Ladder> ladders = new ArrayList<>();
        Set<Integer> usedCells = new HashSet<>();
        Random rand = new Random();

        for (int i = 0; i < numLadders; i++) {
            int start, end;
            do {
                start = rand.nextInt(n * n - 3) + 2; // 2 to n*n-2 (to leave room for end)
                end = rand.nextInt(n * n - start - 1) + start + 1; // start+1 to n*n-1
            } while (usedCells.contains(start) || usedCells.contains(end) || end == n * n);

            ladders.add(new Ladder(start, end));
            usedCells.add(start);
            usedCells.add(end);
        }

        return ladders;
    }

    private List<Snake> generateSnakes(int n) {
        int numSnakes = n - 2;
        List<Snake> snakes = new ArrayList<>();
        Set<Integer> usedCells = new HashSet<>();
        Random rand = new Random();

        for (int i = 0; i < numSnakes; i++) {
            int head, tail;
            do {
                head = rand.nextInt(n * n - 3) + 3; // 3 to n*n-1
                tail = rand.nextInt(head - 1) + 1; // 1 to head-1
            } while (usedCells.contains(head) || usedCells.contains(tail) || head == n * n || tail == 1);

            snakes.add(new Snake(head, tail));
            usedCells.add(head);
            usedCells.add(tail);
        }

        return snakes;
    }

    private List<Integer> generateChoices(int correct) {
        List<Integer> choices = new ArrayList<>();
        choices.add(correct);

        Random rand = new Random();
        Set<Integer> used = new HashSet<>();
        used.add(correct);

        while (choices.size() < 3) {
            int offset = rand.nextInt(5) - 2; // -2 to 2
            int choice = correct + offset;
            if (choice > 0 && !used.contains(choice)) {
                choices.add(choice);
                used.add(choice);
            }
        }

        Collections.shuffle(choices);
        return choices;
    }

    private String laddersToJson(List<Ladder> ladders) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ladders.size(); i++) {
            Ladder l = ladders.get(i);
            sb.append(String.format("{\"start\":%d,\"end\":%d}", l.getStart(), l.getEnd()));
            if (i < ladders.size() - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String snakesToJson(List<Snake> snakes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < snakes.size(); i++) {
            Snake s = snakes.get(i);
            sb.append(String.format("{\"head\":%d,\"tail\":%d}", s.getHead(), s.getTail()));
            if (i < snakes.size() - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public com.nibm.pdsa.games.snakeladder.dto.SnakeLadderLeaderboardResponse getLeaderboard(int limit,
            Long gameRoundId) {
        if (repository == null) {
            throw new BadRequestException("Database leaderboard is not available in in-memory mode.");
        }

        long gameTypeId = getGameTypeId();
        Long effectiveRoundId = gameRoundId != null ? gameRoundId : repository.findLatestRoundId(gameTypeId);
        com.nibm.pdsa.games.snakeladder.dto.SnakeLadderLeaderboardResponse response = new com.nibm.pdsa.games.snakeladder.dto.SnakeLadderLeaderboardResponse();
        response.setGameTypeId(gameTypeId);
        response.setGameCode(GAME_CODE);
        response.setRoundId(effectiveRoundId);
        if (effectiveRoundId == null) {
            response.setLeaderboard(List.of());
            return response;
        }

        response.setLeaderboard(repository.findLeaderboard(gameTypeId, limit, effectiveRoundId));
        return response;
    }
}