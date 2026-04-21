package com.nibm.pdsa.games.knightstour.repository;

import com.nibm.pdsa.games.knightstour.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("knightsTourPlayerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
}
