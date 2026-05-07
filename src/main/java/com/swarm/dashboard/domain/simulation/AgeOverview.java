package com.swarm.dashboard.domain.simulation;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "age_overview")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeOverview {

    @EmbeddedId
    private AgeOverviewId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Simulation project;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "success_rate", precision = 8, scale = 4)
    private BigDecimal successRate;

    @Column(name = "fail_rate", precision = 8, scale = 4)
    private BigDecimal failRate;

    @Column(name = "avg_duration_ms")
    private Long avgDurationMs;

    @Column(name = "avg_actions", precision = 6, scale = 2)
    private BigDecimal avgActions;

    @Column(name = "avg_declare_failure", precision = 6, scale = 2)
    private BigDecimal avgDeclareFailure;
}
