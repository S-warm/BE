package com.swarm.dashboard.domain.issue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class IssueAgeStatsId implements Serializable {

    @Column(name = "issue_id")
    private UUID issueId;

    @Column(name = "age_band", length = 10)
    private String ageBand;
}