package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "시뮬레이션 목록 조회 응답 DTO (사이드바 전용)")
public class SimulationListResponse {

    @Schema(description = "시뮬레이션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    // ✅ siteName 제거 — title로 사이드바 표시

    @Schema(description = "시뮬레이션 제목 (사이드바 표시)", example = "Q1 2026 체크아웃 플로우 UX 테스트")
    private String title;

    @Schema(
            description = "시뮬레이션 상태",
            example = "completed",
            allowableValues = {"pending", "running", "completed", "failed"}
    )
    private String status;

    @Schema(description = "생성 일시", example = "2026-04-11T10:30:45+09:00")
    private OffsetDateTime createdAt;
}