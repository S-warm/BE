package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WcagResultRepository extends JpaRepository<WcagResult, UUID> {
    List<WcagResult> findBySimulationId(UUID simulationId);
    List<WcagResult> findByPageId(UUID pageId);
}