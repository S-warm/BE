package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.swarm.dashboard.domain.wcag.WcagSeverity;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WCAG 검사 탭 응답 DTO")
public class SimulationWcagResponse {

    @Schema(description = "WCAG 점수 (0~100)")
    private Integer score;

    @Schema(description = "WCAG 등급 (AAA/AA/A/미달)")
    private String wcagLabel;

    @Schema(description = "Critical 위반 수")
    private Integer distributionCritical;

    @Schema(description = "Moderate 위반 수")
    private Integer distributionModerate;

    @Schema(description = "Minor 위반 수")
    private Integer distributionMinor;

    @Schema(description = "WCAG 이슈 목록 (Critical → Moderate → Minor 순)")
    private List<WcagIssueDto> issues;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "WCAG 개별 이슈")
    public static class WcagIssueDto {

        @Schema(description = "WCAG 이슈 고유 ID")
        private UUID wcagIssueId;

        @Schema(description = "이슈 제목")
        private String title;

        @Schema(description = "심각도 (Critical/Moderate/Minor)")
        private WcagSeverity severity;

        @Schema(description = "이슈 설명")
        private String description;

        @Schema(description = "위반된 DOM 요소 HTML")
        private String html;

        @Schema(description = "WCAG 기준 번호", example = "1.4.3")
        private String wcagCriteria;
    }
}
