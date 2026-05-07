package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface WcagIssueRepository extends JpaRepository<WcagIssue, UUID> {

    List<WcagIssue> findByWcagResult_Id(UUID wcagResultId);

    @Query("SELECT wi FROM WcagIssue wi JOIN wi.wcagResult wr WHERE wr.project.projectId = :projectId")
    List<WcagIssue> findByProjectId(UUID projectId);
}
