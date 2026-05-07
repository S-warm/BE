package com.swarm.dashboard.domain.simulation;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "simulation_overview")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationOverview {

    @Id
    @Column(name = "project_id", columnDefinition = "uuid", updatable = false)
    private UUID projectId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "project_id")
    private Simulation project;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "avg_duration_ms")
    private Long avgDurationMs;

    @Column(name = "success_rate", precision = 8, scale = 4)
    private BigDecimal successRate;
}
