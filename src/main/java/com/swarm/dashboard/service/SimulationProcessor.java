package com.swarm.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.domain.staging.StagingPayload;
import com.swarm.dashboard.domain.staging.StagingPayloadRepository;
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
import java.util.stream.Collectors;

@Slf4j
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

    // REQUIRES_NEW: receive()의 트랜잭션과 분리 — 실패해도 staging save는 커밋 유지
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void processAll(UUID projectId) throws Exception {
        // Race condition 방지: 다른 스레드가 이미 처리해서 staging 삭제했으면 skip
        if (stagingRepo.countByIdProjectId(projectId) < 5) return;

        Map<String, String> payloads = stagingRepo.findByIdProjectId(projectId).stream()
            .collect(Collectors.toMap(
                p -> p.getId().getEndpoint(),
                StagingPayload::getPayload
            ));

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
    }

    // 실패 시 status 업데이트 — 별도 트랜잭션으로 반드시 커밋
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID projectId) {
        simRepo.findById(projectId).ifPresent(s -> {
            s.setStatus("failed");
            simRepo.save(s);
        });
    }
}
