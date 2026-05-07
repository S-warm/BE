package com.swarm.dashboard.domain.page;

import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "simulation_pages",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "url"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationPage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Simulation project;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "page_order")
    private Integer pageOrder;

    @Column(name = "screenshot_url", length = 1000)
    private String screenshotUrl;
}
