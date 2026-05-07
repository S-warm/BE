package com.swarm.dashboard.domain.wcag;

import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "wcag_results")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WcagResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Simulation project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private SimulationPage page;

    @Column(name = "score")
    private Integer score;

    @Column(name = "wcag_label", length = 10)
    private String wcagLabel;

    @Column(name = "distribution_critical")
    private Integer distributionCritical;

    @Column(name = "distribution_moderate")
    private Integer distributionModerate;

    @Column(name = "distribution_minor")
    private Integer distributionMinor;
}
