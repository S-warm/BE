package com.swarm.dashboard.domain.simulation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "simulation_overview")
@Getter
@NoArgsConstructor
public class SimulationOverview {

    @Id
    @Column(name = "simulation_id", columnDefinition = "uuid")
    private UUID simulationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "conversion_rate", precision = 5, scale = 2)
    private BigDecimal conversionRate;

    @Column(name = "tested_agent_count")
    private Integer testedAgentCount;

    @Column(name = "avg_completion_ms")
    private Integer avgCompletionMs;

    @Column(name = "success_event_count")
    private Integer successEventCount;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}