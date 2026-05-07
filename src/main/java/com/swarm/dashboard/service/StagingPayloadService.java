package com.swarm.dashboard.service;

import com.swarm.dashboard.domain.staging.StagingPayload;
import com.swarm.dashboard.domain.staging.StagingPayloadId;
import com.swarm.dashboard.domain.staging.StagingPayloadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

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
            processor.processAll(projectId);
        }
    }
}
