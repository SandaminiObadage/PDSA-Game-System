package com.nibm.pdsa.games.minimumcost.dto;

public class GameDataResponse {

    private int[][] matrix;
    private int greedyCost;
    private int hungarianCost;
    private int matrixSize;

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getGreedyCost() {
        return greedyCost;
    }

    public void setGreedyCost(int greedyCost) {
        this.greedyCost = greedyCost;
    }

    public int getHungarianCost() {
        return hungarianCost;
    }

    public void setHungarianCost(int hungarianCost) {
        this.hungarianCost = hungarianCost;
    }

    public int getMatrixSize() {
        return matrixSize;
    }

    public void setMatrixSize(int matrixSize) {
        this.matrixSize = matrixSize;
    }
}