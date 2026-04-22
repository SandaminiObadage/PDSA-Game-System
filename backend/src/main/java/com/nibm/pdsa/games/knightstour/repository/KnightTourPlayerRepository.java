package com.nibm.pdsa.games.knightstour.repository;

import com.nibm.pdsa.games.knightstour.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnightTourPlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
}
