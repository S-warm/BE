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
import com.swarm.dashboard.domain.simulation.SimulationOverview;
import com.swarm.dashboard.domain.simulation.SimulationOverviewRepository;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.domain.simulation.SimulationSettings;
import com.swarm.dashboard.domain.simulation.SimulationSettingsRepository;
import com.swarm.dashboard.domain.page.PageAgeStats;
import com.swarm.dashboard.domain.page.PageAgeStatsRepository;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueAgeStats;
import com.swarm.dashboard.domain.issue.IssueAgeStatsRepository;
import com.swarm.dashboard.domain.issue.IssueRepository;
import com.swarm.dashboard.domain.fix.AiFixSuggestion;
import com.swarm.dashboard.domain.fix.AiFixSuggestionRepository;
import com.swarm.dashboard.domain.wcag.WcagIssue;
import com.swarm.dashboard.domain.wcag.WcagIssueRepository;
import com.swarm.dashboard.domain.wcag.WcagResult;
import com.swarm.dashboard.domain.wcag.WcagResultRepository;
import com.swarm.dashboard.domain.user.User;
import com.swarm.dashboard.domain.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final SimulationOverviewRepository simulationOverviewRepository;
    private final SimulationSettingsRepository simulationSettingsRepository;
    private final SimulationPageRepository simulationPageRepository;
    private final PageAgeStatsRepository pageAgeStatsRepository;
    private final IssueRepository issueRepository;
    private final IssueAgeStatsRepository issueAgeStatsRepository;
    private final AiFixSuggestionRepository aiFixSuggestionRepository;
    private final WcagResultRepository wcagResultRepository;
    private final WcagIssueRepository wcagIssueRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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
        SimulationOverview overview = simulationOverviewRepository.findBySimulationId(simulationId)
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));

        List<SimulationOverviewResponse.FunnelPanelDto> funnelPanels = simulationPageRepository
                .findBySimulationIdOrderByPageOrder(simulationId)
                .stream()
                .map(this::buildOverviewPanel)
                .toList();

        int testedAgentCount = defaultInt(overview.getTestedAgentCount());
        int successEventCount = defaultInt(overview.getSuccessEventCount());
        double successRate = testedAgentCount == 0
                ? defaultDecimal(overview.getConversionRate())
                : roundToOneDecimal((successEventCount * 100.0) / testedAgentCount);

        return SimulationOverviewResponse.builder()
                .summary(SimulationOverviewResponse.SummaryDto.builder()
                        .taskSuccessRate(successRate)
                        .totalAgents(testedAgentCount)
                        .avgCompletionSeconds(defaultInt(overview.getAvgCompletionMs()) / 1000)
                        .dropOffAgents(Math.max(0, testedAgentCount - successEventCount))
                        .build())
                .funnelPanels(funnelPanels)
                .build();
    }

    // ────────────────────────────────────────
    // GET - Issues 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationIssuesResponse getIssues(UUID simulationId) {
        getSimulationOrThrow(simulationId);

        List<SimulationPage> pages = simulationPageRepository.findBySimulationIdOrderByPageOrder(simulationId);
        List<Issue> issues = issueRepository.findBySimulationId(simulationId);
        Map<UUID, List<Issue>> issuesByPageId = issues.stream()
                .filter(issue -> issue.getPage() != null)
                .collect(Collectors.groupingBy(issue -> issue.getPage().getId()));

        List<SimulationIssuesResponse.IssuePageDto> issuePages = pages.stream()
                .map(page -> buildIssuePage(page, issuesByPageId.getOrDefault(page.getId(), List.of())))
                .filter(page -> !page.getIssues().isEmpty())
                .toList();

        return SimulationIssuesResponse.builder()
                .pages(issuePages)
                .build();
    }

    // ────────────────────────────────────────
    // GET - AI 수정 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationAiFixResponse getAiFix(UUID simulationId) {
        getSimulationOrThrow(simulationId);
        List<SimulationPage> pages = simulationPageRepository.findBySimulationIdOrderByPageOrder(simulationId);
        List<AiFixSuggestion> fixes = aiFixSuggestionRepository.findBySimulationId(simulationId);
        Map<UUID, List<AiFixSuggestion>> fixesByPageId = fixes.stream()
                .filter(fix -> fix.getPage() != null)
                .collect(Collectors.groupingBy(fix -> fix.getPage().getId()));

        List<SimulationAiFixResponse.AiFixPageDto> aiFixPages = pages.stream()
                .map(page -> buildAiFixPage(page, fixesByPageId.getOrDefault(page.getId(), List.of())))
                .filter(page -> !page.getFixes().isEmpty())
                .toList();

        return SimulationAiFixResponse.builder()
                .pages(aiFixPages)
                .build();
    }

    // ────────────────────────────────────────
    // GET - 히트맵 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationHeatmapResponse getHeatmap(UUID simulationId, String ageGroup, int page, int size) {
        getSimulationOrThrow(simulationId);
        String normalizedAgeGroup = normalizeAgeGroup(ageGroup);
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        List<SimulationPage> pages = simulationPageRepository.findBySimulationIdOrderByPageOrder(simulationId);
        List<Issue> issues = issueRepository.findBySimulationId(simulationId);
        Map<UUID, List<IssueAgeStats>> statsByIssueId = issues.stream()
                .collect(Collectors.toMap(
                        Issue::getId,
                        issue -> issueAgeStatsRepository.findByIssueId(issue.getId())
                ));
        Map<UUID, List<Issue>> issuesByPageId = issues.stream()
                .filter(issue -> issue.getPage() != null)
                .collect(Collectors.groupingBy(issue -> issue.getPage().getId()));

        List<SimulationHeatmapResponse.HeatmapPageDto> heatmapPages = pages.stream()
                .map(simulationPage -> buildHeatmapPage(
                        simulationPage,
                        issuesByPageId.getOrDefault(simulationPage.getId(), List.of()),
                        statsByIssueId,
                        normalizedAgeGroup,
                        safePage,
                        safeSize
                ))
                .toList();

        return SimulationHeatmapResponse.builder()
                .pages(heatmapPages)
                .build();
    }

    // ────────────────────────────────────────
    // GET - WCAG 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationWcagResponse getWcag(UUID simulationId) {
        getSimulationOrThrow(simulationId);
        List<WcagResult> results = wcagResultRepository.findBySimulationId(simulationId);
        List<WcagIssue> issues = results.stream()
                .flatMap(result -> wcagIssueRepository.findByWcagResultId(result.getId()).stream())
                .sorted(Comparator
                        .comparingInt((WcagIssue issue) -> wcagSeverityRank(issue.getSeverity()))
                        .thenComparing(WcagIssue::getIssueNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int totalTests = results.stream().map(WcagResult::getTotalTests).mapToInt(this::defaultInt).sum();
        int passedTests = results.stream().map(WcagResult::getPassedTests).mapToInt(this::defaultInt).sum();
        int foundIssues = results.stream().map(WcagResult::getFoundIssues).mapToInt(this::defaultInt).sum();
        double complianceScore = totalTests == 0
                ? 0.0
                : roundToOneDecimal((passedTests * 100.0) / totalTests);
        String wcagLabel = results.stream()
                .map(WcagResult::getWcagLabel)
                .filter(label -> label != null && !label.isBlank())
                .findFirst()
                .orElse("AA");

        int criticalCount = (int) issues.stream().filter(issue -> "Critical".equalsIgnoreCase(issue.getSeverity())).count();
        int moderateCount = (int) issues.stream().filter(issue -> "Moderate".equalsIgnoreCase(issue.getSeverity())).count();
        int minorCount = (int) issues.stream().filter(issue -> "Minor".equalsIgnoreCase(issue.getSeverity())).count();

        return SimulationWcagResponse.builder()
                .summary(SimulationWcagResponse.WcagSummaryDto.builder()
                        .complianceScore(complianceScore)
                        .wcagLabel(wcagLabel)
                        .totalTests(totalTests)
                        .passedTests(passedTests)
                        .foundIssues(foundIssues)
                        .build())
                .distribution(SimulationWcagResponse.WcagDistributionDto.builder()
                        .critical(criticalCount)
                        .moderate(moderateCount)
                        .minor(minorCount)
                        .build())
                .issues(issues.stream().map(this::buildWcagIssueDto).toList())
                .build();
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

    private static final String[] AGE_KEYS = {
            "10대", "20대", "30대", "40대", "50대", "60대", "70대", "80대"
    };

    private SimulationOverviewResponse.FunnelPanelDto buildOverviewPanel(SimulationPage page) {
        List<PageAgeStats> stats = pageAgeStatsRepository.findByPageId(page.getId());
        Map<String, SimulationOverviewResponse.AgeGroupDto> agentsByAge = buildAgeGroup(stats);

        int totalEntered = stats.stream().map(PageAgeStats::getEntered).mapToInt(this::defaultInt).sum();
        int totalPassed = stats.stream().map(PageAgeStats::getPassed).mapToInt(this::defaultInt).sum();

        return SimulationOverviewResponse.FunnelPanelDto.builder()
                .order(defaultInt(page.getPageOrder()))
                .pageName(page.getPageName())
                .pageUrl(page.getPageUrl() != null ? page.getPageUrl() : "")
                .totalEntered(totalEntered)
                .totalPassed(totalPassed)
                .panelSuccessRate(totalEntered == 0 ? 0.0 : roundToOneDecimal((totalPassed * 100.0) / totalEntered))
                .avgTimeSeconds(estimateAverageTimeSeconds(page.getPageOrder()))
                .agentsByAge(agentsByAge)
                .build();
    }

    private Map<String, SimulationOverviewResponse.AgeGroupDto> buildAgeGroup(
            List<PageAgeStats> stats) {
        Map<String, PageAgeStats> statsByAgeBand = stats.stream()
                .sorted(Comparator.comparing(PageAgeStats::getAgeBand))
                .collect(Collectors.toMap(
                        PageAgeStats::getAgeBand,
                        stat -> stat,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<String, SimulationOverviewResponse.AgeGroupDto> map = new LinkedHashMap<>();
        for (String ageKey : AGE_KEYS) {
            PageAgeStats stat = statsByAgeBand.get(ageKey);
            int entered = stat != null ? defaultInt(stat.getEntered()) : 0;
            int passed = stat != null ? defaultInt(stat.getPassed()) : 0;
            int dropOff = stat != null ? defaultInt(stat.getDropOff()) : Math.max(0, entered - passed);
            double successRate = stat != null && stat.getSuccessRate() != null
                    ? stat.getSuccessRate().doubleValue()
                    : entered == 0 ? 0.0 : roundToOneDecimal((passed * 100.0) / entered);

            map.put(ageKey, SimulationOverviewResponse.AgeGroupDto.builder()
                    .entered(entered)
                    .passed(passed)
                    .dropOff(dropOff)
                    .successRate(successRate)
                    .build());
        }
        return map;
    }

    private int estimateAverageTimeSeconds(Integer pageOrder) {
        if (pageOrder == null) {
            return 0;
        }

        return switch (pageOrder) {
            case 1 -> 12;
            case 2 -> 25;
            case 3 -> 40;
            case 4 -> 18;
            default -> 15;
        };
    }

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    private double defaultDecimal(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private SimulationIssuesResponse.IssuePageDto buildIssuePage(
            SimulationPage page,
            List<Issue> pageIssues
    ) {
        List<SimulationIssuesResponse.IssueDto> issues = pageIssues.stream()
                .sorted(Comparator
                        .comparingInt((Issue issue) -> severityRank(issue.getSeverity()))
                        .thenComparing(Issue::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::buildIssueDto)
                .toList();

        return SimulationIssuesResponse.IssuePageDto.builder()
                .order(defaultInt(page.getPageOrder()))
                .pageName(page.getPageName())
                .pageUrl(page.getPageUrl() != null ? page.getPageUrl() : "")
                .screenshotUrl(page.getScreenshotPath() != null ? page.getScreenshotPath() : "")
                .totalIssueCount(issues.size())
                .issues(issues)
                .build();
    }

    private SimulationIssuesResponse.IssueDto buildIssueDto(Issue issue) {
        List<IssueAgeStats> ageStats = issueAgeStatsRepository.findByIssueId(issue.getId());
        int affectedUsersCount = ageStats.stream()
                .map(IssueAgeStats::getAffectedUsers)
                .mapToInt(this::defaultInt)
                .sum();
        double affectedUsersPercent = ageStats.isEmpty()
                ? 0.0
                : roundToOneDecimal(ageStats.stream()
                .map(IssueAgeStats::getAffectedPercent)
                .filter(value -> value != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0));

        return SimulationIssuesResponse.IssueDto.builder()
                .issueId(issue.getId())
                .title(issue.getTitle())
                .category(issue.getCategory() != null ? issue.getCategory() : "")
                .severity(issue.getSeverity() != null ? issue.getSeverity() : "LOW")
                .affectedUsersCount(affectedUsersCount)
                .affectedUsersPercent(affectedUsersPercent)
                .description(issue.getDescription() != null ? issue.getDescription() : "")
                .targetHtml(issue.getTargetHtml() != null ? issue.getTargetHtml() : "")
                .tags(parseTags(issue.getTags()))
                .build();
    }

    private int severityRank(String severity) {
        if (severity == null) {
            return Integer.MAX_VALUE;
        }

        return switch (severity) {
            case "CRITICAL" -> 0;
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 4;
        };
    }

    private List<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(rawTags, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse issue tags: {}", rawTags, e);
            return List.of();
        }
    }

    private SimulationAiFixResponse.AiFixPageDto buildAiFixPage(
            SimulationPage page,
            List<AiFixSuggestion> pageFixes
    ) {
        List<SimulationAiFixResponse.AiFixDto> fixes = pageFixes.stream()
                .sorted(Comparator
                        .comparingInt((AiFixSuggestion fix) -> severityRank(fix.getSeverity()))
                        .thenComparing(AiFixSuggestion::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::buildAiFixDto)
                .toList();

        return SimulationAiFixResponse.AiFixPageDto.builder()
                .order(defaultInt(page.getPageOrder()))
                .pageName(page.getPageName())
                .pageUrl(page.getPageUrl() != null ? page.getPageUrl() : "")
                .screenshotUrl(page.getScreenshotPath() != null ? page.getScreenshotPath() : "")
                .totalFixCount(fixes.size())
                .fixes(fixes)
                .build();
    }

    private SimulationAiFixResponse.AiFixDto buildAiFixDto(AiFixSuggestion fix) {
        return SimulationAiFixResponse.AiFixDto.builder()
                .issueId(fix.getIssue() != null ? fix.getIssue().getId() : null)
                .title(fix.getTitle() != null ? fix.getTitle() : "")
                .severity(fix.getSeverity() != null ? fix.getSeverity() : "LOW")
                .affectedUsersCount(defaultInt(fix.getImpactedUsers()))
                .beforeCode(fix.getBeforeCode() != null ? fix.getBeforeCode() : "")
                .afterCode(fix.getAfterCode() != null ? fix.getAfterCode() : "")
                .impactDescription(fix.getImpactSummary() != null ? fix.getImpactSummary() : "")
                .changeDescription(fix.getChangeSummaryBody() != null ? fix.getChangeSummaryBody() : "")
                .build();
    }

    private SimulationHeatmapResponse.HeatmapPageDto buildHeatmapPage(
            SimulationPage page,
            List<Issue> pageIssues,
            Map<UUID, List<IssueAgeStats>> statsByIssueId,
            String ageGroup,
            int requestedPage,
            int pageSize
    ) {
        List<SimulationHeatmapResponse.ErrorPointDto> filteredPoints = pageIssues.stream()
                .flatMap(issue -> statsByIssueId.getOrDefault(issue.getId(), List.of()).stream()
                        .filter(stat -> matchesAgeGroup(stat, ageGroup))
                        .map(stat -> buildHeatmapPoint(issue, stat)))
                .toList();

        int totalCount = filteredPoints.size();
        int startIndex = Math.min(requestedPage * pageSize, totalCount);
        int endIndex = Math.min(startIndex + pageSize, totalCount);
        List<SimulationHeatmapResponse.ErrorPointDto> paginatedPoints = filteredPoints.subList(startIndex, endIndex);

        return SimulationHeatmapResponse.HeatmapPageDto.builder()
                .order(defaultInt(page.getPageOrder()))
                .pageName(page.getPageName())
                .pageUrl(page.getPageUrl() != null ? page.getPageUrl() : "")
                .screenshotUrl(page.getScreenshotPath() != null ? page.getScreenshotPath() : "")
                .totalErrorCount(totalCount)
                .errorPoints(paginatedPoints)
                .currentAgeGroup(ageGroup)
                .pagination(SimulationHeatmapResponse.PaginationDto.builder()
                        .totalCount(totalCount)
                        .currentPage(requestedPage)
                        .pageSize(pageSize)
                        .hasMore(endIndex < totalCount)
                        .build())
                .build();
    }

    private SimulationHeatmapResponse.ErrorPointDto buildHeatmapPoint(Issue issue, IssueAgeStats stat) {
        int timeoutCount = defaultInt(stat.getTimeoutCount());
        int networkCount = defaultInt(stat.getNetworkCount());
        int consoleCount = defaultInt(stat.getConsoleCount());
        int totalCount = Math.max(1, timeoutCount + networkCount + consoleCount);

        return SimulationHeatmapResponse.ErrorPointDto.builder()
                .x(defaultDecimal(stat.getCoordX()))
                .y(defaultDecimal(stat.getCoordY()))
                .count(totalCount)
                .severity(toHeatmapSeverity(totalCount))
                .errorType(resolveErrorType(stat))
                .affectedUsersCount(defaultInt(stat.getAffectedUsers()))
                .blockRate(defaultDecimal(stat.getBlockRate()))
                .repeatCount(defaultDecimal(stat.getRepeatCount()))
                .description(buildHeatmapDescription(issue, stat))
                .errorBreakdown(SimulationHeatmapResponse.ErrorBreakdownDto.builder()
                        .timeout(timeoutCount)
                        .network(networkCount)
                        .console(consoleCount)
                        .build())
                .issueId(issue.getId())
                .ageBand(stat.getId() != null ? stat.getId().getAgeBand() : "all")
                .build();
    }

    private boolean matchesAgeGroup(IssueAgeStats stat, String ageGroup) {
        String statAgeBand = stat.getId() != null ? stat.getId().getAgeBand() : null;
        if ("all".equals(ageGroup)) {
            return "all".equals(statAgeBand);
        }
        return ageGroup.equals(statAgeBand);
    }

    private String normalizeAgeGroup(String ageGroup) {
        if (ageGroup == null || ageGroup.isBlank()) {
            return "all";
        }
        return "all".equals(ageGroup) ? "all" : ageGroup;
    }

    private String toHeatmapSeverity(int count) {
        if (count >= 15) {
            return "CRITICAL";
        }
        if (count >= 8) {
            return "HIGH";
        }
        if (count >= 4) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String resolveErrorType(IssueAgeStats stat) {
        if (stat.getErrorType() != null && !stat.getErrorType().isBlank()) {
            return stat.getErrorType();
        }

        int timeoutCount = defaultInt(stat.getTimeoutCount());
        int networkCount = defaultInt(stat.getNetworkCount());
        int consoleCount = defaultInt(stat.getConsoleCount());
        if (timeoutCount >= networkCount && timeoutCount >= consoleCount) {
            return "Timeout";
        }
        if (networkCount >= consoleCount) {
            return "Network";
        }
        return "Console";
    }

    private String buildHeatmapDescription(Issue issue, IssueAgeStats stat) {
        String severity = issue.getSeverity() != null ? issue.getSeverity() : "LOW";
        String title = issue.getTitle() != null ? issue.getTitle() : "오류";
        String ageBand = stat.getId() != null && stat.getId().getAgeBand() != null ? stat.getId().getAgeBand() : "전체";
        return ageBand + " 사용자 구간에서 " + severity + " 수준의 '" + title + "' 문제가 집중적으로 관측되었습니다.";
    }

    private int wcagSeverityRank(String severity) {
        if (severity == null) {
            return Integer.MAX_VALUE;
        }

        return switch (severity) {
            case "Critical", "CRITICAL" -> 0;
            case "Moderate", "MODERATE" -> 1;
            case "Minor", "MINOR" -> 2;
            default -> 3;
        };
    }

    private SimulationWcagResponse.WcagIssueDto buildWcagIssueDto(WcagIssue issue) {
        long issueId = issue.getIssueNo() != null ? issue.getIssueNo().longValue() : 0L;
        return SimulationWcagResponse.WcagIssueDto.builder()
                .wcagIssueId(issueId)
                .title(issue.getTitle() != null ? issue.getTitle() : "")
                .severity(issue.getSeverity() != null ? issue.getSeverity() : "Minor")
                .description(issue.getDescription() != null ? issue.getDescription() : "")
                .build();
    }

}
