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
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "target_url", nullable = false, length = 1000)
    private String targetUrl;

    @Column(name = "persona_count")
    private Integer personaCount;

    @Column(length = 20)
    private String status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}