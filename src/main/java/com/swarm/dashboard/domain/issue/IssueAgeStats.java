package com.swarm.dashboard.domain.issue;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "issue_age_stats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAgeStats {

    @EmbeddedId
    private IssueAgeStatsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("issueId")
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column(name = "affected_users")
    private Integer affectedUsers;
}
