package com.swarm.dashboard.dto.aicallback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WcagRequest(
    Map<String, WcagUrlResultDto> urls   // URL이 키
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WcagUrlResultDto(
        int score,
        String wcagLabel,                    // "AAA"/"AA"/"A"/"미달"
        DistributionDto distribution,
        List<WcagViolationDto> violations,
        String screenshotUrl,
        String error                         // null이면 정상, 있으면 분석 실패 → skip
    ) {}

    public record DistributionDto(
        @JsonProperty("Critical") int critical,
        @JsonProperty("Moderate") int moderate,
        @JsonProperty("Minor") int minor
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WcagViolationDto(
        String wcagIssueId,                                 // 무시 (Spring이 새 UUID 발급)
        String title,                                       // upsert 키
        String severity,                                    // PascalCase 그대로
        String description,
        String html,
        @JsonProperty("wcag_criteria") String wcagCriteria  // "1.4.3"
    ) {}
}
