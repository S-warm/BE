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

    @Column(name = "age_ratio_teen")
    private Integer ageRatioTeen;

    @Column(name = "age_ratio_fifty")
    private Integer ageRatioFifty;

    @Column(name = "age_ratio_eighty")
    private Integer ageRatioEighty;

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