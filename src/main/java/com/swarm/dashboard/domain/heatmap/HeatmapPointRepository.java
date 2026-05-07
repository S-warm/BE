package com.swarm.dashboard.domain.heatmap;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HeatmapPointRepository extends JpaRepository<HeatmapPoint, UUID> {
    List<HeatmapPoint> findByProject_ProjectId(UUID projectId);
    List<HeatmapPoint> findByPage_Id(UUID pageId);
}
