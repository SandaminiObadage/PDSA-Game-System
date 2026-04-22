package com.nibm.pdsa.games.snakeladder;

import com.nibm.pdsa.games.snakeladder.algorithm.BfsShortestPathSolver;
import com.nibm.pdsa.games.snakeladder.algorithm.DpRelaxationSolver;
import com.nibm.pdsa.games.snakeladder.model.Board;
import com.nibm.pdsa.games.snakeladder.model.Ladder;
import com.nibm.pdsa.games.snakeladder.model.Snake;
import com.nibm.pdsa.games.snakeladder.model.SnakeLadderSolveResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SnakeLadderAlgorithmTest {

    private final BfsShortestPathSolver bfsSolver = new BfsShortestPathSolver();
    private final DpRelaxationSolver dpSolver = new DpRelaxationSolver();

    private Board createValidBoard() {
        List<Ladder> ladders = new ArrayList<>();
        ladders.add(new Ladder(2, 15));
        ladders.add(new Ladder(6, 20));
        ladders.add(new Ladder(9, 24));
        ladders.add(new Ladder(13, 30));

        List<Snake> snakes = new ArrayList<>();
        snakes.add(new Snake(34, 14));
        snakes.add(new Snake(31, 10));
        snakes.add(new Snake(29, 7));
        snakes.add(new Snake(27, 5));

        return new Board(6, ladders, snakes);
    }

    @Test
    void testSimpleBoard() {
        Board board = createValidBoard();

        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        assertEquals(bfsResult.getMinThrows(), dpResult.getMinThrows());
        assertTrue(bfsResult.getMinThrows() > 0);
        assertTrue(bfsResult.getElapsedMs() >= 0);
        assertTrue(dpResult.getElapsedMs() >= 0);
    }

    @Test
    void testBoardWithSnake() {
        Board board = createValidBoard();

        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        assertEquals(bfsResult.getMinThrows(), dpResult.getMinThrows());
        assertTrue(bfsResult.getMinThrows() > 0);
    }

    @Test
    void testLargerBoard() {
        Board board = createValidBoard();

        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        assertEquals(bfsResult.getMinThrows(), dpResult.getMinThrows());
        assertTrue(bfsResult.getMinThrows() > 0);
    }
}