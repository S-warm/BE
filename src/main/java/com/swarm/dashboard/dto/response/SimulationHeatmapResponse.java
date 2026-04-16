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
@Schema(description = "히트맵 탭 응답 DTO")
public class SimulationHeatmapResponse {

    @Schema(description = "페이지별 히트맵 데이터 목록 (order 오름차순)")
    private List<HeatmapPageDto> pages;

    // -----------------------------------------------
    // 페이지 단위
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지별 히트맵 데이터")
    public static class HeatmapPageDto {

        @Schema(description = "페이지 순서 (funnelPanels order와 동일)", example = "1")
        private int order;

        @Schema(description = "AI가 감지한 페이지명", example = "로그인 페이지")
        private String pageName;

        @Schema(description = "페이지 URL", example = "https://a-mall.com/login")
        private String pageUrl;

        @Schema(
                description = "히트맵 배경 스크린샷 URL. Issues 탭 screenshotUrl과 동일한 이미지.",
                example = "https://storage.example.com/screenshots/sim42_page1.png"
        )
        private String screenshotUrl;

        @Schema(description = "해당 페이지 총 오류 발생 수", example = "12")
        private int totalErrorCount;

        @Schema(description = "오류 좌표 목록 (일렬 나열). 각 항목의 ageBand 필드로 연령대 구분.")
        private List<ErrorPointDto> errorPoints;

        @Schema(description = "현재 요청한 연령대 필터 (all, 10대, 20대, ...)", example = "all")
        private String currentAgeGroup;

        @Schema(description = "페이징 정보")
        private PaginationDto pagination;
    }

    // -----------------------------------------------
    // 오류 좌표 단위 (팝업 데이터 포함)
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "오류 집중 지점 좌표 및 팝업 데이터")
    public static class ErrorPointDto {

        @Schema(
                description = "정규화 X 좌표 (0~1). 스크린샷 width 기준 비율.",
                example = "0.72"
        )
        private double x;

        @Schema(
                description = "정규화 Y 좌표 (0~1). 스크린샷 height 기준 비율.",
                example = "0.35"
        )
        private double y;

        @Schema(
                description = "오류 발생 횟수. 히트맵 강도(낮음/보통/높음/치명적) 계산 기준.",
                example = "15"
        )
        private int count;

        @Schema(
                description = "오류 강도. 허용값: LOW / MEDIUM / HIGH / CRITICAL. " +
                        "count 기준 계산: 1~3=LOW, 4~7=MEDIUM, 8~14=HIGH, 15+=CRITICAL",
                example = "CRITICAL"
        )
        private String severity;

        @Schema(
                description = "대표 오류 타입. 허용값: Timeout / Network / Console",
                example = "Timeout"
        )
        private String errorType;

        @Schema(description = "영향받은 에이전트 수", example = "2")
        private int affectedUsersCount;

        @Schema(description = "블락 비율 (%) = 해당 지점에서 블락된 에이전트 / 진입 에이전트 * 100", example = "100.0")
        private double blockRate;

        @Schema(description = "평균 반복 시도 횟수", example = "4.5")
        private double repeatCount;

        @Schema(
                description = "오류 집중 지점 설명 텍스트 (AI 생성)",
                example = "클릭/스텝 로그에서 Timeout·Network·Console 오류가 집중된 구간입니다. 주변 UI에서 반복 시도/지연이 발생합니다."
        )
        private String description;

        @Schema(description = "오류 타입별 세부 카운트 (팝업 하단 breakdown)")
        private ErrorBreakdownDto errorBreakdown;

        @Schema(description = "연관 이슈 ID (Issues 탭 issueId와 동일한 키, 없으면 null)", example = "550e8400-e29b-41d4-a716-446655440001")
        private UUID issueId;

        @Schema(
                description = "이 오류점이 속한 연령대 (all, 10대, 20대, ...)",
                example = "all"
        )
        private String ageBand;
    }

    // -----------------------------------------------
    // 오류 타입별 세부 카운트
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "오류 타입별 세부 발생 카운트 (팝업 하단 표시)")
    public static class ErrorBreakdownDto {

        @Schema(description = "Timeout 오류 발생 수", example = "2")
        private int timeout;

        @Schema(description = "Network 오류 발생 수", example = "0")
        private int network;

        @Schema(description = "Console 오류 발생 수", example = "0")
        private int console;
    }

    // -----------------------------------------------
    // 페이징 정보
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이징 메타데이터")
    public static class PaginationDto {

        @Schema(description = "전체 오류점 개수 (해당 연령대 기준)", example = "2000")
        private long totalCount;

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int currentPage;

        @Schema(description = "한 페이지당 항목 수", example = "100")
        private int pageSize;

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        private boolean hasMore;
    }
}
