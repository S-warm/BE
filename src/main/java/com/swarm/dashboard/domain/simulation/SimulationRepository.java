package com.swarm.dashboard.domain.simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SimulationRepository extends JpaRepository<Simulation, UUID> {
    List<Simulation> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}