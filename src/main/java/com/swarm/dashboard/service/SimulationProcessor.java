package com.swarm.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.dto.aicallback.*;
import com.swarm.dashboard.service.processor.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationProcessor {

    private final SimulationRepository simRepo;
    private final ObjectMapper objectMapper;

    private final OverviewProcessor overviewProcessor;
    private final IssueProcessor issueProcessor;
    private final HeatmapProcessor heatmapProcessor;
    private final WcagProcessor wcagProcessor;
    private final FixProcessor fixProcessor;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void processAll(UUID projectId, Map<String, String> payloads) throws Exception {
        OverviewRequest overviewReq = objectMapper.readValue(payloads.get("overview"), OverviewRequest.class);
        IssuesRequest issuesReq = objectMapper.readValue(payloads.get("issues"), IssuesRequest.class);
        HeatmapRequest heatmapReq = objectMapper.readValue(payloads.get("heatmap"), HeatmapRequest.class);
        WcagRequest wcagReq = objectMapper.readValue(payloads.get("wcag"), WcagRequest.class);
        FixesRequest fixesReq = objectMapper.readValue(payloads.get("fixes"), FixesRequest.class);

        // 순서 중요: issues 먼저 (issueIndexMap 생성) → heatmap, fixes가 사용
        overviewProcessor.process(projectId, overviewReq);
        Map<String, UUID> issueIndexMap = issueProcessor.process(projectId, issuesReq);
        heatmapProcessor.process(projectId, heatmapReq, issueIndexMap);
        wcagProcessor.process(projectId, wcagReq);
        fixProcessor.process(projectId, fixesReq);

        Simulation sim = simRepo.findById(projectId).orElseThrow();
        sim.setStatus("completed");
        sim.setCompletedAt(OffsetDateTime.now());
        simRepo.save(sim);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID projectId) {
        simRepo.findById(projectId).ifPresent(s -> {
            s.setStatus("failed");
            simRepo.save(s);
        });
    }
}
