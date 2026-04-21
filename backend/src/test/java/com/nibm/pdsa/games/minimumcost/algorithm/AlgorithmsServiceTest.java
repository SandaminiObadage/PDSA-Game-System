package com.nibm.pdsa.games.minimumcost.algorithm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AlgorithmsService Tests")
class AlgorithmsServiceTest {

    private final AlgorithmsService algorithmsService = new AlgorithmsService();

    @Test
    @DisplayName("Hungarian should find known optimal assignment")
    void hungarianFindsKnownOptimal() {
        int[][] matrix = {
                {82, 83, 69, 92},
                {77, 37, 49, 92},
                {11, 69, 5, 86},
                {8, 9, 98, 23}
        };

        int optimal = algorithmsService.hungarian(matrix);
        assertEquals(140, optimal);
    }

    @Test
    @DisplayName("Greedy and Hungarian should both produce valid positive costs")
    void algorithmsReturnPositiveCosts() {
        int[][] matrix = {
                {20, 200, 150},
                {90, 30, 120},
                {60, 70, 40}
        };

        int greedy = algorithmsService.greedy(matrix);
        int hungarian = algorithmsService.hungarian(matrix);

        assertTrue(greedy > 0);
        assertTrue(hungarian > 0);
    }

    @Test
    @DisplayName("Hungarian should never be worse than Greedy")
    void hungarianIsNoWorseThanGreedy() {
        int[][] matrix = {
                {90, 40, 60, 50},
                {35, 85, 55, 65},
                {45, 95, 25, 75},
                {70, 80, 30, 20}
        };

        int greedy = algorithmsService.greedy(matrix);
        int hungarian = algorithmsService.hungarian(matrix);

        assertTrue(hungarian <= greedy);
    }
}
