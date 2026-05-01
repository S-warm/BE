package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface WcagIssueRepository extends JpaRepository<WcagIssue, UUID> {
    List<WcagIssue> findByWcagResult_Id(UUID wcagResultId);

    // severity는 알파벳 순 정렬이 틀리므로 Java에서 정렬 (Critical→Moderate→Minor)
    @Query("SELECT wi FROM WcagIssue wi JOIN wi.wcagResult wr WHERE wr.simulation.id = :simulationId")
    List<WcagIssue> findBySimulationId(UUID simulationId);
}