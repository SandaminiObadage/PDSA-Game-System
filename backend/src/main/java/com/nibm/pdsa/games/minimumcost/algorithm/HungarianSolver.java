package com.nibm.pdsa.games.minimumcost.algorithm;

import java.util.Arrays;

import com.nibm.pdsa.common.exception.BadRequestException;
import com.nibm.pdsa.games.minimumcost.model.AssignmentResult;

public class HungarianSolver {

    public AssignmentResult solve(int[][] costMatrix) {
        validateMatrix(costMatrix);

        long startedAt = System.nanoTime();
        int n = costMatrix.length;

        int[] leftPotentials = new int[n + 1];
        int[] rightPotentials = new int[n + 1];
        int[] matching = new int[n + 1];
        int[] predecessor = new int[n + 1];

        for (int left = 1; left <= n; left += 1) {
            matching[0] = left;
            int currentRight = 0;
            int[] minDistance = new int[n + 1];
            boolean[] used = new boolean[n + 1];
            Arrays.fill(minDistance, Integer.MAX_VALUE);

            do {
                used[currentRight] = true;
                int currentLeft = matching[currentRight];
                int delta = Integer.MAX_VALUE;
                int nextRight = 0;

                for (int right = 1; right <= n; right += 1) {
                    if (used[right]) {
                        continue;
                    }

                    int currentCost = costMatrix[currentLeft - 1][right - 1] - leftPotentials[currentLeft] - rightPotentials[right];
                    if (currentCost < minDistance[right]) {
                        minDistance[right] = currentCost;
                        predecessor[right] = currentRight;
                    }

                    if (minDistance[right] < delta) {
                        delta = minDistance[right];
                        nextRight = right;
                    }
                }

                for (int right = 0; right <= n; right += 1) {
                    if (used[right]) {
                        leftPotentials[matching[right]] += delta;
                        rightPotentials[right] -= delta;
                    } else {
                        minDistance[right] -= delta;
                    }
                }

                currentRight = nextRight;
            } while (matching[currentRight] != 0);

            do {
                int previousRight = predecessor[currentRight];
                matching[currentRight] = matching[previousRight];
                currentRight = previousRight;
            } while (currentRight != 0);
        }

        int[] assignment = new int[n];
        Arrays.fill(assignment, -1);
        for (int right = 1; right <= n; right += 1) {
            int left = matching[right];
            if (left > 0) {
                assignment[left - 1] = right - 1;
            }
        }

        int totalCost = calculateCost(costMatrix, assignment);
        long elapsedMs = Math.max(1L, (System.nanoTime() - startedAt) / 1_000_000L);
        return new AssignmentResult("HUNGARIAN", "OPTIMAL", totalCost, elapsedMs, assignment);
    }

    private void validateMatrix(int[][] costMatrix) {
        if (costMatrix == null || costMatrix.length == 0) {
            throw new BadRequestException("Cost matrix must not be empty.");
        }

        int n = costMatrix.length;
        for (int[] row : costMatrix) {
            if (row == null || row.length != n) {
                throw new BadRequestException("Cost matrix must be square.");
            }
        }
    }

    private int calculateCost(int[][] costMatrix, int[] assignment) {
        int total = 0;
        for (int employee = 0; employee < assignment.length; employee += 1) {
            int task = assignment[employee];
            if (task < 0 || task >= assignment.length) {
                throw new IllegalStateException("Invalid assignment produced by Hungarian solver.");
            }
            total += costMatrix[employee][task];
        }
        return total;
    }
}