package com.swarm.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "시뮬레이션 생성 요청 DTO")
public class SimulationCreateRequest {

    // ✅ siteName 제거 — title로 통일 (문제 1 B안)

    @NotBlank
    @Schema(description = "시뮬레이션 프로젝트 제목 (사이드바 표시)", example = "Q1 2026 체크아웃 플로우 UX 테스트")
    private String title;

    @NotBlank
    @Schema(description = "테스트 대상 URL", example = "https://shopping-mall.com/checkout")
    private String targetUrl;

    @NotBlank
    @Schema(
            description = "디지털 리터러시 수준",
            example = "medium",
            allowableValues = {"high", "medium", "low"}
    )
    private String digitalLiteracy;

    @NotBlank
    @Schema(description = "성공 조건 (AI 에이전트의 최종 도달 목표)", example = "결제 완료 페이지 도달")
    private String successCondition;

    @NotBlank
    @Schema(
            description = "디바이스 환경",
            example = "desktop",
            allowableValues = {"desktop", "mobile", "tablet"}
    )
    private String personaDevice;

    @NotNull
    @Min(0)
    @Schema(description = "10대 페르소나 수", example = "100")
    private Integer ageCount10;

    @NotNull
    @Min(0)
    @Schema(description = "20대 페르소나 수", example = "150")
    private Integer ageCount20;

    @NotNull
    @Min(0)
    @Schema(description = "30대 페르소나 수", example = "150")
    private Integer ageCount30;

    @NotNull
    @Min(0)
    @Schema(description = "40대 페르소나 수", example = "100")
    private Integer ageCount40;

    @NotNull
    @Min(0)
    @Schema(description = "50대 페르소나 수", example = "100")
    private Integer ageCount50;

    @NotNull
    @Min(0)
    @Schema(description = "60대 페르소나 수", example = "100")
    private Integer ageCount60;

    @NotNull
    @Min(0)
    @Schema(description = "70대 페르소나 수", example = "100")
    private Integer ageCount70;

    // ✅ [M-2] SimulationSettings.visionImpairment / attentionLevel 추가
    @Min(0) @Max(100)
    @Schema(
            description = "시각 장애 수준 (0 = 정상 시력, 100 = 완전 시각 장애). 선택 항목.",
            example = "20"
    )
    private Integer visionImpairment;

    @Min(0) @Max(100)
    @Schema(
            description = "집중력 수준 (0 = 매우 낮음, 100 = 매우 높음). 선택 항목.",
            example = "70"
    )
    private Integer attentionLevel;
}