package com.swarm.dashboard.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)  // 프론트 mock 필드 무시
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "시뮬레이션 생성 요청 DTO")
public class SimulationCreateRequest {

    @NotBlank
    @Schema(description = "시뮬레이션 프로젝트 제목", example = "DBpia UX 테스트")
    private String title;

    @NotBlank
    @Schema(description = "테스트 대상 URL", example = "https://dbpia.co.kr")
    private String targetUrl;

    @Schema(description = "시뮬레이션 목표", example = "검색창에 '파운데이션 모델' 검색하고 논문 상세 페이지까지 이동")
    private String goal;

    @Schema(description = "성공 조건 경로", example = "/journal/articleDetail")
    private String successConditionPath;

    @Schema(description = "성공 조건 파라미터", example = "{\"nodeId\": \"NODE12728926\"}")
    private Map<String, Object> successConditionParams;

    @Min(0)
    @Schema(description = "10대 페르소나 수", example = "0")
    private Integer ageCount10s;

    @Min(0)
    @Schema(description = "20대 페르소나 수", example = "300")
    private Integer ageCount20s;

    @Min(0)
    @Schema(description = "30대 페르소나 수", example = "0")
    private Integer ageCount30s;

    @Min(0)
    @Schema(description = "40대 페르소나 수", example = "0")
    private Integer ageCount40s;

    @Min(0)
    @Schema(description = "50대 페르소나 수", example = "300")
    private Integer ageCount50s;

    @Min(0)
    @Schema(description = "60대 페르소나 수", example = "0")
    private Integer ageCount60s;

    @Min(0)
    @Schema(description = "70대 페르소나 수", example = "300")
    private Integer ageCount70s;

    // 제거된 필드들은 @JsonIgnoreProperties로 무시:
    //   digitalLiteracy, personaDevice, visionImpairment, attentionLevel, successCondition(구버전)
}
