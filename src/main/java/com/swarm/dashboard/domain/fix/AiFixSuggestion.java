package com.swarm.dashboard.domain.fix;

import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.page.SimulationPage;
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
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private SimulationPage page;

    // ✅ [추가] Issues 탭 연동 기준 FK — AiFixDto.issueId 의 실제 소스
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column
    private String title;

    @Column(length = 20)
    private String severity;

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