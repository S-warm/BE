package com.swarm.dashboard.domain.page;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SimulationPageRepository extends JpaRepository<SimulationPage, UUID> {
    List<SimulationPage> findBySimulationIdOrderByPageOrder(UUID simulationId);
}