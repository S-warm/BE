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

        @Schema(description = "이슈 고유 ID (AI 수정 탭 연동 기준 키)", example = "550e8400-e29b-41d4-a716-446655440001")
        private UUID issueId;

        @Schema(description = "이슈 제목", example = "입력 레이블이 낮은 대비율")
        private String title;

        @Schema(
                description = "이슈 카테고리. 허용값: Usability / Accessibility / Performance / Layout",
                example = "Usability"
        )
        private String category;

        @Schema(
                description = "심각도. 허용값: CRITICAL / HIGH / MEDIUM / LOW",
                example = "HIGH"
        )
        private IssueSeverity severity;

        // ✅ DB 매핑: issue_age_stats.affected_users 를 연령대별로 합산한 값
        // ⚠️ issues 테이블에 직접 저장된 컬럼이 아님 — DB 연동 시 SUM(affected_users) 집계 필요
        @Schema(description = "영향받은 에이전트 수. issue_age_stats.affected_users 합산값.", example = "142")
        private int affectedUsersCount;

        // ✅ DB 매핑: issue_age_stats.affected_percent 를 연령대별로 평균낸 값
        @Schema(description = "영향받은 에이전트 비율 (%). issue_age_stats.affected_percent 평균값.", example = "14.2")
        private double affectedUsersPercent;

        @Schema(description = "이슈 상세 설명 (AI 생성 텍스트). DB 컬럼: issues.description", example = "흰색 배경 위의 회색 텍스트로 인해 WCAG 2.1 AA 기준(4.5:1) 미달")
        private String description;

        @Schema(description = "문제 요소 CSS 선택자 또는 HTML 스니펫. DB 컬럼: issues.target_html", example = ".form-label")
        private String targetHtml;

        @Schema(description = "연관 태그 목록. DB 컬럼: issues.tags (JSONB). 예: [\"contrast\", \"wcag_aa\"]", example = "[\"contrast\", \"wcag_aa\"]")
        private List<String> tags;

        // ───────────────────────────────────────────
        // TODO: 아래 DB 필드들은 현재 API 응답에 미포함.
        //       프론트 기획 확정 후 필요 시 추가하세요.
        // ───────────────────────────────────────────
        // issues.sub_category  VARCHAR(100) — 세부 카테고리 (예: "대비율", "키보드 접근성")
        // issues.benefit_label VARCHAR(100) — 개선 효과 레이블 (예: "전환율 +3%")
        // issues.benefit_delta VARCHAR(20)  — 개선 예상 수치 (예: "+3%")
    }
}