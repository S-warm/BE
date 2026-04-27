package com.swarm.dashboard.domain.page;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PageAgeStatsRepository extends JpaRepository<PageAgeStats, UUID> {
    List<PageAgeStats> findByPageId(UUID pageId);
}