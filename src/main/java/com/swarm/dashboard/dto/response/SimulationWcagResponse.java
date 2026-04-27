package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// ✅ 문제 3 B안 — 페이지별 구조 → 시뮬레이션 단위 단일 구조로 변경
// WcagPageDto 래퍼 제거, summary / distribution / issues 직접 노출
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WCAG 검사 탭 응답 DTO (시뮬레이션 단위 통합 결과)")
public class SimulationWcagResponse {

    @Schema(description = "WCAG 2.1 전체 요약 메트릭")
    private WcagSummaryDto summary;

    @Schema(description = "심각도별 이슈 분포")
    private WcagDistributionDto distribution;

    @Schema(description = "WCAG 이슈 목록 (Critical → Moderate → Minor 순)")
    private List<WcagIssueDto> issues;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "WCAG 상단 요약 메트릭")
    public static class WcagSummaryDto {
        // ✅ DB 매핑: wcag_results.compliance_score (DB는 INT, DTO는 double)
        // ⚠️ DB 연동 시 Integer → double 캐스팅 필요: (double) wcagResult.getComplianceScore()
        @Schema(description = "WCAG 2.1 Level AA 준수 점수 (%). 계산: passedTests / totalTests * 100. DB 컬럼: wcag_results.compliance_score (INT → double 변환)", example = "52.0")
        private double complianceScore;
        @Schema(description = "WCAG 준수 등급. 허용값: A / AA / AAA", example = "AA")
        private String wcagLabel;
        @Schema(description = "전체 테스트 항목 수", example = "20")
        private int totalTests;
        @Schema(description = "통과된 테스트 수", example = "9")
        private int passedTests;
        @Schema(description = "발견된 이슈 총 수 = Critical + Moderate + Minor", example = "14")
        private int foundIssues;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "검출 이슈 분석 — Critical / Moderate / Minor 분포")
    public static class WcagDistributionDto {
        @Schema(description = "Critical 이슈 수", example = "4")
        private int critical;
        @Schema(description = "Moderate 이슈 수", example = "6")
        private int moderate;
        @Schema(description = "Minor 이슈 수", example = "4")
        private int minor;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "WCAG 개별 이슈")
    public static class WcagIssueDto {
        @Schema(description = "WCAG 이슈 고유 ID", example = "1")
        private Long wcagIssueId;
        @Schema(description = "이슈 제목", example = "텍스트 대비율")
        private String title;
        @Schema(description = "심각도. 허용값: Critical / Moderate / Minor", example = "Critical")
        private String severity;
        @Schema(description = "이슈 설명 (자세히 보기 펼침 시 노출). AI 생성.", example = "본문/보조 텍스트의 대비가 WCAG 2.1 AA 기준을 충족하지 않아 저시력 사용자의 가독성이 저하됩니다.")
        private String description;
    }
}
