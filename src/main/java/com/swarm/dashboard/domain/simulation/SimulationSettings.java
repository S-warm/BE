package com.swarm.dashboard.domain.simulation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "simulation_settings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationSettings {

    @Id
    @Column(name = "project_id", columnDefinition = "uuid", updatable = false)
    private UUID projectId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "project_id")
    private Simulation project;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(name = "success_condition_path", length = 500)
    private String successConditionPath;

    @Column(name = "success_condition_params", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> successConditionParams;

    @Column(name = "age_count_10s")
    private Integer ageCount10s;

    @Column(name = "age_count_20s")
    private Integer ageCount20s;

    @Column(name = "age_count_30s")
    private Integer ageCount30s;

    @Column(name = "age_count_40s")
    private Integer ageCount40s;

    @Column(name = "age_count_50s")
    private Integer ageCount50s;

    @Column(name = "age_count_60s")
    private Integer ageCount60s;

    @Column(name = "age_count_70s")
    private Integer ageCount70s;
}
