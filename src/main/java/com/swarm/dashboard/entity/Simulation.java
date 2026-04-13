package com.swarm.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// @Entity — domain/simulation/Simulation.java 로 통합됨. 중복 @Table("simulations") 충돌 방지를 위해 비활성화
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Simulation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ← 추가된 필드
    @Column(nullable = false)
    private Long userId;

    // ✅ siteName 제거 — title로 통일

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String targetUrl;

    @Column(nullable = false)
    private Integer personaCount;

    @Column(nullable = false)
    private String digitalLiteracy;

    @Column(nullable = false)
    private String successCondition;

    // desktop / mobile / tablet
    @Column(nullable = false)
    private String personaDevice;

    // ✅ ratio 필드명 DB simulation_settings 컬럼명으로 통일
    @Column(nullable = false)
    private Integer ageRatioTeen;

    @Column(nullable = false)
    private Integer ageRatioFifty;

    @Column(nullable = false)
    private Integer ageRatioEighty;

    // pending / running / completed / failed
    @Column(nullable = false)
    private String status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Simulation(Long userId, String title, String targetUrl,
                      Integer personaCount, String digitalLiteracy,
                      String successCondition, String personaDevice,
                      Integer ageRatioTeen, Integer ageRatioFifty, Integer ageRatioEighty) {
        this.userId = userId;
        this.title = title;
        this.targetUrl = targetUrl;
        this.personaCount = personaCount;
        this.digitalLiteracy = digitalLiteracy;
        this.successCondition = successCondition;
        this.personaDevice = personaDevice;
        this.ageRatioTeen = ageRatioTeen;
        this.ageRatioFifty = ageRatioFifty;
        this.ageRatioEighty = ageRatioEighty;
        this.status = "pending";
    }
}