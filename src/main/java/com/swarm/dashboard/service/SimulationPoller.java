package com.swarm.dashboard.service;

import com.swarm.dashboard.domain.simulation.SimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationPoller {

    private static final int POLL_INTERVAL_MS = 5000;
    private static final int TIMEOUT_MS = 30 * 60 * 1000; // 30분

    private final SimulationRepository simulationRepository;
    private final S3ResultService s3ResultService;
    private final Ec2LifecycleService ec2LifecycleService;
    private final WebClient.Builder webClientBuilder;

    @Value("${python.endpoint-url}")
    private String pythonEndpointUrl;

    @Async
    public void startPolling(UUID projectId, Map<String, Object> requestBody) {
        WebClient webClient = webClientBuilder.build();

        try {
            // 1. EC2 start → health 폴링
            ec2LifecycleService.startInstance();
            boolean healthy = ec2LifecycleService.waitUntilHealthy();
            if (!healthy) {
                log.error("AI EC2 health 확인 실패: projectId={}", projectId);
                markFailed(projectId);
                return;
            }

            // 2. Python /simulate 호출 → job_id 수신
            Map response = webClient.post()
                .uri(pythonEndpointUrl + "/simulate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response == null || response.get("job_id") == null) {
                log.error("Python /simulate 응답 없음: projectId={}", projectId);
                markFailed(projectId);
                return;
            }

            String jobId = response.get("job_id").toString();
            saveJobId(projectId, jobId);
            log.info("Python job_id 수신: projectId={}, jobId={}", projectId, jobId);

            // 3. /status 폴링
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
                try {
                    Thread.sleep(POLL_INTERVAL_MS);

                    Map statusResponse = webClient.get()
                        .uri(pythonEndpointUrl + "/status/" + jobId)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(10))
                        .block();

                    if (statusResponse == null) continue;

                    String status = statusResponse.getOrDefault("status", "").toString();
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

        } catch (Exception e) {
            log.error("시뮬레이션 처리 중 오류: projectId={}", projectId, e);
            markFailed(projectId);
        } finally {
            try {
                ec2LifecycleService.stopInstance();
            } catch (Exception e) {
                log.error("AI EC2 stop 실패: projectId={}", projectId, e);
            }
        }
    }

    @Transactional
    public void saveJobId(UUID projectId, String jobId) {
        simulationRepository.findById(projectId).ifPresent(s -> {
            s.setJobId(jobId);
            simulationRepository.save(s);
        });
    }

    @Transactional
    public void updateDbStatus(UUID projectId, String status) {
        simulationRepository.findById(projectId).ifPresent(s -> {
            if (!status.equals(s.getStatus())) {
                s.setStatus(status);
                simulationRepository.save(s);
            }
        });
    }

    @Transactional
    public void markFailed(UUID projectId) {
        simulationRepository.findById(projectId).ifPresent(s -> {
            s.setStatus("failed");
            simulationRepository.save(s);
        });
    }
}
