package com.swarm.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.util.S3FetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ResultService {

    private final SimulationRepository simulationRepository;
    private final S3FetchService s3FetchService;
    private final SimulationProcessor processor;
    private final ObjectMapper objectMapper;

    public void processFromDoneJson(UUID projectId, String jobId) {
        try {
            simulationRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("시뮬레이션을 찾을 수 없습니다. id=" + projectId));

            String doneJson = s3FetchService.fetchJson("done/" + jobId + ".json");
            JsonNode done = objectMapper.readTree(doneJson);

            JsonNode resultsNode = done.get("results");
            Map<String, String> results = new HashMap<>();
            resultsNode.fields().forEachRemaining(e -> results.put(e.getKey(), e.getValue().asText()));

            String titleSlug = done.has("title_slug") ? done.get("title_slug").asText() : null;
            String datePrefix = done.has("date_prefix") ? done.get("date_prefix").asText() : null;
            String screenshotsPrefix = (titleSlug != null && datePrefix != null)
                ? "raw/" + titleSlug + "/logs/" + datePrefix + "/screenshots/"
                : null;

            processor.processAll(projectId, results, screenshotsPrefix);

        } catch (Exception e) {
            log.error("S3 결과 처리 실패: projectId={}, jobId={}", projectId, jobId, e);
            processor.markFailed(projectId);
        }
    }
}
