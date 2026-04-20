package com.nibm.pdsa.games.snakeladder.algorithm;

import com.nibm.pdsa.games.snakeladder.model.Board;
import com.nibm.pdsa.games.snakeladder.model.SnakeLadderSolveResult;

import java.util.LinkedList;
import java.util.Queue;

public class BfsShortestPathSolver {

    public SnakeLadderSolveResult solve(Board board) {
        long startTime = System.nanoTime();

        int n = board.getN();
        int target = n * n;
        boolean[] visited = new boolean[target + 1];
        int[] throwsToReach = new int[target + 1];

        Queue<Integer> queue = new LinkedList<>();
        queue.add(1);
        visited[1] = true;
        throwsToReach[1] = 0;

        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == target) {
                break;
            }

            // Try all possible dice rolls (1-6)
            for (int dice = 1; dice <= 6; dice++) {
                int next = current + dice;
                if (next > target)
                    continue;

                // Apply ladder/snake if present
                next = board.getDestination(next);

                if (!visited[next]) {
                    visited[next] = true;
                    throwsToReach[next] = throwsToReach[current] + 1;
                    queue.add(next);
                }
            }
        }

        long endTime = System.nanoTime();
        long elapsedMs = (endTime - startTime) / 1_000_000;

        return new SnakeLadderSolveResult(throwsToReach[target], elapsedMs);
    }
}