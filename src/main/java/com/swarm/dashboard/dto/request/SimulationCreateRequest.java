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

    @NotNull
    @Min(1)
    @Schema(description = "총 페르소나 수", example = "500")
    private Integer personaCount;

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

    // ✅ ratio 필드명 DB simulation_settings 컬럼명으로 통일
    @NotNull
    @Min(0) @Max(100)
    @Schema(description = "10~40대 페르소나 비율 (%)", example = "25")
    private Integer ageRatioTeen;

    @NotNull
    @Min(0) @Max(100)
    @Schema(description = "50~60대 페르소나 비율 (%)", example = "25")
    private Integer ageRatioFifty;

    @NotNull
    @Min(0) @Max(100)
    @Schema(description = "70~80대 페르소나 비율 (%)", example = "50")
    private Integer ageRatioEighty;

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