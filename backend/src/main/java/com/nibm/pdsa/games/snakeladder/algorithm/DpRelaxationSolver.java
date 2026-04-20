package com.nibm.pdsa.games.snakeladder.algorithm;

import com.nibm.pdsa.games.snakeladder.model.Board;
import com.nibm.pdsa.games.snakeladder.model.SnakeLadderSolveResult;

public class DpRelaxationSolver {

    public SnakeLadderSolveResult solve(Board board) {
        long startTime = System.nanoTime();

        int n = board.getN();
        int target = n * n;
        int[] minThrows = new int[target + 1];
        for (int i = 0; i <= target; i++) {
            minThrows[i] = Integer.MAX_VALUE;
        }
        minThrows[1] = 0;

        // Relax all edges repeatedly
        boolean updated = true;
        while (updated) {
            updated = false;
            for (int current = 1; current <= target; current++) {
                if (minThrows[current] == Integer.MAX_VALUE)
                    continue;

                for (int dice = 1; dice <= 6; dice++) {
                    int next = current + dice;
                    if (next > target)
                        continue;

                    next = board.getDestination(next);

                    if (minThrows[current] + 1 < minThrows[next]) {
                        minThrows[next] = minThrows[current] + 1;
                        updated = true;
                    }
                }
            }
        }

        long endTime = System.nanoTime();
        long elapsedMs = (endTime - startTime) / 1_000_000;

        return new SnakeLadderSolveResult(minThrows[target], elapsedMs);
    }
}