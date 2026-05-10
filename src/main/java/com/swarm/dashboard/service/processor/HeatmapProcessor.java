package com.swarm.dashboard.service.processor;

import com.swarm.dashboard.domain.heatmap.HeatmapPoint;
import com.swarm.dashboard.domain.heatmap.HeatmapPointRepository;
import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueRepository;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.dto.aicallback.HeatmapRequest;
import com.swarm.dashboard.util.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HeatmapProcessor {

    private final SimulationRepository simRepo;
    private final SimulationPageRepository pageRepo;
    private final IssueRepository issueRepo;
    private final HeatmapPointRepository pointRepo;
    private final S3PresignService s3PresignService;

    public void process(UUID projectId, HeatmapRequest req, Map<String, UUID> issueIndexMap) {
        Simulation sim = simRepo.findById(projectId).orElseThrow();

        for (HeatmapRequest.ErrorPointDto dto : req.errorPoints()) {
            // 1) URL로 simulation_pages upsert (page_order는 다음 순번으로)
            SimulationPage page = pageRepo.findByProject_ProjectIdAndUrl(projectId, dto.url())
                .orElseGet(() -> {
                    Integer nextOrder = pageRepo.findMaxPageOrderByProjectId(projectId) + 1;
                    SimulationPage p = SimulationPage.builder()
                        .project(sim)
                        .url(dto.url())
                        .screenshotUrl(s3PresignService.extractKey(dto.screenshotUrl()))
                        .pageOrder(nextOrder)
                        .build();
                    return pageRepo.save(p);
                });

            // 2) issueId 매핑
            UUID issueUuid = issueIndexMap.get(dto.issueId());  // null이면 매핑 없음
            Issue issue = (issueUuid != null)
                ? issueRepo.findById(issueUuid).orElse(null)
                : null;

            // 3) HeatmapPoint INSERT
            HeatmapPoint point = HeatmapPoint.builder()
                .project(sim)
                .page(page)
                .issue(issue)
                .x(BigDecimal.valueOf(dto.x()))
                .y(BigDecimal.valueOf(dto.y()))
                .ageBand(dto.ageBand())          // "20s" 그대로
                .count(dto.count())
                .severity(dto.severity())        // 대문자 그대로
                .errorType(dto.errorType())      // 한글 그대로
                .build();
            pointRepo.save(point);
        }
    }
}
