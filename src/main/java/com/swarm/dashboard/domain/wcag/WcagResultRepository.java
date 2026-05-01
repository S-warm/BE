package com.swarm.dashboard.domain.wcag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface WcagResultRepository extends JpaRepository<WcagResult, UUID> {
    // simulation 단위 전체 wcag_results 조회 (WCAG 탭 통합 요약용)
    @Query("SELECT wr FROM WcagResult wr JOIN FETCH wr.page WHERE wr.simulation.id = :simulationId")
    List<WcagResult> findBySimulationId(UUID simulationId);

    List<WcagResult> findByPage_Id(UUID pageId);
}