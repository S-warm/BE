package com.swarm.dashboard.domain.page;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SimulationPageRepository extends JpaRepository<SimulationPage, UUID> {
    List<SimulationPage> findByProject_ProjectIdOrderByPageOrder(UUID projectId);

    Optional<SimulationPage> findByProject_ProjectIdAndUrl(UUID projectId, String url);

    @Query("SELECT COALESCE(MAX(p.pageOrder), 0) FROM SimulationPage p WHERE p.project.projectId = :projectId")
    Integer findMaxPageOrderByProjectId(@Param("projectId") UUID projectId);
}
