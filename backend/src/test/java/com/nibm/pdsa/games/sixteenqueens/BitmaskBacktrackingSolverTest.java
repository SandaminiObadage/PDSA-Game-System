package com.nibm.pdsa.games.sixteenqueens;

import com.nibm.pdsa.games.sixteenqueens.algorithm.BitmaskBacktrackingSolver;
import com.nibm.pdsa.games.sixteenqueens.model.QueensSolveResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitmaskBacktrackingSolverTest {

    private final BitmaskBacktrackingSolver solver = new BitmaskBacktrackingSolver();

    @Test
    void shouldReturn2SolutionsFor4QueensEquivalentUsing8RestrictionByValidation() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> solver.solveSequential(4, 10));
        assertTrue(ex.getMessage().contains("between 8 and 16"));
    }

    @Test
    void shouldReturn92SolutionsFor8Queens() {
        QueensSolveResult result = solver.solveSequential(8, 10);
        assertEquals(92, result.getSolutionCount());
        assertFalse(result.getSampleSolutions().isEmpty());
    }

    @Test
    void parallelAndSequentialShouldMatchFor8Queens() {
        QueensSolveResult sequential = solver.solveSequential(8, 20);
        QueensSolveResult parallel = solver.solveParallel(8, 4, 20);
        assertEquals(sequential.getSolutionCount(), parallel.getSolutionCount());
    }

    @Test
    void shouldValidateCorrectAndIncorrectSolutions() {
        int[] valid = solver.parseAnswer("0,4,7,5,2,6,1,3", 8);
        assertTrue(solver.isValidSolution(8, valid));

        int[] invalid = solver.parseAnswer("0,0,0,0,0,0,0,0", 8);
        assertFalse(solver.isValidSolution(8, invalid));
    }
}
