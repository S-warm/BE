package com.swarm.dashboard.service;

import com.swarm.dashboard.dto.request.SimulationCreateRequest;
import com.swarm.dashboard.dto.response.SimulationAiFixResponse;
import com.swarm.dashboard.dto.response.SimulationCreateResponse;
import com.swarm.dashboard.dto.response.SimulationHeatmapResponse;
import com.swarm.dashboard.dto.response.SimulationIssuesResponse;
import com.swarm.dashboard.dto.response.SimulationListResponse;
import com.swarm.dashboard.dto.response.SimulationOverviewResponse;
import com.swarm.dashboard.dto.response.SimulationWcagResponse;
import com.swarm.dashboard.domain.fix.AiFixSuggestion;
import com.swarm.dashboard.domain.fix.AiFixSuggestionRepository;
import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueAgeStats;
import com.swarm.dashboard.domain.issue.IssueAgeStatsRepository;
import com.swarm.dashboard.domain.issue.IssueRepository;
import com.swarm.dashboard.domain.page.PageAgeStats;
import com.swarm.dashboard.domain.page.PageAgeStatsRepository;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationOverview;
import com.swarm.dashboard.domain.simulation.SimulationOverviewRepository;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.domain.simulation.SimulationSettings;
import com.swarm.dashboard.domain.simulation.SimulationSettingsRepository;
import com.swarm.dashboard.domain.user.User;
import com.swarm.dashboard.domain.user.UserRepository;
import com.swarm.dashboard.domain.wcag.WcagIssue;
import com.swarm.dashboard.domain.wcag.WcagIssueRepository;
import com.swarm.dashboard.domain.wcag.WcagResult;
import com.swarm.dashboard.domain.wcag.WcagResultRepository;
import com.swarm.dashboard.domain.wcag.WcagSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationSettingsRepository simulationSettingsRepository;
    private final SimulationOverviewRepository simulationOverviewRepository;
    private final SimulationPageRepository simulationPageRepository;
    private final PageAgeStatsRepository pageAgeStatsRepository;
    private final IssueRepository issueRepository;
    private final IssueAgeStatsRepository issueAgeStatsRepository;
    private final AiFixSuggestionRepository aiFixSuggestionRepository;
    private final WcagResultRepository wcagResultRepository;
    private final WcagIssueRepository wcagIssueRepository;
    private final UserRepository userRepository;

    private static final List<String> AGE_BANDS = List.of("10대", "20대", "30대", "40대", "50대", "60대", "70대");

    // ────────────────────────────────────────
    // POST - 시뮬레이션 생성
    // ────────────────────────────────────────
    @Transactional
    public SimulationCreateResponse createSimulation(UUID userId, SimulationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id=" + userId));

        Simulation simulation = Simulation.builder()
                .user(user)
                .title(request.getTitle())
                .targetUrl(request.getTargetUrl())
                .status("pending")
                .createdAt(OffsetDateTime.now())
                .build();

        Simulation saved = simulationRepository.save(simulation);

        SimulationSettings settings = SimulationSettings.builder()
                .simulation(saved)
                .digitalLiteracy(request.getDigitalLiteracy())
                .successCondition(request.getSuccessCondition())
                .personaDevice(request.getPersonaDevice())
                .ageCount10(request.getAgeCount10())
                .ageCount20(request.getAgeCount20())
                .ageCount30(request.getAgeCount30())
                .ageCount40(request.getAgeCount40())
                .ageCount50(request.getAgeCount50())
                .ageCount60(request.getAgeCount60())
                .ageCount70(request.getAgeCount70())
                .visionImpairment(request.getVisionImpairment())
                .attentionLevel(request.getAttentionLevel())
                .build();

        simulationSettingsRepository.save(settings);

        return SimulationCreateResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // ────────────────────────────────────────
    // GET - 사용자별 시뮬레이션 목록 조회
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
        SimulationOverview overview = simulationOverviewRepository.findBySimulationId(simulationId)
                .orElseThrow(() -> new RuntimeException("Overview 데이터를 찾을 수 없습니다. id=" + simulationId));

        List<SimulationPage> pages = simulationPageRepository.findBySimulationIdOrderByPageOrder(simulationId);

        int totalAgents = overview.getTestedAgentCount() != null ? overview.getTestedAgentCount() : 0;
        int successCount = overview.getSuccessEventCount() != null ? overview.getSuccessEventCount() : 0;
        double taskSuccessRate = totalAgents == 0 ? 0.0
                : Math.round(successCount * 1000.0 / totalAgents) / 10.0;
        int avgCompletionSeconds = overview.getAvgCompletionMs() != null
                ? overview.getAvgCompletionMs() / 1000 : 0;
        int dropOffAgents = totalAgents - successCount;

        List<SimulationOverviewResponse.FunnelPanelDto> funnelPanels = pages.stream().map(page -> {
            List<PageAgeStats> ageStatsList = pageAgeStatsRepository.findByPage_Id(page.getId());

            int totalEntered = ageStatsList.stream()
                    .mapToInt(s -> s.getEntered() != null ? s.getEntered() : 0).sum();
            int totalPassed = ageStatsList.stream()
                    .mapToInt(s -> s.getPassed() != null ? s.getPassed() : 0).sum();
            double panelSuccessRate = totalEntered == 0 ? 0.0
                    : Math.round(totalPassed * 1000.0 / totalEntered) / 10.0;
            // double 유지 후 나눗셈 (int 캐스팅 시 1초 미만 체류시간 0 손실 방지)
            double avgTimeMsDouble = ageStatsList.stream()
                    .mapToInt(s -> s.getAvgTimeMs() != null ? s.getAvgTimeMs() : 0)
                    .average().orElse(0.0);
            int avgTimeSeconds = (int) Math.round(avgTimeMsDouble / 1000.0);

            // 고정 7개 키 보장 — DB에 없는 연령대도 entered=0으로 포함
            Map<String, SimulationOverviewResponse.AgeGroupDto> agentsByAge = new LinkedHashMap<>();
            for (String band : AGE_BANDS) {
                agentsByAge.put(band, SimulationOverviewResponse.AgeGroupDto.builder()
                        .entered(0).passed(0).dropOff(0).successRate(0.0).build());
            }
            for (PageAgeStats stats : ageStatsList) {
                // AGE_BANDS 범위 외 데이터 필터링 (방어 로직)
                if (!AGE_BANDS.contains(stats.getAgeBand())) continue;
                int entered = stats.getEntered() != null ? stats.getEntered() : 0;
                int passed = stats.getPassed() != null ? stats.getPassed() : 0;
                int dropOff = entered - passed;
                double successRate = entered == 0 ? 0.0
                        : Math.round(passed * 1000.0 / entered) / 10.0;
                agentsByAge.put(stats.getAgeBand(), SimulationOverviewResponse.AgeGroupDto.builder()
                        .entered(entered).passed(passed).dropOff(dropOff).successRate(successRate)
                        .build());
            }

            return SimulationOverviewResponse.FunnelPanelDto.builder()
                    .order(page.getPageOrder() != null ? page.getPageOrder() : 0)
                    .pageName(page.getPageName())
                    .pageUrl(page.getPageUrl())
                    .totalEntered(totalEntered)
                    .totalPassed(totalPassed)
                    .panelSuccessRate(panelSuccessRate)
                    .avgTimeSeconds(avgTimeSeconds)
                    .agentsByAge(agentsByAge)
                    .build();
        }).collect(Collectors.toList());

        return SimulationOverviewResponse.builder()
                .summary(SimulationOverviewResponse.SummaryDto.builder()
                        .taskSuccessRate(taskSuccessRate)
                        .totalAgents(totalAgents)
                        .avgCompletionSeconds(avgCompletionSeconds)
                        .dropOffAgents(dropOffAgents)
                        .build())
                .funnelPanels(funnelPanels)
                .build();
    }

    // ────────────────────────────────────────
    // GET - Issues 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationIssuesResponse getIssues(UUID simulationId) {
        List<Issue> issues = issueRepository.findBySimulationIdOrderByPageAndSeverity(simulationId);

        // N+1 방지: simulation 전체 stats 한 번에 조회 후 issueId로 그룹핑
        List<IssueAgeStats> allStats = issueAgeStatsRepository.findBySimulationId(simulationId);
        Map<UUID, List<IssueAgeStats>> statsByIssueId = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getId().getIssueId()));

        Map<SimulationPage, List<Issue>> byPage = issues.stream()
                .collect(Collectors.groupingBy(Issue::getPage, LinkedHashMap::new, Collectors.toList()));

        List<SimulationIssuesResponse.IssuePageDto> pages = byPage.entrySet().stream().map(entry -> {
            SimulationPage page = entry.getKey();
            // severity 정렬: CRITICAL→HIGH→MEDIUM→LOW (enum ordinal 순서)
            List<Issue> pageIssues = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(i -> i.getSeverity() != null ? i.getSeverity().ordinal() : Integer.MAX_VALUE))
                    .collect(Collectors.toList());

            List<SimulationIssuesResponse.IssueDto> issueDtos = pageIssues.stream().map(issue -> {
                List<IssueAgeStats> ageStats = statsByIssueId.getOrDefault(issue.getId(), List.of());
                int affectedUsersCount = ageStats.stream()
                        .mapToInt(s -> s.getAffectedUsers() != null ? s.getAffectedUsers() : 0).sum();
                double affectedUsersPercent = ageStats.stream()
                        .mapToDouble(s -> s.getAffectedPercent() != null
                                ? s.getAffectedPercent().doubleValue() : 0.0)
                        .average().orElse(0.0);

                return SimulationIssuesResponse.IssueDto.builder()
                        .issueId(issue.getId())
                        .title(issue.getTitle())
                        .category(issue.getCategory())
                        .severity(issue.getSeverity())
                        .affectedUsersCount(affectedUsersCount)
                        .affectedUsersPercent(Math.round(affectedUsersPercent * 10.0) / 10.0)
                        .description(issue.getDescription())
                        .targetHtml(issue.getTargetHtml())
                        .tags(issue.getTags() != null ? issue.getTags() : List.of())
                        .build();
            }).collect(Collectors.toList());

            return SimulationIssuesResponse.IssuePageDto.builder()
                    .order(page.getPageOrder() != null ? page.getPageOrder() : 0)
                    .pageName(page.getPageName())
                    .pageUrl(page.getPageUrl())
                    .screenshotUrl(page.getScreenshotPath())
                    .totalIssueCount(issueDtos.size())
                    .issues(issueDtos)
                    .build();
        }).collect(Collectors.toList());

        return SimulationIssuesResponse.builder().pages(pages).build();
    }

    // ────────────────────────────────────────
    // GET - AI 수정 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationAiFixResponse getAiFix(UUID simulationId) {
        List<AiFixSuggestion> fixes = aiFixSuggestionRepository.findBySimulationId(simulationId);

        Map<SimulationPage, List<AiFixSuggestion>> byPage = fixes.stream()
                .filter(f -> f.getIssue() != null && f.getIssue().getPage() != null)
                .collect(Collectors.groupingBy(f -> f.getIssue().getPage(), LinkedHashMap::new, Collectors.toList()));

        List<SimulationAiFixResponse.AiFixPageDto> pages = byPage.entrySet().stream().map(entry -> {
            SimulationPage page = entry.getKey();
            List<AiFixSuggestion> pageFixes = entry.getValue();

            List<SimulationAiFixResponse.AiFixDto> fixDtos = pageFixes.stream().map(fix ->
                    SimulationAiFixResponse.AiFixDto.builder()
                            .issueId(fix.getIssue().getId())
                            .title(fix.getTitle())
                            .severity(fix.getSeverity())
                            .affectedUsersCount(fix.getImpactedUsers() != null ? fix.getImpactedUsers() : 0)
                            .beforeCode(fix.getBeforeCode())
                            .afterCode(fix.getAfterCode())
                            .impactDescription(fix.getImpactSummary())
                            .changeDescription(fix.getChangeSummaryBody())
                            .build()
            ).collect(Collectors.toList());

            return SimulationAiFixResponse.AiFixPageDto.builder()
                    .order(page.getPageOrder() != null ? page.getPageOrder() : 0)
                    .pageName(page.getPageName())
                    .pageUrl(page.getPageUrl())
                    .screenshotUrl(page.getScreenshotPath())
                    .totalFixCount(fixDtos.size())
                    .fixes(fixDtos)
                    .build();
        }).collect(Collectors.toList());

        return SimulationAiFixResponse.builder().pages(pages).build();
    }

    // ────────────────────────────────────────
    // GET - 히트맵 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationHeatmapResponse getHeatmap(UUID simulationId, String ageGroup, int pageNum, int size) {
        List<SimulationPage> simPages = simulationPageRepository.findBySimulationIdOrderByPageOrder(simulationId);

        // N+1 방지: simulation 전체 stats 한 번에 조회 후 pageId로 그룹핑
        List<IssueAgeStats> allSimStats = "all".equals(ageGroup)
                ? issueAgeStatsRepository.findBySimulationId(simulationId)
                : issueAgeStatsRepository.findBySimulationIdAndAgeBand(simulationId, ageGroup);
        Map<UUID, List<IssueAgeStats>> statsByPageId = allSimStats.stream()
                .collect(Collectors.groupingBy(s -> s.getIssue().getPage().getId()));

        List<SimulationHeatmapResponse.HeatmapPageDto> pageDtos = simPages.stream().map(simPage -> {
            List<IssueAgeStats> allStats = statsByPageId.getOrDefault(simPage.getId(), List.of());

            List<SimulationHeatmapResponse.ErrorPointDto> errorPoints = allStats.stream().map(stats -> {
                int timeoutCnt = stats.getTimeoutCount() != null ? stats.getTimeoutCount() : 0;
                int networkCnt = stats.getNetworkCount() != null ? stats.getNetworkCount() : 0;
                int consoleCnt = stats.getConsoleCount() != null ? stats.getConsoleCount() : 0;
                int count = timeoutCnt + networkCnt + consoleCnt;

                String severity;
                if (count >= 15) severity = "CRITICAL";
                else if (count >= 8) severity = "HIGH";
                else if (count >= 4) severity = "MEDIUM";
                else severity = "LOW";

                return SimulationHeatmapResponse.ErrorPointDto.builder()
                        .x(stats.getCoordX() != null ? stats.getCoordX().doubleValue() : 0.0)
                        .y(stats.getCoordY() != null ? stats.getCoordY().doubleValue() : 0.0)
                        .count(count)
                        .severity(severity)
                        .errorType(stats.getErrorType())
                        .affectedUsersCount(stats.getAffectedUsers() != null ? stats.getAffectedUsers() : 0)
                        .blockRate(stats.getBlockRate() != null ? stats.getBlockRate().doubleValue() : 0.0)
                        .repeatCount(stats.getRepeatCount() != null ? stats.getRepeatCount().doubleValue() : 0.0)
                        .description(stats.getDescription())
                        .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                                .timeout(timeoutCnt).network(networkCnt).console(consoleCnt)
                                .build())
                        .issueId(stats.getId().getIssueId())
                        .ageBand(stats.getId().getAgeBand())
                        .build();
            }).collect(Collectors.toList());

            int totalCount = errorPoints.size();
            int startIdx = pageNum * size;
            int endIdx = Math.min(startIdx + size, totalCount);
            List<SimulationHeatmapResponse.ErrorPointDto> paginatedPoints =
                    startIdx >= totalCount ? new ArrayList<>() : errorPoints.subList(startIdx, endIdx);

            SimulationHeatmapResponse.PaginationDto pagination = SimulationHeatmapResponse.PaginationDto.builder()
                    .totalCount(totalCount)
                    .currentPage(pageNum)
                    .pageSize(size)
                    .hasMore(endIdx < totalCount)
                    .build();

            return SimulationHeatmapResponse.HeatmapPageDto.builder()
                    .order(simPage.getPageOrder() != null ? simPage.getPageOrder() : 0)
                    .pageName(simPage.getPageName())
                    .pageUrl(simPage.getPageUrl())
                    .screenshotUrl(simPage.getScreenshotPath())
                    .totalErrorCount(totalCount)
                    .errorPoints(paginatedPoints)
                    .currentAgeGroup(ageGroup)
                    .pagination(pagination)
                    .build();
        }).collect(Collectors.toList());

        return SimulationHeatmapResponse.builder().pages(pageDtos).build();
    }

    // ────────────────────────────────────────
    // GET - WCAG 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationWcagResponse getWcag(UUID simulationId) {
        List<WcagResult> wcagResults = wcagResultRepository.findBySimulationId(simulationId);
        if (wcagResults.isEmpty()) {
            throw new RuntimeException("WCAG 데이터를 찾을 수 없습니다. id=" + simulationId);
        }

        // severity 정렬: Critical→Moderate→Minor (enum ordinal 순서)
        List<WcagIssue> allWcagIssues = wcagIssueRepository.findBySimulationId(simulationId).stream()
                .sorted(Comparator.comparingInt(i -> i.getSeverity() != null ? i.getSeverity().ordinal() : Integer.MAX_VALUE))
                .collect(Collectors.toList());

        int totalTests = wcagResults.stream()
                .mapToInt(r -> r.getTotalTests() != null ? r.getTotalTests() : 0).sum();
        int passedTests = wcagResults.stream()
                .mapToInt(r -> r.getPassedTests() != null ? r.getPassedTests() : 0).sum();
        double complianceScore = totalTests == 0 ? 0.0
                : Math.round(passedTests * 1000.0 / totalTests) / 10.0;
        String wcagLabel = wcagResults.stream()
                .map(WcagResult::getWcagLabel)
                .filter(l -> l != null)
                .findFirst()
                .orElse("AA");

        int critical = (int) allWcagIssues.stream().filter(i -> i.getSeverity() == WcagSeverity.Critical).count();
        int moderate = (int) allWcagIssues.stream().filter(i -> i.getSeverity() == WcagSeverity.Moderate).count();
        int minor = (int) allWcagIssues.stream().filter(i -> i.getSeverity() == WcagSeverity.Minor).count();

        List<SimulationWcagResponse.WcagIssueDto> issueDtos = allWcagIssues.stream().map(issue ->
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(issue.getId())
                        .title(issue.getTitle())
                        .severity(issue.getSeverity())
                        .description(issue.getDescription())
                        .build()
        ).collect(Collectors.toList());

        return SimulationWcagResponse.builder()
                .summary(SimulationWcagResponse.WcagSummaryDto.builder()
                        .complianceScore(complianceScore)
                        .wcagLabel(wcagLabel)
                        .totalTests(totalTests)
                        .passedTests(passedTests)
                        .foundIssues(allWcagIssues.size())
                        .build())
                .distribution(SimulationWcagResponse.WcagDistributionDto.builder()
                        .critical(critical).moderate(moderate).minor(minor)
                        .build())
                .issues(issueDtos)
                .build();
    }
}
