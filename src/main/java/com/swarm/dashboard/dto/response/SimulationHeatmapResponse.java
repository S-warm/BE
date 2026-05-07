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

    @Schema(description = "페이지별 히트맵 데이터 목록")
    private List<HeatmapPageDto> pages;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이지별 히트맵 데이터")
    public static class HeatmapPageDto {

        @Schema(description = "페이지 순서", example = "1")
        private int order;

        @Schema(description = "페이지 URL")
        private String pageUrl;

        @Schema(description = "히트맵 배경 스크린샷 URL")
        private String screenshotUrl;

        @Schema(description = "해당 페이지 총 오류 발생 수")
        private int totalErrorCount;

        @Schema(description = "오류 좌표 목록")
        private List<ErrorPointDto> errorPoints;

        @Schema(description = "현재 요청한 연령대 필터")
        private String currentAgeGroup;

        @Schema(description = "페이징 정보")
        private PaginationDto pagination;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "오류 집중 지점 좌표")
    public static class ErrorPointDto {

        @Schema(description = "정규화 X 좌표 (0~1)", example = "0.124")
        private double x;

        @Schema(description = "정규화 Y 좌표 (0~1)", example = "0.213")
        private double y;

        @Schema(description = "클러스터 내 좌표 수 (히트맵 강도)", example = "11")
        private int count;

        @Schema(description = "심각도 (HIGH/MEDIUM/LOW/CRITICAL)", example = "HIGH")
        private String severity;

        @Schema(description = "에러 유형 (한글)", example = "사용성/시인성 부족")
        private String errorType;

        @Schema(description = "연령대 (한글)", example = "70대")
        private String ageBand;

        @Schema(description = "연관 이슈 ID (없으면 null)")
        private UUID issueId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "페이징 메타데이터")
    public static class PaginationDto {

        @Schema(description = "전체 오류점 개수")
        private long totalCount;

        @Schema(description = "현재 페이지 번호 (0부터 시작)")
        private int currentPage;

        @Schema(description = "한 페이지당 항목 수")
        private int pageSize;

        @Schema(description = "다음 페이지 존재 여부")
        private boolean hasMore;
    }
}
