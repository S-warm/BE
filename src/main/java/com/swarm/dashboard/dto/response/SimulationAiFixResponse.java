package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 수정 탭 응답 DTO")
public class SimulationAiFixResponse {

    @Schema(description = "페이지별 AI 수정 목록")
    private List<AiFixPageDto> pages;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지별 AI 수정 그룹")
    public static class AiFixPageDto {

        @Schema(description = "페이지 순서", example = "1")
        private int order;

        @Schema(description = "페이지 URL")
        private String pageUrl;

        @Schema(description = "스크린샷 URL")
        private String screenshotUrl;

        @Schema(description = "해당 페이지 총 수정 제안 수")
        private int totalFixCount;

        @Schema(description = "해당 페이지 AI 수정 목록")
        private List<AiFixDto> fixes;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 AI 수정 제안")
    public static class AiFixDto {

        @Schema(description = "이슈 고유 ID")
        private UUID issueId;

        @Schema(description = "CSS 셀렉터")
        private String selector;

        @Schema(description = "수정 전 코드")
        private String beforeCode;

        @Schema(description = "수정 후 코드")
        private String afterCode;

        @Schema(description = "수정 적용 시 영향 설명")
        private String impactDescription;

        @Schema(description = "변경 내용 요약")
        private String changeDescription;
    }
}
