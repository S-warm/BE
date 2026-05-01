package com.swarm.dashboard.domain.wcag;

import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wcag_results")
@Getter
@NoArgsConstructor
public class WcagResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private SimulationPage page;

    @Column(name = "compliance_score")
    private Integer complianceScore;

    @Column(name = "wcag_label", length = 50)
    private String wcagLabel;

    @Column(name = "passed_tests")
    private Integer passedTests;

    @Column(name = "total_tests")
    private Integer totalTests;

    @Column(name = "found_issues")
    private Integer foundIssues;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}