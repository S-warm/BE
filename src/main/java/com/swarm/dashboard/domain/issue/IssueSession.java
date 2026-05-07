package com.swarm.dashboard.domain.issue;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "issue_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column(name = "age_band", length = 10)
    private String ageBand;  // "20s" 영문 저장

    @Column(name = "session_id", length = 255)
    private String sessionId;
}
