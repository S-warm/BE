package com.swarm.dashboard.service.processor;

import com.swarm.dashboard.domain.simulation.*;
import com.swarm.dashboard.dto.aicallback.OverviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OverviewProcessor {

    private final SimulationRepository simRepo;
    private final SimulationOverviewRepository overviewRepo;
    private final AgeOverviewRepository ageOverviewRepo;

    public void process(UUID projectId, OverviewRequest req) {
        Simulation sim = simRepo.findById(projectId).orElseThrow();

        // 1) summary → simulation_overview 1행
        OverviewRequest.SummaryDto s = req.summary();
        SimulationOverview overview = SimulationOverview.builder()
            .project(sim)
            .totalSessions(s.totalSessions())
            .successCount(s.successCount())
            .successRate(BigDecimal.valueOf(s.successRate()))
            .avgDurationMs(s.avgDurationMs())
            .build();
        overviewRepo.save(overview);

        // 2) overview[] → age_overview 행들
        for (OverviewRequest.AgeOverviewDto a : req.overview()) {
            AgeOverviewId id = new AgeOverviewId(projectId, a.ageGroup());  // "20s" 그대로
            AgeOverview ao = AgeOverview.builder()
                .id(id)
                .project(sim)
                .totalSessions(a.totalSessions())
                .successCount(a.successCount())
                .successRate(BigDecimal.valueOf(a.successRate()))
                .failRate(BigDecimal.valueOf(a.failRate()))
                .avgDurationMs(a.avgDurationMs())
                .avgActions(BigDecimal.valueOf(a.avgActions()))
                .avgDeclareFailure(BigDecimal.valueOf(a.avgDeclareFailure()))
                .build();
            ageOverviewRepo.save(ao);
        }
    }
}
