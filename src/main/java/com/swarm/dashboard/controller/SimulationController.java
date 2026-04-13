package com.swarm.dashboard.controller;

import com.swarm.dashboard.dto.request.SimulationCreateRequest;
import com.swarm.dashboard.dto.response.SimulationAiFixResponse;
import com.swarm.dashboard.dto.response.SimulationCreateResponse;
import com.swarm.dashboard.dto.response.SimulationHeatmapResponse;
import com.swarm.dashboard.dto.response.SimulationIssuesResponse;
import com.swarm.dashboard.dto.response.SimulationListResponse;
import com.swarm.dashboard.dto.response.SimulationOverviewResponse;
import com.swarm.dashboard.dto.response.SimulationWcagResponse;
import com.swarm.dashboard.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation", description = "시뮬레이션 생성 및 관리 API")
public class SimulationController {

    private final SimulationService simulationService;

    // ────────────────────────────────────────
    // POST /api/simulations?userId={userId}
    // ────────────────────────────────────────
    @PostMapping
    @Operation(
            summary = "시뮬레이션 생성",
            description = """
            새로운 UX 시뮬레이션 프로젝트를 생성합니다.
            
            - device 허용값: Mac / Windows / iPhone / Android / iPad / AndroidTablet
            - digitalLiteracy 허용값: High / Mid / Low
            - 연령대별 비율(ratio10s + ratio40s + ratio60s) 합계는 프론트엔드에서 100%로 보장됩니다.
            - userId 인증은 추후 JWT 방식으로 교체 예정입니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "시뮬레이션 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulationCreateResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "id": 42,
                      "siteName": "ShoppingMall",
                      "status": "pending",
                      "createdAt": "2026-04-11T10:30:45"
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "필수 필드 누락 또는 유효성 검사 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 400, "error": "Bad Request", "message": "필수 입력값이 누락되었습니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<SimulationCreateResponse> createSimulation(
            @Parameter(description = "사용자 ID (임시 query param, 추후 JWT 교체 예정)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID userId,
            @Valid @RequestBody SimulationCreateRequest request
    ) {
        SimulationCreateResponse response = simulationService.createSimulation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ────────────────────────────────────────
    // GET /api/simulations?userId={userId}
    // ────────────────────────────────────────
    @GetMapping
    @Operation(
            summary = "시뮬레이션 목록 조회 (사이드바용)",
            description = """
            특정 사용자의 시뮬레이션 목록을 최신순으로 반환합니다.
            사이드바 전용 경량 응답입니다. (id, siteName, title, status, createdAt)
            userId 인증은 추후 JWT 방식으로 교체 예정입니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    [
                      {
                        "id": 42,
                        "siteName": "ShoppingMall",
                        "title": "Q1 2026 체크아웃 플로우 UX 테스트",
                        "status": "completed",
                        "createdAt": "2026-04-11T10:30:45"
                      },
                      {
                        "id": 41,
                        "siteName": "Fiora",
                        "title": "메인 페이지 UX 테스트",
                        "status": "pending",
                        "createdAt": "2026-04-10T09:00:00"
                      }
                    ]
                    """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "userId 누락",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 400, "error": "Bad Request", "message": "userId는 필수입니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<List<SimulationListResponse>> getSimulations(
            @Parameter(description = "사용자 ID (임시 query param, 추후 JWT 교체 예정)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam UUID userId
    ) {
        List<SimulationListResponse> response = simulationService.getSimulationsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // ────────────────────────────────────────
    // GET /api/simulations/{simulationId}/overview
    // ────────────────────────────────────────
    @GetMapping("/{simulationId}/overview")
    @Operation(
            summary = "시뮬레이션 개요 조회 (Overview 탭)",
            description = """
            시뮬레이션 완료 후 Overview 탭 데이터를 반환합니다.

            [헤더 정보 없음 - 프론트 처리 방식]
            - simulationId, siteName, title, status, createdAt 은 이 API에 포함되지 않습니다.
            - GET /api/simulations?userId={userId} 목록 응답에서 해당 simulationId로 find() 하여 재사용하세요.

            [summary - 상단 4개 메트릭 카드]
            - taskSuccessRate      : 최종 페이지 통과 수 / totalAgents * 100 (%)
            - totalAgents          : 시뮬레이션 생성 시 설정한 personaCount
            - avgCompletionSeconds : 초 단위, 프론트 변환 예시 → Math.floor(n/60) + '분' + (n%60) + '초'
            - dropOffAgents        : totalAgents - 최종 페이지 통과 수

            [funnelPanels - 전환 패널]
            - AI가 감지한 페이지 순서대로 동적 구성 (order 오름차순)
            - avgTimeSeconds : 해당 페이지 전체 평균 체류 시간 (초 단위)
            - agentsByAge    : 고정 8개 키 (10-19 ~ 80+), ratio=0인 연령대도 entered=0으로 포함
            - AgeGroupDto    : entered / passed / dropOff / successRate
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "개요 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulationOverviewResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "summary": {
                        "taskSuccessRate": 28.0,
                        "totalAgents": 1000,
                        "avgCompletionSeconds": 252,
                        "dropOffAgents": 720
                      },
                      "funnelPanels": [
                        {
                          "order": 1,
                          "pageName": "랜딩 페이지",
                          "pageUrl": "https://a-mall.com/",
                          "totalEntered": 1000,
                          "totalPassed": 850,
                          "panelSuccessRate": 85.0,
                          "avgTimeSeconds": 12,
                          "agentsByAge": {
                            "10-19": { "entered": 50,  "passed": 48, "dropOff": 2,  "successRate": 96.0 },
                            "20-29": { "entered": 300, "passed": 270,"dropOff": 30, "successRate": 90.0 },
                            "30-39": { "entered": 250, "passed": 215,"dropOff": 35, "successRate": 86.0 },
                            "40-49": { "entered": 200, "passed": 160,"dropOff": 40, "successRate": 80.0 },
                            "50-59": { "entered": 100, "passed": 75, "dropOff": 25, "successRate": 75.0 },
                            "60-69": { "entered": 70,  "passed": 50, "dropOff": 20, "successRate": 71.4 },
                            "70-79": { "entered": 25,  "passed": 10, "dropOff": 15, "successRate": 40.0 },
                            "80+":   { "entered": 5,   "passed": 1,  "dropOff": 4,  "successRate": 20.0 }
                          }
                        }
                      ]
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 시뮬레이션 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 404, "error": "Not Found", "message": "시뮬레이션을 찾을 수 없습니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<SimulationOverviewResponse> getOverview(
            @Parameter(description = "조회할 시뮬레이션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID simulationId
    ) {
        return ResponseEntity.ok(simulationService.getOverview(simulationId));
    }

    // ────────────────────────────────────────
    // GET /api/simulations/{simulationId}/issues
    // ────────────────────────────────────────
    @GetMapping("/{simulationId}/issues")
    @Operation(
            summary = "주요 이슈 조회 (Issues 탭)",
            description = """
            페이지별 UX/접근성 이슈 목록을 반환합니다.

            [주요이슈 탭 vs WCAG 탭]
            - 주요이슈: AI 에이전트 행동 로그 기반 UX 이슈
            - WCAG: 논문 기반 공식 가중치 오차 산출 접근성 이슈 (별도 분류)

            [좌측 페이지 패널]
            - funnelPanels와 동일한 페이지 순서 (order 오름차순)
            - screenshotUrl: AI 에이전트가 탐색 중 캡처한 스크린샷 URL

            [이슈 정렬]
            - 페이지 order 오름차순 → 페이지 내 severity 높은 순 (High → Medium → Low)

            [AI 수정 탭 연동]
            - issueId 기준으로 GET /api/simulations/{id}/ai-fix 와 동일한 키로 연결
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이슈 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulationIssuesResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "pages": [
                        {
                          "order": 1,
                          "pageName": "로그인 페이지",
                          "pageUrl": "https://a-mall.com/login",
                          "screenshotUrl": "https://storage.example.com/screenshots/sim42_page1.png",
                          "totalIssueCount": 3,
                          "issues": [
                            {
                              "issueId": 1,
                              "title": "입력 레이블이 낮은 대비율",
                              "category": "Accessibility",
                              "severity": "High",
                              "affectedUsersCount": 142,
                              "affectedUsersPercent": 14.2,
                              "description": "흰색 배경 위의 회색 텍스트로 인해 WCAG 2.1 AA 기준(4.5:1) 미달",
                              "selector": ".form-label",
                              "tags": ["contrast", "wcag_aa"]
                            },
                            {
                              "issueId": 2,
                              "title": "제출 버튼이 키보드로 접근 불가",
                              "category": "Accessibility",
                              "severity": "Medium",
                              "affectedUsersCount": 180,
                              "affectedUsersPercent": 18.0,
                              "description": "탭 키로 제출 버튼에 포커스가 되지 않아 키보드 전용 사용자 접근 불가",
                              "selector": ".submit-btn",
                              "tags": ["keyboard", "focus", "wcag_aa"]
                            },
                            {
                              "issueId": 3,
                              "title": "오류 메시지 노출 시간이 짧음",
                              "category": "Usability",
                              "severity": "Low",
                              "affectedUsersCount": 156,
                              "affectedUsersPercent": 15.6,
                              "description": "유효성 검사 실패 시 오류 메시지가 2초 이내 사라져 사용자가 인지하지 못함",
                              "selector": ".error-message",
                              "tags": ["timing", "feedback"]
                            }
                          ]
                        }
                      ]
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 시뮬레이션 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 404, "error": "Not Found", "message": "시뮬레이션을 찾을 수 없습니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<SimulationIssuesResponse> getIssues(
            @Parameter(description = "조회할 시뮬레이션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID simulationId
    ) {
        return ResponseEntity.ok(simulationService.getIssues(simulationId));
    }

    // ────────────────────────────────────────
    // GET /api/simulations/{simulationId}/ai-fix
    // ────────────────────────────────────────
    @GetMapping("/{simulationId}/ai-fix")
    @Operation(
            summary = "AI 수정 제안 조회 (AI 수정 탭)",
            description = """
            Issues 탭의 issueId 기준으로 Before/After 코드 수정 제안을 반환합니다.

            [이슈 카드 선택]
            - 상단 이슈 카드 클릭 시 해당 issueId의 beforeCode / afterCode 표시
            - issueId는 GET /api/simulations/{id}/issues 와 동일한 키

            [코드 형식]
            - beforeCode / afterCode: CSS 또는 HTML 코드 문자열 (\\n으로 줄바꿈)

            [AI 생성 텍스트]
            - impactDescription : 수정 적용 시 영향 설명
            - changeDescription : '무엇이 변경되었나요?' 섹션 텍스트
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 수정 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulationAiFixResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "pages": [
                        {
                          "order": 1,
                          "pageName": "로그인 페이지",
                          "pageUrl": "https://a-mall.com/login",
                          "screenshotUrl": "https://storage.example.com/screenshots/sim42_page1.png",
                          "totalFixCount": 3,
                          "fixes": [
                            {
                              "issueId": 1,
                              "title": "입력 레이블이 낮은 대비율",
                              "severity": "High",
                              "affectedUsersCount": 142,
                              "beforeCode": ".form-label {\\n  color: #999999;\\n  font-size: 14px;\\n}",
                              "afterCode": ".form-label {\\n  color: #334155;\\n  font-size: 14px;\\n  font-weight: 500;\\n}",
                              "impactDescription": "142명의 사용자가 이제 레이블을 명확하게 읽을 수 있음",
                              "changeDescription": "레이블 색상을 #999999에서 #334155로 변경하여 WCAG 표준을 충족합니다."
                            }
                          ]
                        }
                      ]
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 시뮬레이션 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 404, "error": "Not Found", "message": "시뮬레이션을 찾을 수 없습니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<SimulationAiFixResponse> getAiFix(
            @Parameter(description = "조회할 시뮬레이션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID simulationId
    ) {
        return ResponseEntity.ok(simulationService.getAiFix(simulationId));
    }

    // ────────────────────────────────────────
    // GET /api/simulations/{simulationId}/heatmap
    // ────────────────────────────────────────
    @GetMapping("/{simulationId}/heatmap")
    @Operation(
            summary = "히트맵 조회 (히트맵 탭)",
            description = """
            페이지별 오류 집중 구간 히트맵 데이터를 반환합니다.

            [히트맵 배경]
            - screenshotUrl: Issues 탭과 동일한 스크린샷 이미지 URL

            [오류 좌표]
            - x, y: 정규화 좌표 (0~1), 스크린샷 크기 기준 비율
            - Timeout·Network·Console 오류 기반 좌표 집계
            - count 기준 severity 자동 분류:
              1~3 = LOW, 4~7 = MEDIUM, 8~14 = HIGH, 15+ = CRITICAL

            [연령대 필터]
            - errorPointsByAge 키: all / 10대 / 20대 / 30대 / 40대 / 50대 / 60대 / 70대 / 80대
            - 프론트 연령대 탭 클릭 시 해당 키 데이터 렌더링
            - 오버레이 슬라이더: 프론트에서 연령대 누적 처리 (API 무관)

            [팝업 데이터]
            - 좌표 클릭 시 팝업: severity / errorType / affectedCount / blockRate / repeatCount
            - errorBreakdown: timeout / network / console 세부 카운트
            - issueId: Issues 탭 issueId와 동일한 키 (null 가능)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "히트맵 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulationHeatmapResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "pages": [
                        {
                          "order": 1,
                          "pageName": "로그인 페이지",
                          "pageUrl": "https://a-mall.com/login",
                          "screenshotUrl": "https://storage.example.com/screenshots/sim42_page1.png",
                          "totalErrorCount": 12,
                          "errorPointsByAge": {
                            "all": [
                              {
                                "x": 0.72, "y": 0.35, "count": 18,
                                "severity": "CRITICAL", "errorType": "Timeout",
                                "affectedCount": 12, "blockRate": 100.0, "repeatCount": 4.5,
                                "description": "Timeout 오류가 집중된 구간입니다.",
                                "errorBreakdown": { "timeout": 2, "network": 0, "console": 0 },
                                "issueId": 1
                              }
                            ],
                            "10대": [
                              {
                                "x": 0.72, "y": 0.35, "count": 2,
                                "severity": "LOW", "errorType": "Timeout",
                                "affectedCount": 1, "blockRate": 20.0, "repeatCount": 1.0,
                                "description": "10대 에이전트 낮은 빈도 오류입니다.",
                                "errorBreakdown": { "timeout": 1, "network": 0, "console": 0 },
                                "issueId": 1
                              }
                            ]
                          }
                        }
                      ]
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 시뮬레이션 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 404, "error": "Not Found", "message": "시뮬레이션을 찾을 수 없습니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<SimulationHeatmapResponse> getHeatmap(
            @Parameter(description = "조회할 시뮬레이션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID simulationId
    ) {
        return ResponseEntity.ok(simulationService.getHeatmap(simulationId));
    }

    // ────────────────────────────────────────
    // GET /api/simulations/{simulationId}/wcag
    // ────────────────────────────────────────
    @GetMapping("/{simulationId}/wcag")
    @Operation(
            summary = "WCAG 검사 결과 조회 (WCAG 탭)",
            description = """
            페이지별 WCAG 2.1 Level AA 접근성 검사 결과를 반환합니다.

            [주요이슈 탭과의 차이]
            - 주요이슈: AI 에이전트 행동 로그 기반 UX 이슈
            - WCAG: 논문 기반 공식 가중치 오차 산출 접근성 이슈 (별도 분류)

            [상단 3개 메트릭 카드]
            - complianceScore : passedTests / totalTests * 100 (%)
            - wcagLabel       : A / AA / AAA
            - foundIssues     : Critical + Moderate + Minor 합계

            [검출 이슈 분석]
            - distribution: Critical(즉각 조치) / Moderate(우선순위 높음) / Minor(권장 수정)

            [상세 검사 결과]
            - issues: Critical → Moderate → Minor 순 정렬
            - description: 자세히 보기 클릭 시 노출되는 설명 텍스트 (AI 생성)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "WCAG 검사 결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SimulationWcagResponse.class),
                            examples = @ExampleObject(value = """
                    {
                      "pages": [
                        {
                          "order": 1,
                          "pageName": "로그인 페이지",
                          "pageUrl": "https://a-mall.com/login",
                          "screenshotUrl": "https://storage.example.com/screenshots/sim42_page1.png",
                          "summary": {
                            "complianceScore": 38.0,
                            "wcagLabel": "AA",
                            "totalTests": 10,
                            "passedTests": 3,
                            "foundIssues": 10
                          },
                          "distribution": {
                            "critical": 3,
                            "moderate": 4,
                            "minor": 3
                          },
                          "issues": [
                            {
                              "wcagIssueId": 1,
                              "title": "텍스트 대비율",
                              "severity": "Critical",
                              "description": "본문/보조 텍스트의 대비가 WCAG 2.1 AA 기준을 충족하지 않아 저시력 사용자의 가독성이 저하됩니다."
                            },
                            {
                              "wcagIssueId": 2,
                              "title": "최소 글자 크기",
                              "severity": "Moderate",
                              "description": "일부 텍스트가 12px 이하로 표시되어 읽기 어려울 수 있습니다."
                            },
                            {
                              "wcagIssueId": 3,
                              "title": "위치 수정",
                              "severity": "Minor",
                              "description": "일부 UI 요소의 위치가 사용자 예상 동선과 다르게 배치되어 탐색 혼란을 유발할 수 있습니다."
                            }
                          ]
                        }
                      ]
                    }
                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 시뮬레이션 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 404, "error": "Not Found", "message": "시뮬레이션을 찾을 수 없습니다."}
                    """))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {"status": 500, "error": "Internal Server Error", "message": "서버 오류가 발생했습니다."}
                    """)))
    })
    public ResponseEntity<SimulationWcagResponse> getWcag(
            @Parameter(description = "조회할 시뮬레이션 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID simulationId
    ) {
        return ResponseEntity.ok(simulationService.getWcag(simulationId));
    }
}