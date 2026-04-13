package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주요 이슈 탭 응답 DTO")
public class SimulationIssuesResponse {

    @Schema(description = "페이지별 이슈 목록 (AI 감지 순서)")
    private List<IssuePageDto> pages;

    // -----------------------------------------------
    // 페이지 단위
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지별 이슈 그룹")
    public static class IssuePageDto {

        @Schema(description = "페이지 순서 (funnelPanels order와 동일)", example = "1")
        private int order;

        @Schema(description = "AI가 감지한 페이지명", example = "로그인 페이지")
        private String pageName;

        @Schema(description = "페이지 URL", example = "https://a-mall.com/login")
        private String pageUrl;

        @Schema(
                description = "AI 에이전트가 탐색 중 캡처한 스크린샷 URL. " +
                        "추후 DB/스토리지 연동 후 실제 URL로 교체 예정.",
                example = "https://storage.example.com/screenshots/sim42_page1.png"
        )
        private String screenshotUrl;

        @Schema(description = "해당 페이지 총 이슈 수", example = "3")
        private int totalIssueCount;

        @Schema(description = "해당 페이지 이슈 목록 (카테고리별 그룹 없이 severity 높은 순 정렬)")
        private List<IssueDto> issues;
    }

    // -----------------------------------------------
    // 개별 이슈 단위
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 이슈")
    public static class IssueDto {

        @Schema(description = "이슈 고유 ID (AI 수정 탭 연동 기준 키)", example = "1")
        private Long issueId;

        @Schema(description = "이슈 제목", example = "입력 레이블이 낮은 대비율")
        private String title;

        @Schema(
                description = "이슈 카테고리. 허용값: Usability / Accessibility / Performance / Layout",
                example = "Usability"
        )
        private String category;

        @Schema(
                description = "심각도. 허용값: High / Medium / Low",
                example = "High"
        )
        private String severity;

        @Schema(description = "영향받은 에이전트 수", example = "142")
        private int affectedUsersCount;

        @Schema(description = "영향받은 에이전트 비율 (%)", example = "14.2")
        private double affectedUsersPercent;

        @Schema(description = "이슈 상세 설명 (AI 생성 텍스트)", example = "흰색 배경 위의 회색 텍스트로 인해 WCAG 2.1 AA 기준(4.5:1) 미달")
        private String description;

        // ✅ selector → targetHtml (DB issues.target_html 컬럼명 통일)
        @Schema(description = "문제 요소 CSS 선택자", example = ".form-label")
        private String targetHtml;

        // ✅ @Schema 순서 수정 (기존 selector/tags 설명이 뒤바뀌어 있었음)
        @Schema(description = "연관 태그 목록", example = "[\"contrast\", \"wcag_aa\"]")
        private List<String> tags;
    }
}