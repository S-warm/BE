package com.swarm.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.domain.staging.StagingPayload;
import com.swarm.dashboard.domain.staging.StagingPayloadRepository;
import com.swarm.dashboard.dto.aicallback.*;
import com.swarm.dashboard.service.processor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationProcessor {

    private final StagingPayloadRepository stagingRepo;
    private final SimulationRepository simRepo;
    private final ObjectMapper objectMapper;

    private final OverviewProcessor overviewProcessor;
    private final IssueProcessor issueProcessor;
    private final HeatmapProcessor heatmapProcessor;
    private final WcagProcessor wcagProcessor;
    private final FixProcessor fixProcessor;

    @Transactional
    public void processAll(UUID projectId) {
        // Race condition 방지: 다른 스레드가 이미 처리해서 staging 삭제했으면 skip
        if (stagingRepo.countByIdProjectId(projectId) < 5) return;

        Map<String, String> payloads = stagingRepo.findByIdProjectId(projectId).stream()
            .collect(Collectors.toMap(
                p -> p.getId().getEndpoint(),
                StagingPayload::getPayload
            ));

        try {
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

            // status 변경
            Simulation sim = simRepo.findById(projectId).orElseThrow();
            sim.setStatus("completed");
            sim.setCompletedAt(OffsetDateTime.now());
            simRepo.save(sim);

            // staging 정리
            stagingRepo.deleteByIdProjectId(projectId);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse staging payload", e);
        }
    }
}
