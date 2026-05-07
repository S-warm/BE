package com.swarm.dashboard.service.processor;

import com.swarm.dashboard.domain.fix.AiFixSuggestion;
import com.swarm.dashboard.domain.fix.AiFixSuggestionRepository;
import com.swarm.dashboard.domain.issue.Issue;
import com.swarm.dashboard.domain.issue.IssueRepository;
import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.dto.aicallback.FixesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FixProcessor {

    private final SimulationRepository simRepo;
    private final SimulationPageRepository pageRepo;
    private final IssueRepository issueRepo;
    private final AiFixSuggestionRepository fixRepo;

    public void process(UUID projectId, FixesRequest req) {
        Simulation sim = simRepo.findById(projectId).orElseThrow();

        for (FixesRequest.FixUrlDto urlDto : req.fixes()) {
            SimulationPage page = pageRepo.findByProject_ProjectIdAndUrl(projectId, urlDto.url())
                .orElse(null);
            if (page == null) continue;  // issues가 없는 url일 가능성

            for (FixesRequest.FixItemDto fix : urlDto.fixes()) {
                // error 있는 항목은 skip
                if (fix.error() != null && !fix.error().isEmpty()) continue;

                // (page_id + issue_title)로 issue 조회
                Issue issue = issueRepo.findByPage_IdAndTitle(
                        page.getId(), fix.issueTitle() != null ? fix.issueTitle().trim() : null)
                    .orElse(null);
                if (issue == null) continue;  // 매칭 안 되면 skip

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
}
