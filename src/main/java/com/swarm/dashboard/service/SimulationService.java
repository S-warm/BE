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
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id=" + userId));

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

        String paramsJson = null;
        if (request.getSuccessConditionParams() != null) {
            try {
                paramsJson = objectMapper.writeValueAsString(request.getSuccessConditionParams());
            } catch (Exception e) {
                log.warn("successConditionParams 직렬화 실패", e);
            }
        }

        SimulationSettings settings = SimulationSettings.builder()
                .project(saved)
                .goal(request.getGoal())
                .successConditionPath(request.getSuccessConditionPath())
                .successConditionParams(paramsJson)
                .ageCount10s(request.getAgeCount10s())
                .ageCount20s(request.getAgeCount20s())
                .ageCount30s(request.getAgeCount30s())
                .ageCount40s(request.getAgeCount40s())
                .ageCount50s(request.getAgeCount50s())
                .ageCount60s(request.getAgeCount60s())
                .ageCount70s(request.getAgeCount70s())
                .build();

        simulationSettingsRepository.save(settings);

        // Python에 비동기 전송
        sendToPython(saved, settings);

        return SimulationCreateResponse.builder()
                .projectId(saved.getProjectId())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private void sendToPython(Simulation simulation, SimulationSettings settings) {
        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("project_id", simulation.getProjectId().toString());
            requestBody.put("target_url", simulation.getTargetUrl());
            requestBody.put("goal", settings.getGoal());
            Object requiredParams = Map.of();
            if (settings.getSuccessConditionParams() != null) {
                try {
                    requiredParams = objectMapper.readValue(settings.getSuccessConditionParams(), Map.class);
                } catch (Exception e) {
                    log.warn("successConditionParams 역직렬화 실패, 빈 객체로 대체", e);
                }
            }
            requestBody.put("success_condition", Map.of(
                "path", settings.getSuccessConditionPath() != null ? settings.getSuccessConditionPath() : "",
                "required_params", requiredParams
            ));
            requestBody.put("personas", buildPersonasMap(settings));
            requestBody.put("date_prefix", simulation.getDatePrefix());

            webClient
                .post()
                .uri(pythonEndpointUrl)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(java.time.Duration.ofSeconds(30))
                .subscribe(
                    null,
                    err -> {
                        log.error("Python 전송 실패: projectId={}", simulation.getProjectId(), err);
                        simulationRepository.findById(simulation.getProjectId()).ifPresent(s -> {
                            s.setStatus("failed");
                            simulationRepository.save(s);
                        });
                    }
                );
        } catch (Exception e) {
            log.error("Python 전송 준비 실패: projectId={}", simulation.getProjectId(), e);
        }
    }

    private Map<String, Integer> buildPersonasMap(SimulationSettings s) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (s.getAgeCount10s() != null && s.getAgeCount10s() > 0) map.put("10s", s.getAgeCount10s());
        if (s.getAgeCount20s() != null && s.getAgeCount20s() > 0) map.put("20s", s.getAgeCount20s());
        if (s.getAgeCount30s() != null && s.getAgeCount30s() > 0) map.put("30s", s.getAgeCount30s());
        if (s.getAgeCount40s() != null && s.getAgeCount40s() > 0) map.put("40s", s.getAgeCount40s());
        if (s.getAgeCount50s() != null && s.getAgeCount50s() > 0) map.put("50s", s.getAgeCount50s());
        if (s.getAgeCount60s() != null && s.getAgeCount60s() > 0) map.put("60s", s.getAgeCount60s());
        if (s.getAgeCount70s() != null && s.getAgeCount70s() > 0) map.put("70s", s.getAgeCount70s());
        return map;
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
                        .build())
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────
    // GET - Overview 탭
    // ────────────────────────────────────────
    @Transactional(readOnly = true)
    public SimulationOverviewResponse getOverview(UUID projectId) {
        SimulationOverview overview = simulationOverviewRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Overview 데이터를 찾을 수 없습니다. id=" + projectId));

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

        List<HeatmapPoint> allPoints = heatmapPointRepository.findByProject_ProjectId(projectId);

        // 연령대 필터 (영문 코드로 변환)
        List<HeatmapPoint> filteredPoints = "all".equals(ageGroup)
                ? allPoints
                : allPoints.stream()
                    .filter(p -> ageGroup.equals(AgeBandConverter.toKorean(p.getAgeBand())))
                    .collect(Collectors.toList());

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
