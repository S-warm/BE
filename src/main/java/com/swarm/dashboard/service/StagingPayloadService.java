package com.swarm.dashboard.service;

import com.swarm.dashboard.domain.staging.StagingPayload;
import com.swarm.dashboard.domain.staging.StagingPayloadId;
import com.swarm.dashboard.domain.staging.StagingPayloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StagingPayloadService {

    private final StagingPayloadRepository stagingRepo;
    private final SimulationProcessor processor;

    @Transactional
    public void receive(UUID projectId, String endpoint, String rawJson) {
        // UPSERT (재시도 멱등성)
        StagingPayload payload = StagingPayload.builder()
            .id(new StagingPayloadId(projectId, endpoint))
            .payload(rawJson)
            .receivedAt(OffsetDateTime.now())
            .build();
        stagingRepo.save(payload);

        // 5/5 도착 확인
        long count = stagingRepo.countByIdProjectId(projectId);
        if (count == 5) {
            try {
                // REQUIRES_NEW 트랜잭션 — 실패해도 staging save는 이미 커밋됨
                processor.processAll(projectId);
            } catch (Exception e) {
                log.error("AI payload 처리 실패: projectId={}", projectId, e);
                processor.markFailed(projectId);  // 별도 트랜잭션으로 status="failed" 저장
            }
        }
    }
}
