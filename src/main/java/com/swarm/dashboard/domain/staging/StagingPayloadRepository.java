package com.swarm.dashboard.domain.staging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface StagingPayloadRepository extends JpaRepository<StagingPayload, StagingPayloadId> {
    List<StagingPayload> findByIdProjectId(UUID projectId);
    long countByIdProjectId(UUID projectId);

    @Transactional
    void deleteByIdProjectId(UUID projectId);
}
