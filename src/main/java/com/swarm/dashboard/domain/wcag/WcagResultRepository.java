package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface WcagResultRepository extends JpaRepository<WcagResult, UUID> {

    @Query("SELECT wr FROM WcagResult wr JOIN FETCH wr.page WHERE wr.project.projectId = :projectId")
    List<WcagResult> findByProjectId(UUID projectId);

    List<WcagResult> findByPage_Id(UUID pageId);

    java.util.Optional<WcagResult> findByProject_ProjectIdAndPage_Id(UUID projectId, UUID pageId);
}
