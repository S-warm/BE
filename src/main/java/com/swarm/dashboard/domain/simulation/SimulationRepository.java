package com.swarm.dashboard.domain.simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface SimulationRepository extends JpaRepository<Simulation, UUID> {
    // FETCH JOIN으로 N+1 쿼리 방지
    @Query("SELECT s FROM Simulation s JOIN FETCH s.user WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Simulation> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}