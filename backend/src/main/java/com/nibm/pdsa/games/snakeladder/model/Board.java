package com.nibm.pdsa.games.snakeladder.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Board {
    private int n;
    private List<Ladder> ladders;
    private List<Snake> snakes;
    private Map<Integer, Integer> transitions; // cell -> destination

    public Board(int n, List<Ladder> ladders, List<Snake> snakes) {
        if (n < 6 || n > 12) {
            throw new IllegalArgumentException("Board size must be between 6 and 12");
        }
        if (ladders.size() != n - 2) {
            throw new IllegalArgumentException("Number of ladders must be " + (n - 2));
        }
        if (snakes.size() != n - 2) {
            throw new IllegalArgumentException("Number of snakes must be " + (n - 2));
        }
        this.n = n;
        this.ladders = ladders;
        this.snakes = snakes;
        buildTransitions();
    }

    private void buildTransitions() {
        transitions = new HashMap<>();
        for (Ladder ladder : ladders) {
            transitions.put(ladder.getStart(), ladder.getEnd());
        }
        for (Snake snake : snakes) {
            transitions.put(snake.getHead(), snake.getTail());
        }
    }

    public int getSize() {
        return n * n;
    }

    public int getN() {
        return n;
    }

    public List<Ladder> getLadders() {
        return ladders;
    }

    public List<Snake> getSnakes() {
        return snakes;
    }

    public int getDestination(int cell) {
        return transitions.getOrDefault(cell, cell);
    }

    public boolean hasTransition(int cell) {
        return transitions.containsKey(cell);
    }
}