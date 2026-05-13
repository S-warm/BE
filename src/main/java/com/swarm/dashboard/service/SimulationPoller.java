package com.swarm.dashboard.service;

import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationPoller {

    private static final int POLL_INTERVAL_MS = 3000;
    private static final int TIMEOUT_MS = 30 * 60 * 1000; // 30분

    private final SimulationRepository simulationRepository;
    private final S3ResultService s3ResultService;
    private final WebClient.Builder webClientBuilder;

    @Value("${python.endpoint-url}")
    private String pythonEndpointUrl;

    @Async
    public void startPolling(UUID projectId, String jobId) {
        WebClient webClient = webClientBuilder.build();
        long startTime = System.currentTimeMillis();

        log.info("폴링 시작: projectId={}, jobId={}", projectId, jobId);

        while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);

                Map response = webClient.get()
                    .uri(pythonEndpointUrl + "/status/" + jobId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

                if (response == null) continue;

                String status = response.getOrDefault("status", "").toString();
                log.debug("폴링 응답: projectId={}, status={}", projectId, status);

                if ("completed".equals(status)) {
                    log.info("시뮬레이션 완료: projectId={}, jobId={}", projectId, jobId);
                    s3ResultService.processFromDoneJson(projectId, jobId);
                    return;
                }

                if ("failed".equals(status)) {
                    log.warn("시뮬레이션 실패: projectId={}, jobId={}", projectId, jobId);
                    markFailed(projectId);
                    return;
                }

                // running/analyzing → DB status 동기화
                updateDbStatus(projectId, status);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("폴링 중 오류: projectId={}", projectId, e);
            }
        }

        log.error("폴링 타임아웃: projectId={}, jobId={}", projectId, jobId);
        markFailed(projectId);
    }

    private void updateDbStatus(UUID projectId, String status) {
        simulationRepository.findById(projectId).ifPresent(s -> {
            if (!status.equals(s.getStatus())) {
                s.setStatus(status);
                simulationRepository.save(s);
            }
        });
    }

    private void markFailed(UUID projectId) {
        simulationRepository.findById(projectId).ifPresent(s -> {
            s.setStatus("failed");
            simulationRepository.save(s);
        });
    }
}
