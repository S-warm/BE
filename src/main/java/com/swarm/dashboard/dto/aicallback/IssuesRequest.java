package com.swarm.dashboard.dto.aicallback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IssuesRequest(
    @JsonProperty("total_issues") int totalIssues,
    List<IssueDto> issues
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueDto(
        String url,
        String category,
        String subCategory,
        String severity,                         // 소문자 → toUpperCase 처리 필요
        String title,
        String description,
        String targetHtml,
        List<String> tags,
        @JsonProperty("fail_count") int failCount,
        @JsonProperty("fail_rate") double failRate,
        @JsonProperty("affected_personas") List<AffectedPersonaDto> affectedPersonas,
        @JsonProperty("screenshotUrl") String screenshotUrl
        // session_ids, persona_ages는 무시 (선언 안 함)
    ) {}

    public record AffectedPersonaDto(
        @JsonProperty("session_id") String sessionId,
        @JsonProperty("persona_age") String personaAge      // "20s" 영문
    ) {}
}
