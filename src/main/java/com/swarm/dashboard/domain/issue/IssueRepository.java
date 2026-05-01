package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {
    // FETCH JOIN으로 N+1 방지, page_order → severity 순 정렬
    // severity는 알파벳 순 정렬이 틀리므로 Java에서 정렬 (CRITICAL→HIGH→MEDIUM→LOW)
    @Query("SELECT i FROM Issue i JOIN FETCH i.page p WHERE p.simulation.id = :simulationId ORDER BY p.pageOrder ASC")
    List<Issue> findBySimulationIdOrderByPageAndSeverity(UUID simulationId);

    @Query("SELECT i FROM Issue i JOIN FETCH i.page WHERE i.page.id = :pageId")
    List<Issue> findByPage_Id(UUID pageId);
}