package com.swarm.dashboard.domain.issue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    @Query("SELECT i FROM Issue i JOIN FETCH i.page p WHERE p.project.projectId = :projectId ORDER BY p.pageOrder ASC")
    List<Issue> findByProjectIdOrderByPageAndSeverity(UUID projectId);

    @Query("SELECT i FROM Issue i JOIN FETCH i.page WHERE i.page.id = :pageId")
    List<Issue> findByPage_Id(UUID pageId);

    Optional<Issue> findByPage_IdAndTitle(UUID pageId, String title);
}
