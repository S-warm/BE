package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface IssueAgeStatsRepository extends JpaRepository<IssueAgeStats, IssueAgeStatsId> {
    // 복합키의 issueId 필드로 조회
    List<IssueAgeStats> findById_IssueId(UUID issueId);

    // 특정 이슈의 특정 연령대 조회 (히트맵 ageGroup 필터)
    List<IssueAgeStats> findById_IssueIdAndId_AgeBand(UUID issueId, String ageBand);

    // simulation 전체 이슈의 issue_age_stats 조회 (N+1 방지 FETCH JOIN)
    @Query("SELECT ias FROM IssueAgeStats ias JOIN FETCH ias.issue i JOIN FETCH i.page p WHERE p.simulation.id = :simulationId")
    List<IssueAgeStats> findBySimulationId(UUID simulationId);

    // ageGroup 필터 포함 (N+1 방지 FETCH JOIN)
    @Query("SELECT ias FROM IssueAgeStats ias JOIN FETCH ias.issue i JOIN FETCH i.page p WHERE p.simulation.id = :simulationId AND ias.id.ageBand = :ageBand")
    List<IssueAgeStats> findBySimulationIdAndAgeBand(UUID simulationId, String ageBand);

    // 이슈별 affected_users 합산 (Issues 탭 affectedUsersCount 계산용)
    @Query("SELECT COALESCE(SUM(ias.affectedUsers), 0) FROM IssueAgeStats ias WHERE ias.id.issueId = :issueId")
    Integer sumAffectedUsersByIssueId(UUID issueId);
}