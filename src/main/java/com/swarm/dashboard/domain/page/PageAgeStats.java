package com.swarm.dashboard.domain.page;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "page_age_stats")
@Getter
@NoArgsConstructor
public class PageAgeStats {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private SimulationPage page;

    @Column(name = "age_band", length = 10, nullable = false)
    private String ageBand;

    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;

    @Column
    private Integer entered;

    @Column
    private Integer passed;

    @Column(name = "drop_off")
    private Integer dropOff;
}