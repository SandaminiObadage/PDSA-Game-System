package com.nibm.pdsa.games.knightstour.repository;

import com.nibm.pdsa.games.knightstour.entity.Knight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnightRepository extends JpaRepository<Knight, Long> {
}
