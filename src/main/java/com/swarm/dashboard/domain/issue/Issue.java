package com.swarm.dashboard.domain.issue;

import com.swarm.dashboard.config.StringListConverter;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "issues")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue {

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

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

    @Column(length = 50)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private IssueSeverity severity;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_html", length = 500)
    private String targetHtml;

    @Column(name = "fail_count")
    private Integer failCount;

    @Column(name = "fail_rate", precision = 8, scale = 4)
    private BigDecimal failRate;
}
