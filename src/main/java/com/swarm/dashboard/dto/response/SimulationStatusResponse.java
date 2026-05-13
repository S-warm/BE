package com.swarm.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulationStatusResponse {
    private String status;      // pending | running | analyzing | completed | failed
    private Integer completed;
    private Integer total;
    private Integer failed;
}
