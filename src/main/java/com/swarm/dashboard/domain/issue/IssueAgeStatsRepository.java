package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IssueAgeStatsRepository extends JpaRepository<IssueAgeStats, IssueAgeStatsId> {
    List<IssueAgeStats> findByIssueId(UUID issueId);
}