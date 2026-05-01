package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface WcagIssueRepository extends JpaRepository<WcagIssue, UUID> {
    List<WcagIssue> findByWcagResult_Id(UUID wcagResultId);

    // WCAG 탭 분포 집계용: simulation 전체 이슈를 severity 기준으로 조회
    @Query("SELECT wi FROM WcagIssue wi JOIN wi.wcagResult wr WHERE wr.simulation.id = :simulationId ORDER BY wi.severity ASC")
    List<WcagIssue> findBySimulationId(UUID simulationId);
}