package com.swarm.dashboard.service;

import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.util.S3FetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ResultService {

    // 나중에 Python 팀에서 경로 확정되면 이 상수만 교체
    private static final String RESULT_DIR = "result";

    private final SimulationRepository simulationRepository;
    private final S3FetchService s3FetchService;
    private final SimulationProcessor processor;

    public void processFromS3(UUID projectId) {
        try {
            Simulation simulation = simulationRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("시뮬레이션을 찾을 수 없습니다. id=" + projectId));

            if (!"pending".equals(simulation.getStatus())) return;

            String prefix = simulation.getDatePrefix() + "/" + RESULT_DIR + "/";

            Map<String, String> payloads = new LinkedHashMap<>();
            payloads.put("overview", s3FetchService.fetchJson(prefix + "overview.json"));
            payloads.put("issues",   s3FetchService.fetchJson(prefix + "issues.json"));
            payloads.put("heatmap",  s3FetchService.fetchJson(prefix + "heatmap.json"));
            payloads.put("wcag",     s3FetchService.fetchJson(prefix + "wcag.json"));
            payloads.put("fixes",    s3FetchService.fetchJson(prefix + "fixes.json"));

            processor.processAll(projectId, payloads);

        } catch (Exception e) {
            log.error("S3 결과 처리 실패: projectId={}", projectId, e);
            processor.markFailed(projectId);
        }
    }
}
