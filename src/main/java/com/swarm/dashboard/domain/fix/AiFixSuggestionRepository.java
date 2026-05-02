package com.swarm.dashboard.domain.fix;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface AiFixSuggestionRepository extends JpaRepository<AiFixSuggestion, UUID> {
    // issue + issue.page JOIN FETCH (N+1 방지, page 기준 그룹핑 필요)
    @Query("SELECT a FROM AiFixSuggestion a JOIN FETCH a.issue i JOIN FETCH i.page p WHERE a.simulation.id = :simulationId ORDER BY p.pageOrder ASC")
    List<AiFixSuggestion> findBySimulationId(UUID simulationId);
}