package com.nibm.pdsa.games.minimumcost.repository;

import com.nibm.pdsa.games.minimumcost.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository("minimumCostPlayerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    // Find all results for a specific player, ordered by most recent first
    List<Player> findByPlayerNameOrderByTimestampDesc(String playerName);
    
    // Find only winning/correct results, limited to top 20
    @Query(value = "SELECT * FROM game_results WHERE is_correct = 1 ORDER BY timestamp DESC LIMIT 20", nativeQuery = true)
    List<Player> findAllWinningResults();
    
    // Find all results ordered by most recent
    @Query(value = "SELECT * FROM game_results ORDER BY timestamp DESC", nativeQuery = true)
    List<Player> findAllResults();
    
    // Count total games by player
    long countByPlayerName(String playerName);
    
    // Count correct answers by player
    @Query("SELECT COUNT(p) FROM MinimumCostPlayer p WHERE p.playerName = :playerName AND p.isCorrect = true")
    long countCorrectByPlayerName(@Param("playerName") String playerName);
}