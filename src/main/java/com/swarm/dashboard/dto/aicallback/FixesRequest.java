package com.swarm.dashboard.dto.aicallback;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixesRequest(
    List<FixUrlDto> fixes
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixUrlDto(
        String url,
        List<FixItemDto> fixes,
        @JsonAlias({"screenshotUrl", "screenshot_url"}) String screenshotUrl
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixItemDto(
        @JsonProperty("issue_title") String issueTitle,
        String selector,
        String before,
        String after,
        String description,
        String impact,
        String error            // null=정상, 있으면 AI 생성 실패 → skip
    ) {}
}
