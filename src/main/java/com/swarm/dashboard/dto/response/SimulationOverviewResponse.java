package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시뮬레이션 개요 응답 DTO")
public class SimulationOverviewResponse {

    @Schema(description = "전체 세션 수")
    private Integer totalSessions;

    @Schema(description = "성공 세션 수")
    private Integer successCount;

    @Schema(description = "전체 성공률 (소수 4자리)")
    private BigDecimal successRate;

    @Schema(description = "전체 평균 완료 시간 (ms)")
    private Long avgDurationMs;

    @Schema(description = "연령대별 개요 목록")
    private List<AgeOverviewDto> ageOverview;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "연령대별 개요 데이터")
    public static class AgeOverviewDto {

        @Schema(description = "연령대 (한글 변환)", example = "20대")
        private String ageBand;

        @Schema(description = "해당 연령대 총 세션 수")
        private Integer totalSessions;

        @Schema(description = "성공 세션 수")
        private Integer successCount;

        @Schema(description = "성공률")
        private BigDecimal successRate;

        @Schema(description = "실패율")
        private BigDecimal failRate;

        @Schema(description = "평균 완료 시간 (ms)")
        private Long avgDurationMs;

        @Schema(description = "평균 액션 수")
        private BigDecimal avgActions;

        @Schema(description = "평균 declare_failure 횟수")
        private BigDecimal avgDeclareFailure;
    }
}
