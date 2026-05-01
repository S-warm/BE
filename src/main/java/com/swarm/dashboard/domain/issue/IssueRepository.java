package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    // FETCH JOIN으로 N+1 방지, page_order → severity 순 정렬
    @Query("SELECT i FROM Issue i JOIN FETCH i.page p WHERE p.simulation.id = :simulationId ORDER BY p.pageOrder ASC, i.severity ASC")
    List<Issue> findBySimulationIdOrderByPageAndSeverity(UUID simulationId);

    List<Issue> findByPage_Id(UUID pageId);
}