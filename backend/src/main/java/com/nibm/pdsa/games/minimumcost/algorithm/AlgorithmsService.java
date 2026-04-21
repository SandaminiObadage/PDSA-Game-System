// package com.example.gameproject;
package com.nibm.pdsa.games.minimumcost.algorithm;

import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class AlgorithmsService {

    // Greedy algorithm for assignment problem
    public int greedy(int[][] costMatrix) {
        int n = costMatrix.length;
        boolean[] used = new boolean[n];
        int totalCost = 0;

        for (int i = 0; i < n; i++) {
            int min = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < n; j++) {
                if (!used[j] && costMatrix[i][j] < min) {
                    min = costMatrix[i][j];
                    minIndex = j;
                }
            }
            if (minIndex != -1) {
                used[minIndex] = true;
                totalCost += min;
            }
        }
        return totalCost;
    }

    // Hungarian algorithm (optimal using brute force for small n)
    public int hungarian(int[][] costMatrix) {
        int n = costMatrix.length;
        if (n == 0) {
            return 0;
        }

        for (int[] row : costMatrix) {
            if (row.length != n) {
                throw new IllegalArgumentException("Cost matrix must be square");
            }
        }

        // O(n^3) Hungarian method for minimum cost bipartite matching.
        int[] u = new int[n + 1];
        int[] v = new int[n + 1];
        int[] p = new int[n + 1];
        int[] way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            int[] minv = new int[n + 1];
            boolean[] used = new boolean[n + 1];
            Arrays.fill(minv, Integer.MAX_VALUE / 4);

            do {
                used[j0] = true;
                int i0 = p[j0];
                int delta = Integer.MAX_VALUE / 4;
                int j1 = 0;

                for (int j = 1; j <= n; j++) {
                    if (used[j]) {
                        continue;
                    }
                    int cur = costMatrix[i0 - 1][j - 1] - u[i0] - v[j];
                    if (cur < minv[j]) {
                        minv[j] = cur;
                        way[j] = j0;
                    }
                    if (minv[j] < delta) {
                        delta = minv[j];
                        j1 = j;
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) {
                        u[p[j]] += delta;
                        v[j] -= delta;
                    } else {
                        minv[j] -= delta;
                    }
                }
                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        int totalCost = 0;
        for (int j = 1; j <= n; j++) {
            int worker = p[j] - 1;
            int task = j - 1;
            totalCost += costMatrix[worker][task];
        }
        return totalCost;
    }
}