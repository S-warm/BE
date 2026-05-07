package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "시뮬레이션 생성 응답 DTO")
public class SimulationCreateResponse {

    @Schema(description = "생성된 프로젝트 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID projectId;

    @Schema(description = "시뮬레이션 제목", example = "DBpia UX 테스트")
    private String title;

    @Schema(description = "시뮬레이션 상태", example = "pending")
    private String status;

    @Schema(description = "생성 일시", example = "2026-04-11T10:30:45+09:00")
    private OffsetDateTime createdAt;
}
