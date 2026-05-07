package com.swarm.dashboard.domain.fix;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface AiFixSuggestionRepository extends JpaRepository<AiFixSuggestion, UUID> {

    @Query("SELECT a FROM AiFixSuggestion a JOIN FETCH a.issue i JOIN FETCH i.page p WHERE a.project.projectId = :projectId ORDER BY p.pageOrder ASC")
    List<AiFixSuggestion> findByProjectId(UUID projectId);
}
