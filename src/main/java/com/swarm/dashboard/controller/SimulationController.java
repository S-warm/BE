package com.swarm.dashboard.controller;

import com.swarm.dashboard.dto.request.SimulationCreateRequest;
import com.swarm.dashboard.dto.response.SimulationAiFixResponse;
import com.swarm.dashboard.dto.response.SimulationCreateResponse;
import com.swarm.dashboard.dto.response.SimulationHeatmapResponse;
import com.swarm.dashboard.dto.response.SimulationIssuesResponse;
import com.swarm.dashboard.dto.response.SimulationListResponse;
import com.swarm.dashboard.dto.response.SimulationOverviewResponse;
import com.swarm.dashboard.dto.response.SimulationStatusResponse;
import com.swarm.dashboard.dto.response.SimulationWcagResponse;
import com.swarm.dashboard.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/simulations")
@RequiredArgsConstructor
@Tag(name = "Simulation", description = "시뮬레이션 생성 및 관리 API")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping
    @Operation(summary = "시뮬레이션 생성")
    public ResponseEntity<SimulationCreateResponse> createSimulation(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam UUID userId,
            @Valid @RequestBody SimulationCreateRequest request
    ) {
        log.info("[시뮬레이션 생성 요청] userId={}, title={}, targetUrl={}, task={}, digitalLiteracy={}, personaDevice={}, successCondition={path={}, requiredParams={}}, ageCounts=[10={}, 20={}, 30={}, 40={}, 50={}, 60={}, 70={}]",
                userId, request.getTitle(), request.getTargetUrl(), request.getTask(),
                request.getDigitalLiteracy(), request.getPersonaDevice(),
                request.getSuccessCondition() != null ? request.getSuccessCondition().getPath() : null,
                request.getSuccessCondition() != null ? request.getSuccessCondition().getRequiredParams() : null,
                request.getAgeCount10(), request.getAgeCount20(), request.getAgeCount30(),
                request.getAgeCount40(), request.getAgeCount50(), request.getAgeCount60(), request.getAgeCount70());
        SimulationCreateResponse response = simulationService.createSimulation(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}/status")
    @Operation(summary = "시뮬레이션 진행 상태 조회")
    public ResponseEntity<SimulationStatusResponse> getStatus(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(simulationService.getStatus(projectId));
    }

    @GetMapping
    @Operation(summary = "시뮬레이션 목록 조회 (사이드바용)")
    public ResponseEntity<List<SimulationListResponse>> getSimulations(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam UUID userId
    ) {
        List<SimulationListResponse> response = simulationService.getSimulationsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/overview")
    @Operation(summary = "시뮬레이션 개요 조회 (Overview 탭)")
    public ResponseEntity<SimulationOverviewResponse> getOverview(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(simulationService.getOverview(projectId));
    }

    @GetMapping("/{projectId}/issues")
    @Operation(summary = "주요 이슈 조회 (Issues 탭)")
    public ResponseEntity<SimulationIssuesResponse> getIssues(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(simulationService.getIssues(projectId));
    }

    @GetMapping("/{projectId}/ai-fix")
    @Operation(summary = "AI 수정 제안 조회 (AI 수정 탭)")
    public ResponseEntity<SimulationAiFixResponse> getAiFix(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(simulationService.getAiFix(projectId));
    }

    @GetMapping("/{projectId}/heatmap")
    @Operation(summary = "히트맵 조회 (히트맵 탭)")
    public ResponseEntity<SimulationHeatmapResponse> getHeatmap(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable UUID projectId,
            @Parameter(description = "연령대 필터 (all, 10대, 20대, ..., 70대)", example = "all")
            @RequestParam(defaultValue = "all") String ageGroup,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지당 오류점 수", example = "100")
            @RequestParam(defaultValue = "100") int size
    ) {
        validateAgeGroup(ageGroup);
        return ResponseEntity.ok(simulationService.getHeatmap(projectId, ageGroup, page, size));
    }

    @GetMapping("/{projectId}/wcag")
    @Operation(summary = "WCAG 검사 결과 조회 (WCAG 탭)")
    public ResponseEntity<SimulationWcagResponse> getWcag(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(simulationService.getWcag(projectId));
    }

    private void validateAgeGroup(String ageGroup) {
        Set<String> valid = Set.of("all", "10대", "20대", "30대", "40대", "50대", "60대", "70대");
        if (!valid.contains(ageGroup)) {
            throw new IllegalArgumentException("Invalid ageGroup: " + ageGroup);
        }
    }
}
