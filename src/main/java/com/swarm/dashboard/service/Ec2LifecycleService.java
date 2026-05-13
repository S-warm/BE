package com.swarm.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class Ec2LifecycleService {

    private static final int HEALTH_POLL_INTERVAL_MS = 5000;
    private static final int HEALTH_TIMEOUT_MS = 5 * 60 * 1000; // 5분

    private final Ec2Client ec2Client;
    private final WebClient.Builder webClientBuilder;

    @Value("${aws.ai-ec2.instance-id}")
    private String instanceId;

    @Value("${python.endpoint-url}")
    private String pythonEndpointUrl;

    public void startInstance() {
        log.info("AI EC2 start: instanceId={}", instanceId);
        ec2Client.startInstances(StartInstancesRequest.builder()
            .instanceIds(instanceId)
            .build());
    }

    public void stopInstance() {
        log.info("AI EC2 stop: instanceId={}", instanceId);
        ec2Client.stopInstances(StopInstancesRequest.builder()
            .instanceIds(instanceId)
            .build());
    }

    // EC2 부팅 완료될 때까지 /health 폴링 (타임아웃 5분)
    public boolean waitUntilHealthy() {
        WebClient webClient = webClientBuilder.build();
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < HEALTH_TIMEOUT_MS) {
            try {
                Thread.sleep(HEALTH_POLL_INTERVAL_MS);

                String result = webClient.get()
                    .uri(pythonEndpointUrl + "/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

                if (result != null) {
                    log.info("AI EC2 healthy: instanceId={}", instanceId);
                    return true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception ignored) {
                // 아직 부팅 중 → 재시도
            }
        }

        log.error("AI EC2 health 타임아웃: instanceId={}", instanceId);
        return false;
    }
}
