package com.nibm.pdsa.games.minimumcost.algorithm;

import java.util.Arrays;

import com.nibm.pdsa.common.exception.BadRequestException;
import com.nibm.pdsa.games.minimumcost.model.AssignmentResult;

public class GreedyLocalOptimizationSolver {

    public AssignmentResult solve(int[][] costMatrix) {
        validateMatrix(costMatrix);

        long startedAt = System.nanoTime();
        int n = costMatrix.length;
        int[] assignment = new int[n];
        Arrays.fill(assignment, -1);
        boolean[] usedTasks = new boolean[n];

        for (int employee = 0; employee < n; employee += 1) {
            int chosenTask = -1;
            int chosenCost = Integer.MAX_VALUE;
            for (int task = 0; task < n; task += 1) {
                if (usedTasks[task]) {
                    continue;
                }
                int currentCost = costMatrix[employee][task];
                if (currentCost < chosenCost) {
                    chosenCost = currentCost;
                    chosenTask = task;
                }
            }

            if (chosenTask < 0) {
                throw new IllegalStateException("Greedy solver could not assign a task.");
            }

            assignment[employee] = chosenTask;
            usedTasks[chosenTask] = true;
        }

        boolean improved;
        do {
            improved = false;
            for (int first = 0; first < n; first += 1) {
                for (int second = first + 1; second < n; second += 1) {
                    int currentCost = costMatrix[first][assignment[first]] + costMatrix[second][assignment[second]];
                    int swappedCost = costMatrix[first][assignment[second]] + costMatrix[second][assignment[first]];
                    if (swappedCost < currentCost) {
                        int temp = assignment[first];
                        assignment[first] = assignment[second];
                        assignment[second] = temp;
                        improved = true;
                    }
                }
            }
        } while (improved);

        int totalCost = calculateCost(costMatrix, assignment);
        long elapsedMs = Math.max(1L, (System.nanoTime() - startedAt) / 1_000_000L);
        return new AssignmentResult("GREEDY_LOCAL_OPTIMIZATION", "GREEDY_WITH_SWAP_REFINEMENT", totalCost, elapsedMs, assignment);
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
                throw new IllegalStateException("Invalid assignment produced by greedy solver.");
            }
            total += costMatrix[employee][task];
        }
        return total;
    }
}