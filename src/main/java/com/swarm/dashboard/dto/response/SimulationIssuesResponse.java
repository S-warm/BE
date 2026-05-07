package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.swarm.dashboard.domain.issue.IssueSeverity;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주요 이슈 탭 응답 DTO")
public class SimulationIssuesResponse {

    @Schema(description = "페이지별 이슈 목록")
    private List<IssuePageDto> pages;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지별 이슈 그룹")
    public static class IssuePageDto {

        @Schema(description = "페이지 순서", example = "1")
        private int order;

        @Schema(description = "페이지 URL")
        private String pageUrl;

        @Schema(description = "스크린샷 URL")
        private String screenshotUrl;

        @Schema(description = "해당 페이지 총 이슈 수")
        private int totalIssueCount;

        @Schema(description = "해당 페이지 이슈 목록 (severity 높은 순)")
        private List<IssueDto> issues;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 이슈")
    public static class IssueDto {

        @Schema(description = "이슈 고유 ID")
        private UUID issueId;

        @Schema(description = "이슈 제목")
        private String title;

        @Schema(description = "카테고리")
        private String category;

        @Schema(description = "서브카테고리")
        private String subCategory;

        @Schema(description = "심각도 (HIGH/MEDIUM/LOW)")
        private IssueSeverity severity;

        @Schema(description = "이슈 발생 세션 수")
        private Integer failCount;

        @Schema(description = "영향받은 사용자 수 (age_stats 합산)")
        private int affectedUsersCount;

        @Schema(description = "이슈 상세 설명")
        private String description;

        @Schema(description = "문제 요소 자연어 요약")
        private String targetHtml;

        @Schema(description = "태그 목록")
        private List<String> tags;
    }
}
