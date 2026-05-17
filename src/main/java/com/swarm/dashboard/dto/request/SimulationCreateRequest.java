package com.swarm.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "시뮬레이션 생성 요청 DTO")
public class SimulationCreateRequest {

    @NotBlank
    @Schema(description = "시뮬레이션 프로젝트 제목 (사이드바 표시)", example = "test-0515-2")
    private String title;

    @NotBlank
    @Schema(description = "테스트 대상 URL", example = "https://automationexercise.com/products")
    private String targetUrl;

    @NotBlank
    @Schema(description = "AI 에이전트 수행 목표", example = "상품 목록에서 첫 번째 View Product 버튼 클릭")
    private String task;

    @NotBlank
    @Schema(
            description = "디지털 리터러시 수준",
            example = "medium",
            allowableValues = {"high", "medium", "low"}
    )
    private String digitalLiteracy;

    @NotNull
    @Schema(description = "성공 조건 (AI 에이전트의 최종 도달 목표)")
    private SuccessCondition successCondition;

    @NotBlank
    @Schema(
            description = "디바이스 환경",
            example = "desktop",
            allowableValues = {"desktop", "mobile", "tablet"}
    )
    private String personaDevice;

    @NotNull
    @Min(0)
    @Schema(description = "10대 페르소나 수", example = "0")
    private Integer ageCount10;

    @NotNull
    @Min(0)
    @Schema(description = "20대 페르소나 수", example = "2")
    private Integer ageCount20;

    @NotNull
    @Min(0)
    @Schema(description = "30대 페르소나 수", example = "0")
    private Integer ageCount30;

    @NotNull
    @Min(0)
    @Schema(description = "40대 페르소나 수", example = "0")
    private Integer ageCount40;

    @NotNull
    @Min(0)
    @Schema(description = "50대 페르소나 수", example = "0")
    private Integer ageCount50;

    @NotNull
    @Min(0)
    @Schema(description = "60대 페르소나 수", example = "0")
    private Integer ageCount60;

    @NotNull
    @Min(0)
    @Schema(description = "70대 페르소나 수", example = "0")
    private Integer ageCount70;

    @Min(0) @Max(100)
    @Schema(description = "시각 장애 수준 (0 = 정상 시력, 100 = 완전 시각 장애). 선택 항목.", example = "20")
    private Integer visionImpairment;

    @Min(0) @Max(100)
    @Schema(description = "집중력 수준 (0 = 매우 낮음, 100 = 매우 높음). 선택 항목.", example = "70")
    private Integer attentionLevel;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SuccessCondition {
        @NotBlank
        @Schema(description = "도달해야 할 URL path", example = "/product_details")
        private String path;

        @Schema(description = "URL에 포함되어야 할 query params", example = "{}")
        private Map<String, String> requiredParams;
    }
}
