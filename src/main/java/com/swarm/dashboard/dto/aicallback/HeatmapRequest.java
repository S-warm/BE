package com.swarm.dashboard.dto.aicallback;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HeatmapRequest(
    List<ErrorPointDto> errorPoints
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorPointDto(
        String issueId,                 // "issue_0", "issue_1" (배열 인덱스)
        String url,
        double x,
        double y,
        String ageBand,                 // "20s"/"50s"/"70s"
        int count,
        String severity,                // 대문자 그대로
        String errorType,               // "사용성/시인성 부족" 한글 그대로
        @JsonAlias({"screenshotUrl", "screenshot_url"}) String screenshotUrl
    ) {}
}
