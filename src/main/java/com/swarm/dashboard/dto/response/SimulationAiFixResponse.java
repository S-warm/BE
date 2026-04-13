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
@Schema(description = "AI 수정 탭 응답 DTO")
public class SimulationAiFixResponse {

    @Schema(description = "페이지별 AI 수정 목록 (AI 감지 순서)")
    private List<AiFixPageDto> pages;

    // -----------------------------------------------
    // 페이지 단위
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지별 AI 수정 그룹")
    public static class AiFixPageDto {

        @Schema(description = "페이지 순서 (funnelPanels order와 동일)", example = "1")
        private int order;

        @Schema(description = "AI가 감지한 페이지명", example = "로그인 페이지")
        private String pageName;

        @Schema(description = "페이지 URL", example = "https://a-mall.com/login")
        private String pageUrl;

        @Schema(
                description = "AI 에이전트가 탐색 중 캡처한 스크린샷 URL",
                example = "https://storage.example.com/screenshots/sim42_page1.png"
        )
        private String screenshotUrl;

        @Schema(description = "해당 페이지 총 수정 제안 수", example = "3")
        private int totalFixCount;

        @Schema(description = "해당 페이지 AI 수정 목록")
        private List<AiFixDto> fixes;
    }

    // -----------------------------------------------
    // 개별 AI 수정 단위
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 AI 수정 제안")
    public static class AiFixDto {

        @Schema(description = "이슈 고유 ID (Issues 탭 issueId와 동일한 키)", example = "1")
        private Long issueId;

        @Schema(description = "이슈 제목", example = "입력 레이블이 낮은 대비율")
        private String title;

        @Schema(description = "심각도. 허용값: High / Medium / Low", example = "High")
        private String severity;

        @Schema(description = "영향받은 에이전트 수", example = "142")
        private int affectedUsersCount;

        @Schema(description = "수정 전 코드", example = ".form-label { color: #999999; font-size: 14px; }")
        private String beforeCode;

        @Schema(description = "AI 생성 수정 후 코드", example = ".form-label { color: #334155; font-size: 14px; font-weight: 500; }")
        private String afterCode;

        @Schema(
                description = "수정 적용 시 영향 설명 (AI 생성)",
                example = "142명의 사용자가 이제 레이블을 명확하게 읽을 수 있음"
        )
        private String impactDescription;

        @Schema(
                description = "변경 내용 요약 (AI 생성, '무엇이 변경되었나요?' 섹션)",
                example = "레이블 색상을 #999999에서 #334155로 변경하여 대비율을 달성하고 WCAG의 표준을 충족합니다."
        )
        private String changeDescription;
    }
}