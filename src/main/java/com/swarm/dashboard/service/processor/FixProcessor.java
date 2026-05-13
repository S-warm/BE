package com.swarm.dashboard.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarm.dashboard.domain.fix.AiFixSuggestion;
import com.swarm.dashboard.domain.fix.AiFixSuggestionRepository;
import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueRepository;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.dto.aicallback.FixesRequest;
import com.swarm.dashboard.util.S3FetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixProcessor {

    private final SimulationRepository simRepo;
    private final SimulationPageRepository pageRepo;
    private final IssueRepository issueRepo;
    private final AiFixSuggestionRepository fixRepo;
    private final S3FetchService s3FetchService;
    private final ObjectMapper objectMapper;

    // fixes/ 폴더 prefix를 받아서 내부 파일 목록 조회 후 처리
    public void process(UUID projectId, String fixesFolderKey) {
        Simulation sim = simRepo.findById(projectId).orElseThrow();

        List<String> keys = s3FetchService.listKeys(fixesFolderKey);

        for (String key : keys) {
            try {
                String json = s3FetchService.fetchJson(key);
                FixesRequest.FixUrlDto urlDto = objectMapper.readValue(json, FixesRequest.FixUrlDto.class);
                processUrlDto(sim, projectId, urlDto);
            } catch (Exception e) {
                log.warn("fix 파일 처리 실패: key={}", key, e);
            }
        }
    }

    private void processUrlDto(Simulation sim, UUID projectId, FixesRequest.FixUrlDto urlDto) {
        SimulationPage page = pageRepo.findByProject_ProjectIdAndUrl(projectId, urlDto.url())
            .orElse(null);
        if (page == null) return;

        for (FixesRequest.FixItemDto fix : urlDto.fixes()) {
            if (fix.error() != null && !fix.error().isEmpty()) continue;

            Issue issue = issueRepo.findByPage_IdAndTitle(
                    page.getId(), fix.issueTitle() != null ? fix.issueTitle().trim() : null)
                .orElse(null);
            if (issue == null) continue;

            AiFixSuggestion suggestion = AiFixSuggestion.builder()
                .project(sim)
                .issue(issue)
                .selector(fix.selector())
                .beforeCode(fix.before())
                .afterCode(fix.after())
                .changeSummaryBody(fix.description())
                .impactSummary(fix.impact())
                .build();
            fixRepo.save(suggestion);
        }
    }
}
