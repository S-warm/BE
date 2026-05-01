package com.swarm.dashboard.domain.wcag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;


@Entity
@Table(name = "wcag_issues")
@Getter
@NoArgsConstructor
public class WcagIssue {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wcag_result_id")
    private WcagResult wcagResult;

    @Column(name = "issue_no")
    private Integer issueNo;

    @Column
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WcagSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String description;
}