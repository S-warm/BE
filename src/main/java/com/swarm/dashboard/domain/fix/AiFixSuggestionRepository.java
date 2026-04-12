package com.swarm.dashboard.domain.fix;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AiFixSuggestionRepository extends JpaRepository<AiFixSuggestion, UUID> {
    List<AiFixSuggestion> findBySimulationId(UUID simulationId);
    List<AiFixSuggestion> findByPageId(UUID pageId);
}