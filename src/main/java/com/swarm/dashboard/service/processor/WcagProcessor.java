package com.swarm.dashboard.service.processor;

import com.swarm.dashboard.domain.page.SimulationPage;
import com.swarm.dashboard.domain.page.SimulationPageRepository;
import com.swarm.dashboard.domain.simulation.Simulation;
import com.swarm.dashboard.domain.simulation.SimulationRepository;
import com.swarm.dashboard.domain.wcag.WcagResult;
import com.swarm.dashboard.domain.wcag.WcagResultRepository;
import com.swarm.dashboard.dto.aicallback.WcagRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WcagProcessor {

    private final SimulationRepository simRepo;
    private final SimulationPageRepository pageRepo;
    private final WcagResultRepository resultRepo;
    private final EntityManager em;  // native upsert용

    public void process(UUID projectId, WcagRequest req) {
        Simulation sim = simRepo.findById(projectId).orElseThrow();

        for (Map.Entry<String, WcagRequest.WcagUrlResultDto> entry : req.urls().entrySet()) {
            String url = entry.getKey();
            WcagRequest.WcagUrlResultDto dto = entry.getValue();

            // error 있는 URL은 skip
            if (dto.error() != null && !dto.error().isEmpty()) continue;

            // 1) page upsert (page_order는 다음 순번으로)
            SimulationPage page = pageRepo.findByProject_ProjectIdAndUrl(projectId, url)
                .orElseGet(() -> {
                    Integer nextOrder = pageRepo.findMaxPageOrderByProjectId(projectId) + 1;
                    SimulationPage p = SimulationPage.builder()
                        .project(sim)
                        .url(url)
                        .screenshotUrl(dto.screenshotUrl())
                        .pageOrder(nextOrder)
                        .build();
                    return pageRepo.save(p);
                });

            // 2) wcag_results INSERT — 이미 존재하면 skip (재실행 중복 방지)
            if (resultRepo.findByProject_ProjectIdAndPage_Id(projectId, page.getId()).isPresent()) continue;

            WcagRequest.DistributionDto d = dto.distribution();
            WcagResult result = WcagResult.builder()
                .project(sim)
                .page(page)
                .score(dto.score())
                .wcagLabel(dto.wcagLabel())
                .distributionCritical(d.critical())
                .distributionModerate(d.moderate())
                .distributionMinor(d.minor())
                .build();
            resultRepo.save(result);

            // 3) violations[] → wcag_issues UPSERT (PascalCase severity 그대로)
            for (WcagRequest.WcagViolationDto v : dto.violations()) {
                // (wcag_result_id, title) UNIQUE 제약 활용
                em.createNativeQuery("""
                    INSERT INTO wcag_issues
                        (id, wcag_result_id, title, severity, description, html, wcag_criteria)
                    VALUES
                        (gen_random_uuid(), :rid, :title, :sev, :desc, :html, :crit)
                    ON CONFLICT (wcag_result_id, title) DO UPDATE
                    SET severity      = EXCLUDED.severity,
                        description   = EXCLUDED.description,
                        html          = EXCLUDED.html,
                        wcag_criteria = EXCLUDED.wcag_criteria
                    """)
                  .setParameter("rid",   result.getId())
                  .setParameter("title", v.title())
                  .setParameter("sev",   v.severity())
                  .setParameter("desc",  v.description())
                  .setParameter("html",  v.html())
                  .setParameter("crit",  v.wcagCriteria())
                  .executeUpdate();
            }
        }
    }
}
