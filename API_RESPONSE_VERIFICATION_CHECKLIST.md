# API Response Verification Checklist (Pre-Integration)

## Scope
- Goal: verify API contracts before FE page-level live wiring.
- Seed source: `src/main/resources/db/seed/S0__api_validation_seed.sql`
- Target simulation id: `11111111-1111-1111-1111-111111111111`

## 1) Build and boot pre-check
1. `./gradlew compileJava`
2. `./gradlew bootRun`
3. open Swagger UI and confirm controller endpoints are listed.

## 2) Seed apply
Use psql or DB tool to run:
```sql
\i src/main/resources/db/seed/S0__api_validation_seed.sql
```

Quick SQL sanity checks:
```sql
SELECT count(*) FROM simulations WHERE id = '11111111-1111-1111-1111-111111111111';
SELECT count(*) FROM simulation_pages WHERE simulation_id = '11111111-1111-1111-1111-111111111111';
SELECT count(*) FROM issues WHERE simulation_id = '11111111-1111-1111-1111-111111111111';
SELECT count(*) FROM wcag_results WHERE simulation_id = '11111111-1111-1111-1111-111111111111';
```

Expected: non-zero rows for all checks.

## 3) Endpoint verification

### A. Overview
- `GET /api/simulations/{id}/overview`
- Verify:
  - `summary.taskSuccessRate`
  - `summary.totalAgents`
  - `summary.avgCompletionSeconds`
  - `summary.dropOffAgents`
  - `funnelPanels[].agentsByAge` has exactly 7 keys:
    - `10대`, `20대`, `30대`, `40대`, `50대`, `60대`, `70대`

### B. Issues
- `GET /api/simulations/{id}/issues`
- Verify:
  - `pages[].order/pageName/pageUrl/screenshotUrl`
  - `issues[].issueId`
  - `issues[].severity` in `CRITICAL|HIGH|MEDIUM|LOW`
  - `issues[].affectedUsersCount`
  - `issues[].affectedUsersPercent`
  - `issues[].targetHtml`
  - `issues[].tags` array

### C. AI Fix
- `GET /api/simulations/{id}/ai-fix`
- Verify:
  - `pages[].fixes[].issueId`
  - `beforeCode`, `afterCode`
  - `impactDescription`, `changeDescription`

### D. Heatmap
- `GET /api/simulations/{id}/heatmap?ageGroup=all&page=0&size=100`
- Verify:
  - `errorPoints[].x/y` in range `0..1`
  - `errorPoints[].errorBreakdown.{timeout,network,console}`
  - `pagination.totalCount/currentPage/pageSize/hasMore`
  - `currentAgeGroup` reflects query

### E. WCAG
- `GET /api/simulations/{id}/wcag`
- Verify:
  - `summary.complianceScore`
  - `summary.wcagLabel` in `A|AA|AAA`
  - `distribution.critical/moderate/minor`
  - `issues[].severity` in `Critical|Moderate|Minor`

## 4) Failure triage
- 500 on any endpoint:
  - check `simulation_id` exists
  - check seed script applied successfully
  - check enum/string values match domain constraints
- Empty arrays:
  - check FK links (`simulation_id`, `page_id`, `issue_id`, `wcag_result_id`)
- Heatmap range issues:
  - verify `coord_x/coord_y` values in DB are normalized.

## 5) Done criteria (before FE live wiring)
- `compileJava` passes.
- All 5 result endpoints return 200 for target simulation id.
- Response keys match FE TypeScript contracts.
- No missing 7-age-band entries in overview payload.
