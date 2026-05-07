package com.swarm.dashboard.domain.simulation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AgeOverviewRepository extends JpaRepository<AgeOverview, AgeOverviewId> {
    List<AgeOverview> findByProject_ProjectId(UUID projectId);
}
