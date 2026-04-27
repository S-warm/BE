package com.swarm.dashboard.domain.simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SimulationSettingsRepository extends JpaRepository<SimulationSettings, UUID> {
}