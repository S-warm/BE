package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WcagIssueRepository extends JpaRepository<WcagIssue, UUID> {
    List<WcagIssue> findByWcagResultId(UUID wcagResultId);
}