// package com.example.gameproject;
package com.nibm.pdsa.games.minimumcost.algorithm;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

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
        List<int[]> permutations = generatePermutations(n);
        int minCost = Integer.MAX_VALUE;

        for (int[] perm : permutations) {
            int cost = 0;
            for (int i = 0; i < n; i++) {
                cost += costMatrix[i][perm[i]];
            }
            if (cost < minCost) {
                minCost = cost;
            }
        }
        return minCost;
    }

    private List<int[]> generatePermutations(int n) {
        List<int[]> result = new ArrayList<>();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        permute(arr, 0, result);
        return result;
    }

    private void permute(int[] arr, int start, List<int[]> result) {
        if (start == arr.length - 1) {
            result.add(arr.clone());
            return;
        }
        for (int i = start; i < arr.length; i++) {
            swap(arr, start, i);
            permute(arr, start + 1, result);
            swap(arr, start, i);
        }
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}