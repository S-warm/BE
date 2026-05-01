package com.swarm.dashboard.domain.fix;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AiFixSuggestionRepository extends JpaRepository<AiFixSuggestion, UUID> {
    List<AiFixSuggestion> findBySimulationId(UUID simulationId);
    // findByPageId 제거 — page_id FK가 Entity에서 제거됨. issue_id → issues.page_id 로 접근
}