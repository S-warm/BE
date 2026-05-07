package com.swarm.dashboard.domain.fix;

import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "ai_fix_suggestions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiFixSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Simulation project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column(name = "selector", length = 500)
    private String selector;

    @Column(name = "before_code", columnDefinition = "TEXT")
    private String beforeCode;

    @Column(name = "after_code", columnDefinition = "TEXT")
    private String afterCode;

    @Column(name = "change_summary_body", columnDefinition = "TEXT")
    private String changeSummaryBody;

    @Column(name = "impact_summary", columnDefinition = "TEXT")
    private String impactSummary;
}
