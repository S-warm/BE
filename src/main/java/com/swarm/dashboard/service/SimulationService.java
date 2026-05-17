package com.swarm.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarm.dashboard.domain.fix.AiFixSuggestion;
import com.swarm.dashboard.domain.fix.AiFixSuggestionRepository;
import com.swarm.dashboard.domain.heatmap.HeatmapPoint;
import com.swarm.dashboard.domain.heatmap.HeatmapPointRepository;
import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueAgeStats;
import com.swarm.dashboard.domain.issue.IssueAgeStatsRepository;
import com.swarm.dashboard.domain.issue.IssueRepository;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.*;
import com.swarm.dashboard.domain.user.User;
import com.swarm.dashboard.domain.user.UserRepository;
import com.swarm.dashboard.domain.wcag.WcagIssue;
import com.swarm.dashboard.domain.wcag.WcagIssueRepository;
import com.swarm.dashboard.domain.wcag.WcagResult;
import com.swarm.dashboard.domain.wcag.WcagResultRepository;
import com.swarm.dashboard.dto.request.SimulationCreateRequest;
import com.swarm.dashboard.dto.response.*;
import com.swarm.dashboard.util.AgeBandConverter;
import com.swarm.dashboard.util.S3PresignService;
import com.swarm.dashboard.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationRepository simulationRepository;
    private final SimulationSettingsRepository simulationSettingsRepository;
    private final SimulationOverviewRepository simulationOverviewRepository;
    private final SimulationPageRepository simulationPageRepository;
    private final AgeOverviewRepository ageOverviewRepository;
    private final IssueRepository issueRepository;
    private final IssueAgeStatsRepository issueAgeStatsRepository;
    private final AiFixSuggestionRepository aiFixSuggestionRepository;
    private final WcagResultRepository wcagResultRepository;
    private final WcagIssueRepository wcagIssueRepository;
    private final HeatmapPointRepository heatmapPointRepository;
    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final S3PresignService s3PresignService;
    private final SimulationPoller simulationPoller;

    @Value("${python.endpoint-url}")
    private String pythonEndpointUrl;

    private WebClient webClient;  // @PostConstruct에서 싱글톤으로 초기화

    @jakarta.annotation.PostConstruct
    private void init() {
        this.webClient = webClientBuilder.build();
    }

    // ────────────────────────────────────────
    // POST - 시뮬레이션 생성
    // ────────────────────────────────────────
    @Transactional
    public SimulationCreateResponse createSimulation(UUID userId, SimulationCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. id=" + userId));

        String datePrefix = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        Simulation simulation = Simulation.builder()
                .user(user)
                .title(request.getTitle())
                .targetUrl(request.getTargetUrl())
                .status("pending")
                .datePrefix(datePrefix)
                .createdAt(OffsetDateTime.now())
                .build();

        Simulation saved = simulationRepository.save(simulation);

        SimulationSettings settings = SimulationSettings.builder()
                .project(saved)
                .goal(null)
                .successConditionPath(request.getSuccessCondition() != null ? request.getSuccessCondition().getPath() : null)
                .successConditionParams(request.getSuccessCondition() != null && request.getSuccessCondition().getRequiredParams() != null
                        ? new java.util.HashMap<>(request.getSuccessCondition().getRequiredParams())
                        : null)
                .ageCount10s(request.getAgeCount10())
                .ageCount20s(request.getAgeCount20())
                .ageCount30s(request.getAgeCount30())
                .ageCount40s(request.getAgeCount40())
                .ageCount50s(request.getAgeCount50())
                .ageCount60s(request.getAgeCount60())
                .ageCount70s(request.getAgeCount70())
                .build();

        simulationSettingsRepository.save(settings);

        // EC2 start → Python 호출 → 폴링 → EC2 stop (비동기)
        simulationPoller.startPolling(saved.getProjectId(), buildPythonRequestBody(settings, request));

        return SimulationCreateResponse.builder()
                .projectId(saved.getProjectId())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private Map<String, Object> buildPythonRequestBody(SimulationSettings settings, SimulationCreateRequest request) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("title", request.getTitle());
        requestBody.put("target_url", request.getTargetUrl());
        requestBody.put("task", request.getTask());

        Object requiredParams = settings.getSuccessConditionParams() != null
                ? settings.getSuccessConditionParams()
                : Map.of();
        requestBody.put("success_condition", Map.of(
            "path", settings.getSuccessConditionPath() != null ? settings.getSuccessConditionPath() : "",
            "required_params", requiredParams
        ));

        if (settings.getAgeCount10s() != null) requestBody.put("age_count_10", settings.getAgeCount10s());
        if (settings.getAgeCount20s() != null) requestBody.put("age_count_20", settings.getAgeCount20s());
        if (settings.getAgeCount30s() != null) requestBody.put("age_count_30", settings.getAgeCount30s());
        if (settings.getAgeCount40s() != null) requestBody.put("age_count_40", settings.getAgeCount40s());
        if (settings.getAgeCount50s() != null) requestBody.put("age_count_50", settings.getAgeCount50s());
        if (settings.getAgeCount60s() != null) requestBody.put("age_count_60", settings.getAgeCount60s());
        if (settings.getAgeCount70s() != null) requestBody.put("age_count_70", settings.getAgeCount70s());

        return requestBody;
    }

    // ────────────────────────────────────────
    // GET - 시뮬레이션 진행 상태 조회
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationStatusResponse getStatus(UUID projectId) {
        Simulation simulation = simulationRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("시뮬레이션을 찾을 수 없습니다. id=" + projectId));

        String dbStatus = simulation.getStatus();

        // completed/failed는 DB 값 그대로 반환
        if ("completed".equals(dbStatus) || "failed".equals(dbStatus) || "pending".equals(dbStatus)) {
            return SimulationStatusResponse.builder().status(dbStatus).build();
        }

        // running/analyzing 중에는 Python에 폴링해서 최신 상태 반환
        String jobId = simulation.getJobId();
        if (jobId == null) {
            return SimulationStatusResponse.builder().status(dbStatus).build();
        }

        try {
            Map response = webClient.get()
                .uri(pythonEndpointUrl + "/status/" + jobId)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(java.time.Duration.ofSeconds(5))
                .block();

            if (response == null) return SimulationStatusResponse.builder().status(dbStatus).build();

            return SimulationStatusResponse.builder()
                .status(response.getOrDefault("status", dbStatus).toString())
                .completed(response.get("completed") != null ? ((Number) response.get("completed")).intValue() : null)
                .total(response.get("total") != null ? ((Number) response.get("total")).intValue() : null)
                .failed(response.get("failed") != null ? ((Number) response.get("failed")).intValue() : null)
                .build();
        } catch (Exception e) {
            log.warn("Python /status 폴링 실패: jobId={}", jobId, e);
            return SimulationStatusResponse.builder().status(dbStatus).build();
        }
    }

    // ────────────────────────────────────────
    // GET - 사용자별 시뮬레이션 목록 조회
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SimulationListResponse> getSimulationsByUserId(UUID userId) {
        return simulationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(s -> SimulationListResponse.builder()
                        .projectId(s.getProjectId())
                        .title(s.getTitle())
                        .status(s.getStatus())
                        .createdAt(s.getCreatedAt())
                        .targetUrl(s.getTargetUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────
    // GET - Overview 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationOverviewResponse getOverview(UUID projectId) {
        SimulationOverview overview = simulationOverviewRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Overview 데이터를 찾을 수 없습니다. id=" + projectId));

        List<AgeOverview> ageOverviews = ageOverviewRepository.findByProject_ProjectId(projectId);

        List<SimulationOverviewResponse.AgeOverviewDto> ageOverviewDtos = ageOverviews.stream()
                .map(ao -> SimulationOverviewResponse.AgeOverviewDto.builder()
                        .ageBand(AgeBandConverter.toKorean(ao.getId().getAgeBand()))
                        .totalSessions(ao.getTotalSessions())
                        .successCount(ao.getSuccessCount())
                        .successRate(ao.getSuccessRate())
                        .failRate(ao.getFailRate())
                        .avgDurationMs(ao.getAvgDurationMs())
                        .avgActions(ao.getAvgActions())
                        .avgDeclareFailure(ao.getAvgDeclareFailure())
                        .build())
                .collect(Collectors.toList());

        return SimulationOverviewResponse.builder()
                .totalSessions(overview.getTotalSessions())
                .successCount(overview.getSuccessCount())
                .successRate(overview.getSuccessRate())
                .avgDurationMs(overview.getAvgDurationMs())
                .ageOverview(ageOverviewDtos)
                .build();
    }

    // ────────────────────────────────────────
    // GET - Issues 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationIssuesResponse getIssues(UUID projectId) {
        List<Issue> issues = issueRepository.findByProjectIdOrderByPageAndSeverity(projectId);

        // N+1 방지: project 전체 stats 한 번에 조회 후 issueId로 그룹핑
        List<IssueAgeStats> allStats = issueAgeStatsRepository.findByProjectId(projectId);
        Map<UUID, List<IssueAgeStats>> statsByIssueId = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getId().getIssueId()));

        Map<SimulationPage, List<Issue>> byPage = issues.stream()
                .collect(Collectors.groupingBy(Issue::getPage, LinkedHashMap::new, Collectors.toList()));

        List<SimulationIssuesResponse.IssuePageDto> pages = byPage.entrySet().stream().map(entry -> {
            SimulationPage page = entry.getKey();
            List<Issue> pageIssues = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(i -> i.getSeverity() != null ? i.getSeverity().ordinal() : Integer.MAX_VALUE))
                    .collect(Collectors.toList());

            List<SimulationIssuesResponse.IssueDto> issueDtos = pageIssues.stream().map(issue -> {
                List<IssueAgeStats> ageStats = statsByIssueId.getOrDefault(issue.getId(), List.of());
                int affectedUsersCount = ageStats.stream()
                        .mapToInt(s -> s.getAffectedUsers() != null ? s.getAffectedUsers() : 0).sum();

                return SimulationIssuesResponse.IssueDto.builder()
                        .issueId(issue.getId())
                        .title(issue.getTitle())
                        .category(issue.getCategory())
                        .subCategory(issue.getSubCategory())
                        .severity(issue.getSeverity())
                        .failCount(issue.getFailCount())
                        .affectedUsersCount(affectedUsersCount)
                        .description(issue.getDescription())
                        .targetHtml(issue.getTargetHtml())
                        .tags(issue.getTags() != null ? issue.getTags() : List.of())
                        .build();
            }).collect(Collectors.toList());

            return SimulationIssuesResponse.IssuePageDto.builder()
                    .order(page.getPageOrder() != null ? page.getPageOrder() : 0)
                    .pageUrl(page.getUrl())
                    .screenshotUrl(s3PresignService.presign(page.getScreenshotUrl()))
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
    public SimulationAiFixResponse getAiFix(UUID projectId) {
        List<AiFixSuggestion> fixes = aiFixSuggestionRepository.findByProjectId(projectId);

        Map<SimulationPage, List<AiFixSuggestion>> byPage = fixes.stream()
                .filter(f -> f.getIssue() != null && f.getIssue().getPage() != null)
                .collect(Collectors.groupingBy(f -> f.getIssue().getPage(), LinkedHashMap::new, Collectors.toList()));

        List<SimulationAiFixResponse.AiFixPageDto> pages = byPage.entrySet().stream().map(entry -> {
            SimulationPage page = entry.getKey();
            List<AiFixSuggestion> pageFixes = entry.getValue();

            List<SimulationAiFixResponse.AiFixDto> fixDtos = pageFixes.stream().map(fix ->
                    SimulationAiFixResponse.AiFixDto.builder()
                            .issueId(fix.getIssue().getId())
                            .selector(fix.getSelector())
                            .beforeCode(fix.getBeforeCode())
                            .afterCode(fix.getAfterCode())
                            .impactDescription(fix.getImpactSummary())
                            .changeDescription(fix.getChangeSummaryBody())
                            .build()
            ).collect(Collectors.toList());

            return SimulationAiFixResponse.AiFixPageDto.builder()
                    .order(page.getPageOrder() != null ? page.getPageOrder() : 0)
                    .pageUrl(page.getUrl())
                    .screenshotUrl(s3PresignService.presign(page.getScreenshotUrl()))
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
    public SimulationHeatmapResponse getHeatmap(UUID projectId, String ageGroup, int pageNum, int size) {
        List<SimulationPage> simPages = simulationPageRepository.findByProject_ProjectIdOrderByPageOrder(projectId);

        List<HeatmapPoint> filteredPoints = "all".equals(ageGroup)
                ? heatmapPointRepository.findByProject_ProjectId(projectId)
                : heatmapPointRepository.findByProject_ProjectIdAndAgeBand(
                        projectId, AgeBandConverter.toEnglish(ageGroup));

        Map<UUID, List<HeatmapPoint>> pointsByPageId = filteredPoints.stream()
                .collect(Collectors.groupingBy(p -> p.getPage().getId()));

        List<SimulationHeatmapResponse.HeatmapPageDto> pageDtos = simPages.stream().map(simPage -> {
            List<HeatmapPoint> pagePoints = pointsByPageId.getOrDefault(simPage.getId(), List.of());

            List<SimulationHeatmapResponse.ErrorPointDto> errorPoints = pagePoints.stream().map(p ->
                    SimulationHeatmapResponse.ErrorPointDto.builder()
                            .x(p.getX() != null ? p.getX().doubleValue() : 0.0)
                            .y(p.getY() != null ? p.getY().doubleValue() : 0.0)
                            .count(p.getCount() != null ? p.getCount() : 0)
                            .severity(p.getSeverity())
                            .errorType(p.getErrorType())
                            .ageBand(AgeBandConverter.toKorean(p.getAgeBand()))
                            .issueId(p.getIssue() != null ? p.getIssue().getId() : null)
                            .build()
            ).collect(Collectors.toList());

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
                    .pageUrl(simPage.getUrl())
                    .screenshotUrl(s3PresignService.presign(simPage.getScreenshotUrl()))
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
    public SimulationWcagResponse getWcag(UUID projectId) {
        List<WcagResult> wcagResults = wcagResultRepository.findByProjectId(projectId);
        if (wcagResults.isEmpty()) {
            return SimulationWcagResponse.builder()
                    .score(0)
                    .wcagLabel("미달")
                    .distributionCritical(0)
                    .distributionModerate(0)
                    .distributionMinor(0)
                    .issues(List.of())
                    .build();
        }

        // 점수: 페이지별 점수 평균 (반올림)
        double avgScore = wcagResults.stream()
                .mapToInt(r -> r.getScore() != null ? r.getScore() : 0)
                .average()
                .orElse(0.0);
        int aggregatedScore = (int) Math.round(avgScore);

        // 등급: 평균 점수 기준 (95+ AAA / 70+ AA / 50+ A / <50 미달)
        String aggregatedLabel = aggregatedScore >= 95 ? "AAA"
                : aggregatedScore >= 70 ? "AA"
                : aggregatedScore >= 50 ? "A"
                : "미달";

        int totalCritical = wcagResults.stream()
                .mapToInt(r -> r.getDistributionCritical() != null ? r.getDistributionCritical() : 0).sum();
        int totalModerate = wcagResults.stream()
                .mapToInt(r -> r.getDistributionModerate() != null ? r.getDistributionModerate() : 0).sum();
        int totalMinor = wcagResults.stream()
                .mapToInt(r -> r.getDistributionMinor() != null ? r.getDistributionMinor() : 0).sum();

        // severity 정렬: Critical→Moderate→Minor
        List<WcagIssue> allWcagIssues = wcagIssueRepository.findByProjectId(projectId).stream()
                .sorted(Comparator.comparingInt(i -> i.getSeverity() != null ? i.getSeverity().ordinal() : Integer.MAX_VALUE))
                .collect(Collectors.toList());

        List<SimulationWcagResponse.WcagIssueDto> issueDtos = allWcagIssues.stream().map(issue ->
                SimulationWcagResponse.WcagIssueDto.builder()
                        .wcagIssueId(issue.getId())
                        .title(issue.getTitle())
                        .severity(issue.getSeverity())
                        .description(issue.getDescription())
                        .html(issue.getHtml())
                        .wcagCriteria(issue.getWcagCriteria())
                        .build()
        ).collect(Collectors.toList());

        return SimulationWcagResponse.builder()
                .score(aggregatedScore)
                .wcagLabel(aggregatedLabel)
                .distributionCritical(totalCritical)
                .distributionModerate(totalModerate)
                .distributionMinor(totalMinor)
                .issues(issueDtos)
                .build();
    }
}
