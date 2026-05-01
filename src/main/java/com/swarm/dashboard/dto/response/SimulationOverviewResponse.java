package com.swarm.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시뮬레이션 개요 응답 DTO (헤더 정보는 목록 API 재사용)")
public class SimulationOverviewResponse {

    // ✅ 헤더 정보(simulationId, siteName, title, status, createdAt) 제거
    // 프론트에서 GET /api/simulations?userId={userId} 응답 데이터 재사용

    @Schema(description = "상단 4개 요약 메트릭 카드")
    private SummaryDto summary;

    @Schema(description = "AI가 감지한 동적 페이지 단계별 전환 패널 목록 (order 오름차순)")
    private List<FunnelPanelDto> funnelPanels;

    // -----------------------------------------------
    // 상단 4개 메트릭 카드
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "상단 요약 메트릭")
    public static class SummaryDto {

        // ✅ DB 매핑: simulation_overview.success_event_count / tested_agent_count * 100
        @Schema(description = "태스크 성공률 (%) = success_event_count / tested_agent_count * 100. DB 컬럼: simulation_overview.conversion_rate 와 동일 개념.", example = "28.0")
        private double taskSuccessRate;

        // ✅ DB 매핑: simulation_overview.tested_agent_count
        @Schema(description = "테스트 AI 에이전트 총 수. DB 컬럼: simulation_overview.tested_agent_count", example = "1000")
        private int totalAgents;

        // ✅ DB 매핑: simulation_overview.avg_completion_ms / 1000
        // ⚠️ DB는 밀리초(ms) 저장 → 응답 시 /1000 변환 필수
        @Schema(
                description = "평균 완료 시간 (초 단위). DB 컬럼: simulation_overview.avg_completion_ms 를 /1000 변환한 값." +
                        " 최종 successCondition 달성 에이전트 기준." +
                        " 프론트 표시: Math.floor(n/60) + '분' + (n%60) + '초'",
                example = "252"
        )
        private int avgCompletionSeconds;

        // ✅ DB 매핑: simulation_overview.tested_agent_count - success_event_count (서버 계산)
        @Schema(description = "이탈 에이전트 수 = tested_agent_count - success_event_count. 서버에서 계산하여 반환.", example = "720")
        private int dropOffAgents;
    }

    // -----------------------------------------------
    // 전환 패널 (AI 감지 동적 페이지 단계)
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "AI가 감지한 페이지 단계별 전환 패널")
    public static class FunnelPanelDto {

        @Schema(description = "단계 순서 (1부터 시작)", example = "1")
        private int order;

        @Schema(description = "AI가 감지한 페이지명", example = "랜딩 페이지")
        private String pageName;

        @Schema(description = "해당 페이지 URL", example = "https://a-mall.com/")
        private String pageUrl;

        @Schema(description = "해당 단계 전체 진입 에이전트 수", example = "1000")
        private int totalEntered;

        @Schema(description = "해당 단계 전체 통과 에이전트 수", example = "850")
        private int totalPassed;

        @Schema(description = "해당 패널 전체 성공률 (%)", example = "85.0")
        private double panelSuccessRate;

        @Schema(
                description = "해당 페이지 전체 평균 체류 시간 (초 단위)." +
                        " 진입한 전체 에이전트 기준 평균.",
                example = "18"
        )
        private int avgTimeSeconds;

        // ✅ [ageBand 통일] Heatmap ageGroup 파라미터와 동일한 한국어 형식으로 변경
        @Schema(
                description = "연령대별 세분화 데이터. 고정 8개 키: " +
                        "'10대','20대','30대','40대','50대','60대','70대'. " +
                        "ratio가 0인 연령대도 entered=0으로 포함."
        )
        private Map<String, AgeGroupDto> agentsByAge;
    }

    // -----------------------------------------------
    // 연령대별 데이터
    // -----------------------------------------------
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "연령대별 에이전트 현황")
    public static class AgeGroupDto {

        @Schema(description = "해당 연령대 진입 수", example = "300")
        private int entered;

        @Schema(description = "해당 연령대 통과 수", example = "270")
        private int passed;

        @Schema(description = "해당 연령대 이탈 수 = entered - passed", example = "30")
        private int dropOff;

        @Schema(description = "해당 연령대 성공률 (%) = passed / entered * 100", example = "90.0")
        private double successRate;
    }
}