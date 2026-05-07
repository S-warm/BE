package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface IssueAgeStatsRepository extends JpaRepository<IssueAgeStats, IssueAgeStatsId> {

    List<IssueAgeStats> findById_IssueId(UUID issueId);

    @Query("SELECT ias FROM IssueAgeStats ias JOIN FETCH ias.issue i JOIN FETCH i.page p WHERE p.project.projectId = :projectId")
    List<IssueAgeStats> findByProjectId(UUID projectId);
}
