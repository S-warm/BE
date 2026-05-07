package com.swarm.dashboard.dto.aicallback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverviewRequest(
    SummaryDto summary,
    List<AgeOverviewDto> overview
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SummaryDto(
        @JsonProperty("total_sessions") int totalSessions,
        @JsonProperty("success_count") int successCount,
        @JsonProperty("success_rate") double successRate,
        @JsonProperty("avg_duration_ms") long avgDurationMs
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AgeOverviewDto(
        @JsonProperty("age_group") String ageGroup,           // "20s"/"50s"/"70s"
        @JsonProperty("total_sessions") int totalSessions,
        @JsonProperty("success_count") int successCount,
        @JsonProperty("success_rate") double successRate,
        @JsonProperty("fail_rate") double failRate,
        @JsonProperty("avg_duration_ms") long avgDurationMs,
        @JsonProperty("avg_actions") double avgActions,
        @JsonProperty("avg_declare_failure") double avgDeclareFailure
    ) {}
}
