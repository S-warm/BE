package com.swarm.dashboard.service;

import com.swarm.dashboard.exception.SimulationNotFoundException;
import com.swarm.dashboard.dto.request.SimulationCreateRequest;
import com.swarm.dashboard.dto.response.SimulationAiFixResponse;
import com.swarm.dashboard.dto.response.SimulationCreateResponse;
import com.swarm.dashboard.dto.response.SimulationHeatmapResponse;
import com.swarm.dashboard.dto.response.SimulationIssuesResponse;
import com.swarm.dashboard.dto.response.SimulationListResponse;
import com.swarm.dashboard.dto.response.SimulationOverviewResponse;
import com.swarm.dashboard.dto.response.SimulationWcagResponse;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.domain.simulation.SimulationSettings;
import com.swarm.dashboard.domain.simulation.SimulationSettingsRepository;
import com.swarm.dashboard.domain.user.User;
import com.swarm.dashboard.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationSettingsRepository simulationSettingsRepository;
    private final UserRepository userRepository;

    // ────────────────────────────────────────
    // POST - 시뮬레이션 생성
    // ────────────────────────────────────────
    @Transactional
    public SimulationCreateResponse createSimulation(UUID userId, SimulationCreateRequest request) {
        User user = resolveUser(userId);

        int ratioSum = request.getAgeRatioTeen() + request.getAgeRatioFifty() + request.getAgeRatioEighty();
        if (ratioSum != 100) {
            throw new IllegalArgumentException(
                    "연령대 비율 합계는 100이어야 합니다. 현재 합계: " + ratioSum);
        }

        Simulation simulation = Simulation.builder()
                .user(user)
                .title(request.getTitle())
                .targetUrl(request.getTargetUrl())
                .personaCount(request.getPersonaCount())
                .status("pending")
                .createdAt(OffsetDateTime.now())
                .build();

        Simulation saved = simulationRepository.save(simulation);

        SimulationSettings settings = SimulationSettings.builder()
                .simulation(saved)
                .digitalLiteracy(request.getDigitalLiteracy())
                .successCondition(request.getSuccessCondition())
                .personaDevice(request.getPersonaDevice())
                .ageRatioTeen(request.getAgeRatioTeen())
                .ageRatioFifty(request.getAgeRatioFifty())
                .ageRatioEighty(request.getAgeRatioEighty())
                .visionImpairment(request.getVisionImpairment())
                .attentionLevel(request.getAttentionLevel())
                .build();

        SimulationSettings savedSettings = simulationSettingsRepository.save(settings);
        log.info("Created simulation {} for user {}", saved.getId(), userId);
        log.debug("Stored simulation settings for simulation {}", savedSettings.getSimulationId());

        return SimulationCreateResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // ────────────────────────────────────────
    // GET - 사용자별 시뮬레이션 목록 조회 (사이드바용)
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SimulationListResponse> getSimulationsByUserId(UUID userId) {
        return simulationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(s -> SimulationListResponse.builder()
                        .id(s.getId())
                        .title(s.getTitle())
                        .status(s.getStatus())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────
    // GET - Overview 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationOverviewResponse getOverview(UUID simulationId) {
        Simulation simulation = getSimulationOrThrow(simulationId);
        return buildMockOverview(simulation);
    }

    // ────────────────────────────────────────
    // GET - Issues 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationIssuesResponse getIssues(UUID simulationId) {
        getSimulationOrThrow(simulationId);
        return buildMockIssues();
    }

    // ────────────────────────────────────────
    // GET - AI 수정 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationAiFixResponse getAiFix(UUID simulationId) {
        getSimulationOrThrow(simulationId);
        return buildMockAiFix();
    }

    // ────────────────────────────────────────
    // GET - 히트맵 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationHeatmapResponse getHeatmap(UUID simulationId, String ageGroup, int page, int size) {
        getSimulationOrThrow(simulationId);
        return buildMockHeatmap(ageGroup, page, size);
    }

    // ────────────────────────────────────────
    // GET - WCAG 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationWcagResponse getWcag(UUID simulationId) {
        getSimulationOrThrow(simulationId);
        return buildMockWcag();
    }

    private User resolveUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseGet(() -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    User newUser = new User();
                    newUser.setId(userId);
                    newUser.setUsername("user_" + userId.toString().substring(0, 8));
                    newUser.setProvider("system");
                    newUser.setCreatedAt(now);
                    newUser.setUpdatedAt(now);
                    log.warn("User {} not found. Creating development fallback user.", userId);
                    return userRepository.save(newUser);
                });
    }

    private Simulation getSimulationOrThrow(UUID simulationId) {
        return simulationRepository.findById(simulationId)
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
    }

    // ================================================================
    // ✅ TODO: 실제 로그 데이터 연동 후 아래 Mock 메서드 전체 삭제 후 교체
    // ================================================================

    // ────────────────────────────────────────
    // Mock - Overview
    // ────────────────────────────────────────
    private SimulationOverviewResponse buildMockOverview(Simulation simulation) {
        List<SimulationOverviewResponse.FunnelPanelDto> funnelPanels = List.of(
                SimulationOverviewResponse.FunnelPanelDto.builder()
                        .order(1).pageName("랜딩 페이지")
                        .pageUrl(simulation.getTargetUrl())
                        .totalEntered(1000).totalPassed(850).panelSuccessRate(85.0).avgTimeSeconds(12)
                        .agentsByAge(buildMockAgeGroup(
                                new int[]{50, 300, 250, 200, 100, 70, 25, 5},
                                new int[]{48, 270, 215, 160,  75, 50, 10, 1}))
                        .build(),
                SimulationOverviewResponse.FunnelPanelDto.builder()
                        .order(2).pageName("로그인 폼")
                        .pageUrl(simulation.getTargetUrl() + "/login")
                        .totalEntered(850).totalPassed(600).panelSuccessRate(70.6).avgTimeSeconds(25)
                        .agentsByAge(buildMockAgeGroup(
                                new int[]{48, 270, 215, 160,  75, 50, 10, 1},
                                new int[]{45, 240, 170, 100,  40, 20,  3, 0}))
                        .build(),
                SimulationOverviewResponse.FunnelPanelDto.builder()
                        .order(3).pageName("필드 입력")
                        .pageUrl(simulation.getTargetUrl() + "/login/input")
                        .totalEntered(600).totalPassed(400).panelSuccessRate(66.7).avgTimeSeconds(40)
                        .agentsByAge(buildMockAgeGroup(
                                new int[]{45, 240, 170, 100,  40, 20,  3, 0},
                                new int[]{43, 210, 130,  60,  22,  8,  1, 0}))
                        .build(),
                SimulationOverviewResponse.FunnelPanelDto.builder()
                        .order(4).pageName("유효성 검사")
                        .pageUrl(simulation.getTargetUrl() + "/login/validate")
                        .totalEntered(400).totalPassed(280).panelSuccessRate(70.0).avgTimeSeconds(18)
                        .agentsByAge(buildMockAgeGroup(
                                new int[]{43, 210, 130,  60,  22,  8,  1, 0},
                                new int[]{40, 180, 100,  40,  12,  5,  0, 0}))
                        .build()
        );
        return SimulationOverviewResponse.builder()
                .summary(SimulationOverviewResponse.SummaryDto.builder()
                        .taskSuccessRate(28.0).totalAgents(1000)
                        .avgCompletionSeconds(252).dropOffAgents(720).build())
                .funnelPanels(funnelPanels)
                .build();
    }

    // ✅ [ageBand 통일] Heatmap ageGroup 파라미터와 동일한 한국어 형식 사용
    private static final String[] AGE_KEYS = {
            "10대", "20대", "30대", "40대", "50대", "60대", "70대", "80대"
    };

    private Map<String, SimulationOverviewResponse.AgeGroupDto> buildMockAgeGroup(
            int[] entered, int[] passed) {
        Map<String, SimulationOverviewResponse.AgeGroupDto> map = new LinkedHashMap<>();
        for (int i = 0; i < AGE_KEYS.length; i++) {
            int dropOff = entered[i] - passed[i];
            double rate = entered[i] == 0 ? 0.0
                    : Math.round((passed[i] * 1000.0 / entered[i])) / 10.0;
            map.put(AGE_KEYS[i], SimulationOverviewResponse.AgeGroupDto.builder()
                    .entered(entered[i]).passed(passed[i]).dropOff(dropOff).successRate(rate)
                    .build());
        }
        return map;
    }

    // ────────────────────────────────────────
    // Mock - Issues
    // ────────────────────────────────────────
    private SimulationIssuesResponse buildMockIssues() {
        // ✅ [C-1] Mock issueId: Long → UUID (실제 DB 연동 시 Issue.id 값으로 교체)
        UUID mockIssueId1 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        UUID mockIssueId2 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
        UUID mockIssueId3 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003");
        UUID mockIssueId4 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000004");
        UUID mockIssueId5 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000005");

        List<SimulationIssuesResponse.IssueDto> page1Issues = List.of(
                SimulationIssuesResponse.IssueDto.builder()
                        .issueId(mockIssueId1).title("입력 레이블이 낮은 대비율").category("Accessibility").severity("HIGH")
                        .affectedUsersCount(142).affectedUsersPercent(14.2)
                        .description("흰색 배경 위의 회색 텍스트로 인해 WCAG 2.1 AA 기준(4.5:1) 미달")
                        .targetHtml(".form-label")
                        .tags(List.of("contrast", "wcag_aa")).build(),
                SimulationIssuesResponse.IssueDto.builder()
                        .issueId(mockIssueId2).title("제출 버튼이 키보드로 접근 불가").category("Accessibility").severity("MEDIUM")
                        .affectedUsersCount(180).affectedUsersPercent(18.0)
                        .description("탭 키로 제출 버튼에 포커스가 되지 않아 키보드 전용 사용자 접근 불가")
                        .targetHtml(".submit-btn")
                        .tags(List.of("keyboard", "focus", "wcag_aa")).build(),
                SimulationIssuesResponse.IssueDto.builder()
                        .issueId(mockIssueId3).title("오류 메시지 노출 시간이 짧음").category("Usability").severity("LOW")
                        .affectedUsersCount(156).affectedUsersPercent(15.6)
                        .description("유효성 검사 실패 시 오류 메시지가 2초 이내 사라져 사용자가 인지하지 못함")
                        .targetHtml(".error-message")
                        .tags(List.of("timing", "feedback")).build()
        );
        List<SimulationIssuesResponse.IssueDto> page2Issues = List.of(
                SimulationIssuesResponse.IssueDto.builder()
                        .issueId(mockIssueId4).title("배너 이미지 alt 텍스트 누락").category("Accessibility").severity("HIGH")
                        .affectedUsersCount(210).affectedUsersPercent(21.0)
                        .description("메인 배너 이미지에 alt 속성이 없어 스크린리더 사용자 접근 불가")
                        .targetHtml(".main-banner img")
                        .tags(List.of("alt", "wcag_aa", "screen-reader")).build(),
                SimulationIssuesResponse.IssueDto.builder()
                        .issueId(mockIssueId5).title("모바일 터치 영역 너무 작음").category("Usability").severity("MEDIUM")
                        .affectedUsersCount(95).affectedUsersPercent(9.5)
                        .description("하단 네비게이션 버튼의 터치 영역이 24px로 권장 최소값(44px) 미달")
                        .targetHtml(".nav-btn")
                        .tags(List.of("touch-target", "mobile")).build()
        );
        return SimulationIssuesResponse.builder()
                .pages(List.of(
                        SimulationIssuesResponse.IssuePageDto.builder()
                                .order(1).pageName("로그인 페이지").pageUrl("https://a-mall.com/login")
                                .screenshotUrl("https://storage.example.com/screenshots/sim42_page1.png")
                                .totalIssueCount(page1Issues.size()).issues(page1Issues).build(),
                        SimulationIssuesResponse.IssuePageDto.builder()
                                .order(2).pageName("메인 페이지").pageUrl("https://a-mall.com/")
                                .screenshotUrl("https://storage.example.com/screenshots/sim42_page2.png")
                                .totalIssueCount(page2Issues.size()).issues(page2Issues).build()
                )).build();
    }

    // ────────────────────────────────────────
    // Mock - AI Fix
    // ────────────────────────────────────────
    private SimulationAiFixResponse buildMockAiFix() {
        // ✅ [C-1] Issues와 동일한 Mock UUID 재사용 (issueId 연동 기준 키)
        UUID mockIssueId1 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        UUID mockIssueId2 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
        UUID mockIssueId3 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003");

        List<SimulationAiFixResponse.AiFixDto> page1Fixes = List.of(
                SimulationAiFixResponse.AiFixDto.builder()
                        .issueId(mockIssueId1).title("입력 레이블이 낮은 대비율").severity("HIGH").affectedUsersCount(142)
                        .beforeCode(".form-label {\n  color: #999999;\n  font-size: 14px;\n  margin-bottom: 8px;\n}")
                        .afterCode(".form-label {\n  color: #334155;\n  font-size: 14px;\n  margin-bottom: 8px;\n  font-weight: 500;\n}")
                        .impactDescription("142명의 사용자가 이제 레이블을 명확하게 읽을 수 있음")
                        .changeDescription("레이블 색상을 #999999에서 #334155로 변경하여 대비율을 달성하고 WCAG의 표준을 충족합니다.").build(),
                SimulationAiFixResponse.AiFixDto.builder()
                        .issueId(mockIssueId2).title("제출 버튼이 키보드로 접근 불가").severity("MEDIUM").affectedUsersCount(180)
                        .beforeCode(".submit-btn {\n  outline: none;\n}")
                        .afterCode(".submit-btn {\n  outline: 2px solid #334155;\n  outline-offset: 2px;\n}")
                        .impactDescription("180명의 키보드 사용자가 버튼에 접근 가능해짐")
                        .changeDescription("outline: none 제거 후 명시적 포커스 스타일을 추가하여 키보드 접근성을 확보합니다.").build(),
                SimulationAiFixResponse.AiFixDto.builder()
                        .issueId(mockIssueId3).title("오류 메시지 노출 시간이 짧음").severity("LOW").affectedUsersCount(156)
                        .beforeCode(".error-message {\n  display: none;\n  animation: fadeOut 2s ease;\n}")
                        .afterCode(".error-message {\n  display: block;\n  animation: fadeOut 5s ease;\n}")
                        .impactDescription("156명의 사용자가 오류 메시지를 충분한 시간 동안 인지할 수 있음")
                        .changeDescription("오류 메시지 노출 시간을 2초에서 5초로 늘려 사용자가 내용을 확인할 수 있도록 개선합니다.").build()
        );
        return SimulationAiFixResponse.builder()
                .pages(List.of(
                        SimulationAiFixResponse.AiFixPageDto.builder()
                                .order(1).pageName("로그인 페이지").pageUrl("https://a-mall.com/login")
                                .screenshotUrl("https://storage.example.com/screenshots/sim42_page1.png")
                                .totalFixCount(page1Fixes.size()).fixes(page1Fixes).build()
                )).build();
    }

    // ────────────────────────────────────────
    // Mock - Heatmap
    // ────────────────────────────────────────
    private SimulationHeatmapResponse buildMockHeatmap(String ageGroup, int page, int size) {

        // 1️⃣ 페이지별 전체 오류점 맵 생성
        Map<String, Map<String, List<SimulationHeatmapResponse.ErrorPointDto>>> allPagesMaps =
                createAllPagesErrorPointsMaps();

        // 2️⃣ 페이지별 응답 조립
        List<SimulationHeatmapResponse.HeatmapPageDto> pageDtos = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<SimulationHeatmapResponse.ErrorPointDto>>> pageEntry
                : allPagesMaps.entrySet()) {

            String pageKey = pageEntry.getKey();
            Map<String, List<SimulationHeatmapResponse.ErrorPointDto>> errorPointsByAge = pageEntry.getValue();

            // 3️⃣ 연령대 필터링
            List<SimulationHeatmapResponse.ErrorPointDto> filtered;
            if ("all".equals(ageGroup)) {
                filtered = errorPointsByAge.values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            } else {
                filtered = errorPointsByAge.getOrDefault(ageGroup, new ArrayList<>());
            }

            // 4️⃣ ageBand 필드 채우기
            List<SimulationHeatmapResponse.ErrorPointDto> pointsWithAgeBand = filtered.stream()
                    .map(p -> SimulationHeatmapResponse.ErrorPointDto.builder()
                            .x(p.getX()).y(p.getY()).count(p.getCount())
                            .severity(p.getSeverity()).errorType(p.getErrorType())
                            .affectedUsersCount(p.getAffectedUsersCount()).blockRate(p.getBlockRate())
                            .repeatCount(p.getRepeatCount()).description(p.getDescription())
                            .errorBreakdown(p.getErrorBreakdown()).issueId(p.getIssueId())
                            .ageBand(ageGroup)
                            .build())
                    .collect(Collectors.toList());

            // 5️⃣ 페이징 계산
            int totalCount = pointsWithAgeBand.size();
            int startIdx = page * size;
            int endIdx = Math.min(startIdx + size, totalCount);
            List<SimulationHeatmapResponse.ErrorPointDto> paginatedPoints =
                    (startIdx >= totalCount)
                            ? new ArrayList<>()
                            : pointsWithAgeBand.subList(startIdx, endIdx);

            // 6️⃣ 페이징 메타데이터
            SimulationHeatmapResponse.PaginationDto pagination = SimulationHeatmapResponse.PaginationDto.builder()
                    .totalCount(totalCount)
                    .currentPage(page)
                    .pageSize(size)
                    .hasMore(endIdx < totalCount)
                    .build();

            // 7️⃣ 페이지별 DTO 조립
            String[] parts = pageKey.split("\\|");
            pageDtos.add(SimulationHeatmapResponse.HeatmapPageDto.builder()
                    .order(Integer.parseInt(parts[0]))
                    .pageName(parts[1])
                    .pageUrl(parts[2])
                    .screenshotUrl(parts[3])
                    .totalErrorCount(totalCount)
                    .errorPoints(paginatedPoints)
                    .currentAgeGroup(ageGroup)
                    .pagination(pagination)
                    .build());
        }

        return SimulationHeatmapResponse.builder().pages(pageDtos).build();
    }

    /**
     * 모든 페이지별, 연령대별 오류점 Mock 데이터 생성
     * key: "order|pageName|pageUrl|screenshotUrl"
     */
    private Map<String, Map<String, List<SimulationHeatmapResponse.ErrorPointDto>>> createAllPagesErrorPointsMaps() {

        // ✅ [C-1] Heatmap issueId: Long → UUID (Issues 탭의 mockIssueId와 동일한 값으로 연동)
        UUID hmIssueId1 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        UUID hmIssueId2 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
        UUID hmIssueId3 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000003");
        UUID hmIssueId4 = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000004");

        Map<String, Map<String, List<SimulationHeatmapResponse.ErrorPointDto>>> pages = new LinkedHashMap<>();

        // ── 페이지 1: 로그인 페이지 ──
        Map<String, List<SimulationHeatmapResponse.ErrorPointDto>> page1 = new LinkedHashMap<>();
        page1.put("all", List.of(
                SimulationHeatmapResponse.ErrorPointDto.builder()
                        .x(0.72).y(0.35).count(18).severity("CRITICAL").errorType("Timeout")
                        .affectedUsersCount(12).blockRate(100.0).repeatCount(4.5)
                        .description("클릭/스텝 로그에서 Timeout 오류가 집중된 구간입니다.")
                        .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                                .timeout(2).network(0).console(0).build())
                        .issueId(hmIssueId1).build(),
                SimulationHeatmapResponse.ErrorPointDto.builder()
                        .x(0.38).y(0.52).count(9).severity("HIGH").errorType("Network")
                        .affectedUsersCount(5).blockRate(60.0).repeatCount(2.1)
                        .description("네트워크 오류로 인한 페이지 로딩 지연 구간입니다.")
                        .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                                .timeout(0).network(4).console(1).build())
                        .issueId(hmIssueId2).build(),
                SimulationHeatmapResponse.ErrorPointDto.builder()
                        .x(0.55).y(0.68).count(5).severity("MEDIUM").errorType("Console")
                        .affectedUsersCount(3).blockRate(40.0).repeatCount(1.8)
                        .description("Console 오류로 인한 UI 렌더링 지연 구간입니다.")
                        .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                                .timeout(0).network(0).console(3).build())
                        .issueId(hmIssueId3).build()
        ));
        page1.put("10대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(2).severity("LOW").errorType("Timeout")
                .affectedUsersCount(1).blockRate(20.0).repeatCount(1.0)
                .description("10대 에이전트 낮은 빈도 오류입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(1).network(0).console(0).build())
                .issueId(hmIssueId1).build()));
        page1.put("20대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(3).severity("LOW").errorType("Timeout")
                .affectedUsersCount(2).blockRate(25.0).repeatCount(1.3)
                .description("20대 에이전트 낮은 빈도 오류입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(2).network(0).console(0).build())
                .issueId(hmIssueId1).build()));
        page1.put("30대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.38).y(0.52).count(4).severity("MEDIUM").errorType("Network")
                .affectedUsersCount(2).blockRate(30.0).repeatCount(1.5)
                .description("30대 에이전트 네트워크 오류 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(0).network(3).console(0).build())
                .issueId(hmIssueId2).build()));
        page1.put("40대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(6).severity("MEDIUM").errorType("Timeout")
                .affectedUsersCount(3).blockRate(45.0).repeatCount(2.2)
                .description("40대 에이전트 Timeout 오류 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(3).network(1).console(0).build())
                .issueId(hmIssueId1).build()));
        page1.put("50대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(8).severity("HIGH").errorType("Timeout")
                .affectedUsersCount(5).blockRate(65.0).repeatCount(3.1)
                .description("50대 에이전트 높은 빈도의 Timeout 오류 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(5).network(1).console(0).build())
                .issueId(hmIssueId1).build()));
        page1.put("60대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(11).severity("HIGH").errorType("Timeout")
                .affectedUsersCount(6).blockRate(80.0).repeatCount(4.0)
                .description("60대 에이전트 심각한 Timeout 오류 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(6).network(2).console(0).build())
                .issueId(hmIssueId1).build()));
        page1.put("70대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(15).severity("CRITICAL").errorType("Timeout")
                .affectedUsersCount(8).blockRate(100.0).repeatCount(6.8)
                .description("70대 에이전트 심각한 Timeout 오류 집중 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(8).network(2).console(1).build())
                .issueId(hmIssueId1).build()));
        page1.put("80대", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.72).y(0.35).count(18).severity("CRITICAL").errorType("Timeout")
                .affectedUsersCount(9).blockRate(100.0).repeatCount(8.3)
                .description("80대 에이전트 가장 심각한 Timeout 오류 집중 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(9).network(3).console(2).build())
                .issueId(hmIssueId1).build()));
        pages.put("1|로그인 페이지|https://a-mall.com/login|https://storage.example.com/screenshots/sim42_page1.png", page1);

        // ── 페이지 2: 메인 페이지 ──
        Map<String, List<SimulationHeatmapResponse.ErrorPointDto>> page2 = new LinkedHashMap<>();
        page2.put("all", List.of(SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(0.45).y(0.30).count(5).severity("MEDIUM").errorType("Console")
                .affectedUsersCount(3).blockRate(35.0).repeatCount(1.5)
                .description("메인 페이지 Console 오류 집중 구간입니다.")
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(0).network(0).console(3).build())
                .issueId(hmIssueId4).build()));
        pages.put("2|메인 페이지|https://a-mall.com/|https://storage.example.com/screenshots/sim42_page2.png", page2);

        return pages;
    }

    // ────────────────────────────────────────
    // Mock - WCAG
    // ────────────────────────────────────────
    // ✅ FIX 7: SimulationWcagResponse가 pages 래퍼 없이 flat 구조로 변경됨
    //           (summary / distribution / issues 직접 노출)
    //           두 페이지 이슈를 통합하여 단일 응답으로 반환
    private SimulationWcagResponse buildMockWcag() {

        // 두 페이지 이슈를 하나의 리스트로 통합 (Critical → Moderate → Minor 정렬)
        List<SimulationWcagResponse.WcagIssueDto> allIssues = List.of(
                // ── Critical (4건) ──
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(1L).title("텍스트 대비율").severity("Critical")
                        .description("본문/보조 텍스트의 대비가 WCAG 2.1 AA 기준을 충족하지 않아, 저시력 사용자의 가독성이 크게 저하됩니다. 대비(명도 차이)를 높이거나 배경색을 조정해 최소 대비율을 만족하도록 개선하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(2L).title("키보드 포커스 표시 없음").severity("Critical")
                        .description("키보드로 탐색 시 포커스 인디케이터가 표시되지 않아 키보드 전용 사용자가 현재 위치를 파악할 수 없습니다. outline 스타일을 명시적으로 지정하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(3L).title("폼 레이블 연결 누락").severity("Critical")
                        .description("입력 필드에 연결된 레이블이 없어 스크린리더 사용자가 필드의 목적을 알 수 없습니다. label 태그 또는 aria-label 속성을 추가하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(11L).title("이미지 대체 텍스트 누락").severity("Critical")
                        .description("배너 이미지에 alt 속성이 없어 스크린리더 사용자가 콘텐츠를 인식할 수 없습니다. 모든 의미 있는 이미지에 적절한 대체 텍스트를 제공하세요.").build(),
                // ── Moderate (6건) ──
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(4L).title("최소 글자 크기").severity("Moderate")
                        .description("일부 설명 텍스트가 12px 이하로 표시되어 읽기 어려울 수 있습니다. 기본 폰트 크기 및 line-height를 상향해 가독성을 개선하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(5L).title("버튼 클릭 영역 부족").severity("Moderate")
                        .description("일부 버튼의 클릭/터치 영역이 24px로 권장 최소값(44px)에 미달합니다. padding을 추가하여 터치 영역을 확보하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(6L).title("오류 메시지 접근성").severity("Moderate")
                        .description("오류 발생 시 aria-live 속성이 없어 스크린리더가 오류 메시지를 자동으로 읽지 않습니다. role=alert 또는 aria-live=assertive를 추가하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(7L).title("링크 텍스트 불명확").severity("Moderate")
                        .description("'여기를 클릭' 같은 불명확한 링크 텍스트는 스크린리더 사용자에게 링크 목적을 전달하지 못합니다. 링크 목적을 명확히 하는 텍스트로 변경하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(12L).title("키보드 포커스 순서").severity("Moderate")
                        .description("탭 키 탐색 시 포커스 순서가 시각적 레이아웃과 일치하지 않아 키보드 사용자에게 혼란을 줄 수 있습니다. tabindex를 정리하거나 DOM 순서를 조정하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(13L).title("제목 계층 구조 오류").severity("Moderate")
                        .description("h1 → h3으로 건너뛰는 제목 계층 구조는 스크린리더 탐색을 방해합니다. 순차적인 제목 계층을 유지하세요.").build(),
                // ── Minor (4건) ──
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(8L).title("위치 수정").severity("Minor")
                        .description("일부 UI 요소의 위치가 사용자 예상 동선과 다르게 배치되어 탐색 혼란을 유발할 수 있습니다. 일관된 레이아웃 패턴을 적용하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(9L).title("색상만으로 정보 전달").severity("Minor")
                        .description("오류 상태를 색상만으로 표시하면 색맹 사용자가 인지하기 어렵습니다. 아이콘 또는 텍스트를 함께 사용하세요.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(10L).title("자동 재생 미디어").severity("Minor")
                        .description("자동 재생되는 미디어가 있을 경우 일시 정지 또는 음소거 컨트롤을 제공해야 합니다.").build(),
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(14L).title("언어 속성 누락").severity("Minor")
                        .description("HTML lang 속성이 없어 스크린리더가 올바른 언어로 콘텐츠를 읽지 못할 수 있습니다. lang=ko를 추가하세요.").build()
        );

        // 전체 통합 summary (두 페이지 합산: totalTests=20, passedTests=6)
        return SimulationWcagResponse.builder()
                .summary(SimulationWcagResponse.WcagSummaryDto.builder()
                        .complianceScore(52.0)   // (9 / totalTests 20) * 100 — 반올림
                        .wcagLabel("AA")
                        .totalTests(20)
                        .passedTests(9)
                        .foundIssues(allIssues.size())  // 14
                        .build())
                .distribution(SimulationWcagResponse.WcagDistributionDto.builder()
                        .critical(4)
                        .moderate(6)
                        .minor(4)
                        .build())
                .issues(allIssues)
                .build();
    }
}
