package com.swarm.dashboard.domain.staging;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class StagingPayloadId implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "endpoint", length = 20)
    private String endpoint;  // overview/issues/heatmap/wcag/fixes

    public StagingPayloadId(UUID projectId, String endpoint) {
        this.projectId = projectId;
        this.endpoint = endpoint;
    }
}
