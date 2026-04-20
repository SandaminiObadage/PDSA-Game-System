package com.nibm.pdsa.games.snakeladder;

import com.nibm.pdsa.games.snakeladder.algorithm.BfsShortestPathSolver;
import com.nibm.pdsa.games.snakeladder.algorithm.DpRelaxationSolver;
import com.nibm.pdsa.games.snakeladder.model.Board;
import com.nibm.pdsa.games.snakeladder.model.Ladder;
import com.nibm.pdsa.games.snakeladder.model.Snake;
import com.nibm.pdsa.games.snakeladder.model.SnakeLadderSolveResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnakeLadderAlgorithmTest {

    private final BfsShortestPathSolver bfsSolver = new BfsShortestPathSolver();
    private final DpRelaxationSolver dpSolver = new DpRelaxationSolver();

    @Test
    void testSimpleBoard() {
        // 2x2 board: cells 1,2,3,4
        List<Ladder> ladders = Arrays.asList(new Ladder(2, 4));
        List<Snake> snakes = Arrays.asList();
        Board board = new Board(2, ladders, snakes);

        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        assertEquals(2, bfsResult.getMinThrows()); // 1->2(dice1)->4(ladder)
        assertEquals(2, dpResult.getMinThrows());
        assertTrue(bfsResult.getElapsedMs() >= 0);
        assertTrue(dpResult.getElapsedMs() >= 0);
    }

    @Test
    void testBoardWithSnake() {
        // 2x2 board with snake
        List<Ladder> ladders = Arrays.asList();
        List<Snake> snakes = Arrays.asList(new Snake(3, 1));
        Board board = new Board(2, ladders, snakes);

        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        assertEquals(2, bfsResult.getMinThrows()); // 1->2(dice1)->3(snake)->1, then 1->2->3->1, etc. Wait, better path:
                                                   // 1->3(dice2)->1, then 1->4(dice3)
        // Actually for 2x2: 1->4 with dice 3, or 1->2->4 with dice1+1=2 throws
        assertEquals(2, bfsResult.getMinThrows());
        assertEquals(2, dpResult.getMinThrows());
    }

    @Test
    void testLargerBoard() {
        // 3x3 board
        List<Ladder> ladders = Arrays.asList(new Ladder(2, 6), new Ladder(4, 8));
        List<Snake> snakes = Arrays.asList(new Snake(7, 3));
        Board board = new Board(3, ladders, snakes);

        SnakeLadderSolveResult bfsResult = bfsSolver.solve(board);
        SnakeLadderSolveResult dpResult = dpSolver.solve(board);

        assertEquals(bfsResult.getMinThrows(), dpResult.getMinThrows());
        assertTrue(bfsResult.getMinThrows() > 0);
    }
}