package com.nibm.pdsa.games.minimumcost;

import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.nibm.pdsa.games.minimumcost.algorithm.HungarianSolver;
import com.nibm.pdsa.games.minimumcost.model.AssignmentResult;

class HungarianSolverTest {

    private final HungarianSolver solver = new HungarianSolver();

    @Test
    void shouldMatchBruteForceOnThreeByThreeMatrix() {
        int[][] matrix = {
                {9, 2, 7},
                {6, 4, 3},
                {5, 8, 1}
        };

        AssignmentResult result = solver.solve(matrix);

        assertEquals(bruteForceOptimalCost(matrix), result.totalCost());
        assertTrue(isPermutation(result.assignment()));
    }

    @Test
    void shouldHandleSameCostsMatrix() {
        int[][] matrix = {
                {7, 7, 7, 7, 7},
                {7, 7, 7, 7, 7},
                {7, 7, 7, 7, 7},
                {7, 7, 7, 7, 7},
                {7, 7, 7, 7, 7}
        };

        AssignmentResult result = solver.solve(matrix);

        assertEquals(35, result.totalCost());
        assertTrue(isPermutation(result.assignment()));
    }

    @Test
    void shouldRunWithinExpectedBoundsForMediumMatrix() {
        int[][] matrix = new int[20][20];
        for (int row = 0; row < matrix.length; row += 1) {
            Arrays.fill(matrix[row], 50 + row);
        }

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> solver.solve(matrix));
    }

    private int bruteForceOptimalCost(int[][] matrix) {
        boolean[] used = new boolean[matrix.length];
        return bruteForce(matrix, 0, used);
    }

    private int bruteForce(int[][] matrix, int row, boolean[] used) {
        if (row == matrix.length) {
            return 0;
        }

        int best = Integer.MAX_VALUE;
        for (int task = 0; task < matrix.length; task += 1) {
            if (used[task]) {
                continue;
            }

            used[task] = true;
            int candidate = matrix[row][task] + bruteForce(matrix, row + 1, used);
            best = Math.min(best, candidate);
            used[task] = false;
        }

        return best;
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