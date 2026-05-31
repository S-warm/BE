package com.swarm.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "목업 시뮬레이션 생성 요청 DTO")
public class MockSimulationRequest {

    @NotBlank
    @Schema(description = "시뮬레이션 프로젝트 제목", example = "muun site test v6 260525")
    private String title;

    @NotBlank
    @Schema(description = "테스트 대상 URL", example = "http://muun-shop-demo.s3-website.ap-northeast-2.amazonaws.com")
    private String targetUrl;

    @NotBlank
    @Schema(description = "S3에 업로드된 done.json의 jobId", example = "mock-muun-v6-260525")
    private String jobId;
}
