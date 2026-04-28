package com.swarm.dashboard.domain.issue;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "issue_age_stats")
@Getter
@NoArgsConstructor
public class IssueAgeStats {

    @EmbeddedId
    private IssueAgeStatsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("issueId")
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column(name = "coord_x", precision = 6, scale = 4)
    private BigDecimal coordX;

    @Column(name = "coord_y", precision = 6, scale = 4)
    private BigDecimal coordY;

    @Column(name = "scroll_y")
    private Integer scrollY;

    @Column(name = "affected_users")
    private Integer affectedUsers;

    @Column(name = "affected_percent", precision = 5, scale = 2)
    private BigDecimal affectedPercent;

    @Column(name = "block_rate", precision = 5, scale = 2)
    private BigDecimal blockRate;

    @Column(name = "repeat_count", precision = 5, scale = 2)
    private BigDecimal repeatCount;

    @Column(name = "error_type", length = 20)
    private String errorType;

    @Column(name = "timeout_count")
    private Integer timeoutCount;

    @Column(name = "network_count")
    private Integer networkCount;

    @Column(name = "console_count")
    private Integer consoleCount;
}