package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IssueSessionRepository extends JpaRepository<IssueSession, UUID> {
    List<IssueSession> findByIssue_Id(UUID issueId);
}
