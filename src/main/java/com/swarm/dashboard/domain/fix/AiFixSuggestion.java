package com.swarm.dashboard.domain.fix;

import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueSeverity;
import com.swarm.dashboard.domain.simulation.Simulation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_fix_suggestions")
@Getter
@NoArgsConstructor
public class AiFixSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    // page_id 제거 — issue_id → issues.page_id 로 접근 (중복 FK 정규화)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private IssueSeverity severity;

    @Column(name = "before_code", columnDefinition = "TEXT")
    private String beforeCode;

    @Column(name = "after_code", columnDefinition = "TEXT")
    private String afterCode;

    @Column(name = "impact_summary", columnDefinition = "TEXT")
    private String impactSummary;

    @Column(name = "change_summary_title")
    private String changeSummaryTitle;

    @Column(name = "change_summary_body", columnDefinition = "TEXT")
    private String changeSummaryBody;

    @Column(name = "impacted_users")
    private Integer impactedUsers;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}