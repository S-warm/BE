package com.swarm.dashboard.domain.heatmap;

import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "heatmap_points")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapPoint {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;  // nullable — 이슈 매핑 안 되는 일반 클릭도 저장 가능

    @Column(precision = 6, scale = 4, nullable = false)
    private BigDecimal x;

    @Column(precision = 6, scale = 4, nullable = false)
    private BigDecimal y;

    @Column(name = "age_band", length = 10, nullable = false)
    private String ageBand;  // "20s" 영문 저장

    @Column
    private Integer count;

    @Column(length = 20)
    private String severity;  // 대문자 그대로 ("HIGH"/"MEDIUM"/"LOW"/"CRITICAL")

    @Column(name = "error_type", length = 200)
    private String errorType;  // "사용성/시인성 부족" 한글 그대로
}
