package com.swarm.dashboard.service.processor;

import com.swarm.dashboard.domain.issue.*;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.dto.aicallback.IssuesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueProcessor {

    private final SimulationRepository simRepo;
    private final SimulationPageRepository pageRepo;
    private final IssueRepository issueRepo;
    private final IssueAgeStatsRepository ageStatsRepo;
    private final IssueSessionRepository sessionRepo;

    public Map<String, UUID> process(UUID projectId, IssuesRequest req) {
        Simulation sim = simRepo.findById(projectId).orElseThrow();
        Map<String, UUID> issueIndexMap = new HashMap<>();

        // 1) URL별 그룹핑
        Map<String, List<IssuesRequest.IssueDto>> byUrl = req.issues().stream()
            .collect(Collectors.groupingBy(IssuesRequest.IssueDto::url, LinkedHashMap::new, Collectors.toList()));

        int globalIndex = 0;  // 전체 issues 배열 인덱스 (issue_0, issue_1, ...)

        // 2) URL별 처리
        for (Map.Entry<String, List<IssuesRequest.IssueDto>> entry : byUrl.entrySet()) {
            String url = entry.getKey();
            List<IssuesRequest.IssueDto> urlIssues = entry.getValue();

            // 2-1) simulation_pages upsert (page_order는 다음 순번으로)
            SimulationPage page = pageRepo.findByProject_ProjectIdAndUrl(projectId, url)
                .orElseGet(() -> {
                    Integer nextOrder = pageRepo.findMaxPageOrderByProjectId(projectId) + 1;
                    SimulationPage p = SimulationPage.builder()
                        .project(sim)
                        .url(url)
                        .screenshotUrl(urlIssues.get(0).screenshotUrl())
                        .pageOrder(nextOrder)
                        .build();
                    return pageRepo.save(p);
                });

            // 2-2) 각 issue 저장
            for (IssuesRequest.IssueDto dto : urlIssues) {
                // severity 정규화 (소문자 → 대문자)
                String severityUpper = dto.severity() != null
                    ? dto.severity().toUpperCase()
                    : null;

                Issue issue = Issue.builder()
                    .project(sim)
                    .page(page)
                    .category(dto.category())
                    .subCategory(dto.subCategory())
                    .severity(severityUpper != null ? IssueSeverity.valueOf(severityUpper) : null)
                    .title(dto.title())
                    .description(dto.description())
                    .targetHtml(dto.targetHtml())
                    .tags(dto.tags())
                    .failCount(dto.failCount())
                    .failRate(BigDecimal.valueOf(dto.failRate()))
                    .build();
                issueRepo.save(issue);

                // 2-3) issueIndexMap 등록
                issueIndexMap.put("issue_" + globalIndex, issue.getId());
                globalIndex++;

                // 2-4) affected_personas → age_band별 그룹핑 → IssueAgeStats
                if (dto.affectedPersonas() != null) {
                    Map<String, Long> ageStatsMap = dto.affectedPersonas().stream()
                        .collect(Collectors.groupingBy(
                            IssuesRequest.AffectedPersonaDto::personaAge,
                            Collectors.counting()
                        ));
                    for (Map.Entry<String, Long> as : ageStatsMap.entrySet()) {
                        IssueAgeStatsId stId = new IssueAgeStatsId(issue.getId(), as.getKey());
                        IssueAgeStats stats = IssueAgeStats.builder()
                            .id(stId)
                            .issue(issue)
                            .affectedUsers(as.getValue().intValue())
                            .build();
                        ageStatsRepo.save(stats);
                    }

                    // 2-5) affected_personas 각 항목을 IssueSession에 1:1 저장
                    for (IssuesRequest.AffectedPersonaDto p : dto.affectedPersonas()) {
                        IssueSession sess = IssueSession.builder()
                            .issue(issue)
                            .ageBand(p.personaAge())  // "20s" 그대로 저장
                            .sessionId(p.sessionId())
                            .build();
                        sessionRepo.save(sess);
                    }
                }
            }
        }

        return issueIndexMap;
    }
}
