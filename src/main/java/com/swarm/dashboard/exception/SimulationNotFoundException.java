package com.swarm.dashboard.exception;

import java.util.UUID;

public class SimulationNotFoundException extends ResourceNotFoundException {

    public SimulationNotFoundException(UUID simulationId) {
        super("시뮬레이션을 찾을 수 없습니다. id=" + simulationId);
    }
}
