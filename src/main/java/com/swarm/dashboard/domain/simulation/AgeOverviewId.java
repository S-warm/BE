package com.swarm.dashboard.domain.simulation;

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
public class AgeOverviewId implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "age_band", length = 10)
    private String ageBand;  // "20s" 영문으로 저장

    public AgeOverviewId(UUID projectId, String ageBand) {
        this.projectId = projectId;
        this.ageBand = ageBand;
    }
}
