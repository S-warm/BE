package com.swarm.dashboard.domain.simulation;

import com.swarm.dashboard.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "simulations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "project_id", columnDefinition = "uuid", updatable = false)
    private UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "target_url", nullable = false, length = 1000)
    private String targetUrl;

    @Setter
    @Column(length = 20)
    private String status;

    @Column(name = "date_prefix", length = 30)
    private String datePrefix;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Setter
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
