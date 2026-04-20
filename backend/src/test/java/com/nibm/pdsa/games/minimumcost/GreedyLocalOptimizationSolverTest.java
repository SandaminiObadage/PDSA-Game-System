package com.nibm.pdsa.games.minimumcost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.nibm.pdsa.games.minimumcost.algorithm.GreedyLocalOptimizationSolver;
import com.nibm.pdsa.games.minimumcost.model.AssignmentResult;

class GreedyLocalOptimizationSolverTest {

    private final GreedyLocalOptimizationSolver solver = new GreedyLocalOptimizationSolver();

    @Test
    void shouldReturnValidAssignmentForSmallMatrix() {
        int[][] matrix = {
                {9, 2, 7},
                {6, 4, 3},
                {5, 8, 1}
        };

        AssignmentResult result = solver.solve(matrix);

        assertTrue(isPermutation(result.assignment()));
        assertEquals(3, result.assignment().length);
    }

    @Test
    void shouldHandleSameCostsMatrix() {
        int[][] matrix = {
                {5, 5, 5},
                {5, 5, 5},
                {5, 5, 5}
        };

        AssignmentResult result = solver.solve(matrix);

        assertEquals(15, result.totalCost());
        assertTrue(isPermutation(result.assignment()));
    }

    private boolean isPermutation(int[] assignment) {
        boolean[] seen = new boolean[assignment.length];
        for (int task : assignment) {
            if (task < 0 || task >= assignment.length || seen[task]) {
                return false;
            }
            seen[task] = true;
        }
        return true;
    }
}