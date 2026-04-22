package com.nibm.pdsa.games.minimumcost.model;

import jakarta.persistence.*;

@Entity(name = "MinimumCostPlayer")
@Table(name = "game_results")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String playerName;
    
    @Column(nullable = false)
    private int correctCost;
    
    @Column(nullable = false)
    private int selectedCost;
    
    @Column(nullable = false)
    private int timeRemaining;
    
    @Column(nullable = false)
    private boolean isCorrect;
    
    @Column(nullable = false)
    private long timestamp;

    public Player() {
        this.timestamp = System.currentTimeMillis();
    }

    public Player(String playerName, int correctCost, int selectedCost, int timeRemaining, boolean isCorrect) {
        this.playerName = playerName;
        this.correctCost = correctCost;
        this.selectedCost = selectedCost;
        this.timeRemaining = timeRemaining;
        this.isCorrect = isCorrect;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCorrectCost() {
        return correctCost;
    }

    public void setCorrectCost(int correctCost) {
        this.correctCost = correctCost;
    }

    public int getSelectedCost() {
        return selectedCost;
    }

    public void setSelectedCost(int selectedCost) {
        this.selectedCost = selectedCost;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}