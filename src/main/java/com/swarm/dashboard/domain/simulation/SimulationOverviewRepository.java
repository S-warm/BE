package com.swarm.dashboard.domain.simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SimulationOverviewRepository extends JpaRepository<SimulationOverview, UUID> {
    Optional<SimulationOverview> findBySimulationId(UUID simulationId);
}