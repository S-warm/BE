package com.swarm.dashboard.domain.page;

import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "simulation_pages")
@Getter
@NoArgsConstructor
public class SimulationPage {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "page_key", length = 100, nullable = false)
    private String pageKey;

    @Column(name = "page_name", nullable = false)
    private String pageName;

    @Column(name = "screenshot_path", length = 500)
    private String screenshotPath;

    @Column(name = "viewport_width")
    private Integer viewportWidth;

    @Column(name = "viewport_height")
    private Integer viewportHeight;

    @Column(name = "page_order")
    private Integer pageOrder;
}