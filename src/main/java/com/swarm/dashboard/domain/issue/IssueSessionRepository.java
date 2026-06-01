package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface IssueSessionRepository extends JpaRepository<IssueSession, UUID> {
    List<IssueSession> findByIssue_Id(UUID issueId);

    @Query("SELECT s FROM IssueSession s WHERE s.issue.project.projectId = :projectId")
    List<IssueSession> findByProjectId(@Param("projectId") UUID projectId);
}
