package com.swarm.dashboard.domain.wcag;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "wcag_issues")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WcagIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wcag_result_id")
    private WcagResult wcagResult;

    @Column
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private WcagSeverity severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String html;

    @Column(name = "wcag_criteria", length = 20)
    private String wcagCriteria;
}
