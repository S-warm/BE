package com.swarm.dashboard.domain.simulation;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "simulation_settings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationSettings {

    @Id
    @Column(name = "simulation_id", columnDefinition = "uuid")
    private UUID simulationId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "age_count_10")
    private Integer ageCount10;

    @Column(name = "age_count_20")
    private Integer ageCount20;

    @Column(name = "age_count_30")
    private Integer ageCount30;

    @Column(name = "age_count_40")
    private Integer ageCount40;

    @Column(name = "age_count_50")
    private Integer ageCount50;

    @Column(name = "age_count_60")
    private Integer ageCount60;

    @Column(name = "age_count_70")
    private Integer ageCount70;

    @Column(name = "digital_literacy", length = 20)
    private String digitalLiteracy;

    @Column(name = "vision_impairment")
    private Integer visionImpairment;

    @Column(name = "attention_level")
    private Integer attentionLevel;

    @Column(name = "persona_device", length = 50)
    private String personaDevice;

    @Column(name = "success_condition", columnDefinition = "TEXT")
    private String successCondition;
}