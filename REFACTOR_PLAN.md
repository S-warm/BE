# 전체 수정 계획 및 이유

---

## A. 연령대 구조 변경

### 문제
현재 입력(`simulation_settings`)은 3개 그룹으로 비율을 받고, 결과(`page_age_stats`, `issue_age_stats`)는 10대~70대 7개로 저장한다.
`age_ratio_teen = 25%`가 10대/20대/30대/40대에 각각 몇 명씩 배분되는지 근거가 없어서, 결과값이 어떻게 산출된 건지 역추적이 불가능하다.

### 왜 이 방향이 맞는가
입력과 출력의 단위가 같아야 데이터 흐름이 명확하다. 결과를 7개 연령대로 저장하려면 입력도 7개로 받아야 1:1 대응이 된다.
또한 비율(%)이 아닌 실제 인원수로 받으면 `personaCount`를 별도로 입력받을 필요 없이 연령대별 합산이 곧 총 페르소나 수가 되므로 입력 구조가 단순해지고 검증 로직도 줄어든다.

### 수정 내용
- `simulation_settings`: `age_ratio_teen`, `age_ratio_fifty`, `age_ratio_eighty` 제거 → `age_count_10` ~ `age_count_70` 7개 추가
- `simulations`: `persona_count` 제거 (연령대별 합산으로 대체)
- `SimulationSettings` Entity: 필드 동일하게 수정
- `SimulationCreateRequest` DTO: 필드 동일하게 수정
- `SimulationService`: 비율 합계 100 검증 제거, personaCount = 연령대 합산으로 교체

---

## B. DB 컬럼 추가

### 문제
API 응답에 `FunnelPanelDto.avgTimeSeconds`(페이지별 평균 체류 시간)와 `ErrorPointDto.description`(히트맵 오류 지점 설명)이 있는데, 이 값을 저장하는 DB 컬럼이 없다.
DB 연동 시 값을 채울 방법이 없다.

### 왜 이 방향이 맞는가
API 응답 구조를 바꾸는 것보다 DB에 컬럼을 추가하는 게 맞다.
`avgTimeSeconds`는 AI 에이전트가 페이지에 머문 시간을 측정한 실제 데이터이고, `description`은 AI가 생성한 오류 설명 텍스트다.
둘 다 시뮬레이션 실행 결과로 저장되어야 할 데이터다.
단위는 ms로 저장해서 Service에서 초로 변환하는 것이 원본 정밀도를 보존하는 방법이다.

### 수정 내용
- `page_age_stats`: `avg_time_ms INT` 추가
- `issue_age_stats`: `description TEXT` 추가
- 각 Entity에 필드 추가

---

## C. DB 설계 정리

### 문제 1 — `ai_fix_suggestions`에 `page_id`와 `issue_id` 둘 다 존재
`issues` 테이블에 이미 `page_id`가 있다. `ai_fix_suggestions`에서 `issue_id`로 JOIN하면 `issues.page_id`로 페이지를 알 수 있는데, 중복으로 `page_id`를 직접 가지고 있다.
두 값이 불일치하는 데이터가 들어올 수 있고, 어느 쪽이 맞는지 알 수 없는 상황이 생긴다.

### 왜 이 방향이 맞는가
정규화 원칙상 같은 정보를 두 곳에 저장하면 안 된다.
`ai_fix_suggestions.page_id`를 제거하고 `issue_id → issues.page_id`로 접근하면 단일 출처(Single Source of Truth)가 유지된다.

### 수정 내용
- `ai_fix_suggestions.page_id` 컬럼 제거
- `AiFixSuggestion` Entity `page` 필드 제거

---

### 문제 2 — `simulation_overview.conversion_rate` 중복
`conversion_rate`는 `success_event_count / tested_agent_count * 100`으로 항상 계산 가능한 값이다.
저장된 `conversion_rate`와 계산값이 다른 상황이 생기면 어느 쪽을 신뢰해야 할지 알 수 없다.

### 왜 이 방향이 맞는가
계산 가능한 값은 저장하지 않는 것이 DB 설계 원칙이다.
Service에서 계산해서 반환하면 항상 정확한 값이 보장된다.

### 수정 내용
- `simulation_overview.conversion_rate` 컬럼 제거
- `SimulationOverview` Entity `conversionRate` 필드 제거

---

## D. DTO 수정

### 문제 1 — `WcagIssueDto.wcagIssueId`가 `Long`
프로젝트 전체 ID 타입이 UUID로 통일되어 있고 `WcagIssue` Entity PK도 UUID인데, DTO만 `Long`으로 선언되어 있다.
DB 연동 시 UUID를 Long으로 변환할 방법이 없어서 매핑 자체가 불가능하다.

### 왜 이 방향이 맞는가
ID 타입은 프로젝트 전체에서 일관성을 유지해야 한다.
Issues 탭의 `issueId`와 Heatmap의 `issueId`가 모두 UUID인데 WCAG만 Long이면 프론트에서도 타입을 다르게 처리해야 하는 불필요한 복잡도가 생긴다.

### 수정 내용
- `SimulationWcagResponse.WcagIssueDto.wcagIssueId` 타입 `Long` → `UUID`
- Mock 데이터 `1L`, `2L` → UUID로 교체

---

### 문제 2 — WCAG Swagger 예시가 실제 DTO 구조와 다름
Swagger 예시에는 `pages` 배열 안에 데이터가 있는 구조로 되어 있는데, 실제 `SimulationWcagResponse`는 `pages` 없이 flat 구조다.
프론트가 Swagger 문서 보고 개발하면 실제 응답과 달라서 연동 오류가 난다.

### 왜 이 방향이 맞는가
Swagger는 실제 API 계약서다. 코드와 문서가 일치해야 프론트와 협업 시 혼란이 없다.

### 수정 내용
- `SimulationController` WCAG 엔드포인트 `@ExampleObject` 내용을 실제 flat 구조에 맞게 수정

---

## E. Entity 수정

### 문제 1 — `Issue.tags`가 `String`으로 JSONB 저장
DB 컬럼은 JSONB 타입인데 Entity에서 `String`으로 받으면, Service에서 매번 `ObjectMapper`로 파싱해야 한다.
파싱 코드가 여러 곳에 흩어지면 유지보수가 어렵고 실수할 여지가 생긴다.

### 왜 이 방향이 맞는가
JPA `AttributeConverter`를 Entity에 한 번만 붙이면 DB ↔ Java 타입 변환이 자동으로 처리된다.
Service에서는 바로 `List<String>`으로 사용할 수 있어 코드가 단순해진다.

### 수정 내용
- `StringListConverter` 클래스 추가
- `Issue.tags` 타입 `String` → `List<String>`, `@Convert(converter = StringListConverter.class)` 추가

---

### 문제 2 — `PageAgeStats`의 `entered`, `passed`에 `@Column(name=)` 없음
현재는 Hibernate 기본 전략으로 우연히 동작하고 있지만, Hibernate 설정이 바뀌거나 다른 개발자가 코드를 볼 때 DB 컬럼명을 명확히 알 수 없다.

### 수정 내용
- `@Column(name = "entered")`, `@Column(name = "passed")` 명시적으로 추가

---

### 문제 3 — Severity가 String으로 관리됨
`"CRITICAL"`, `"Critical"` 등 잘못된 값이 들어와도 컴파일 타임에 잡을 수 없다.
또한 Issues/AiFix 심각도와 WCAG 심각도가 다른 체계인데, 둘 다 `String severity`로 선언되어 있어 구분이 코드상 명확하지 않다.

### 왜 이 방향이 맞는가
Enum으로 선언하면 허용되지 않는 값은 컴파일 에러로 잡히고, 두 심각도 체계가 다르다는 의도도 코드에 명시된다.
WCAG는 국제 표준 용어(`Critical / Moderate / Minor`)를 사용하고, 일반 이슈는 범용 체계(`CRITICAL / HIGH / MEDIUM / LOW`)를 사용하는 것이 각각의 도메인에 맞다.

### 수정 내용
```java
public enum IssueSeverity { CRITICAL, HIGH, MEDIUM, LOW }  // issues, ai_fix_suggestions
public enum WcagSeverity  { Critical, Moderate, Minor }    // wcag_issues
```
- 각 Entity/DTO의 `String severity` → 해당 Enum 타입으로 교체

---

## F. Service 수정 (DB 연동 시)

### 왜 이 방향이 맞는가
DB는 원본 데이터를 정밀하게 보존하고(`BigDecimal`, ms 단위 등), 표현 방식의 변환은 Service 레이어에서 처리하는 것이 계층 분리 원칙에 맞다.
DB 스키마를 API 응답에 맞게 바꾸면 다른 곳에서 DB를 사용할 때 문제가 생길 수 있다.

### 수정 내용

| 항목 | 처리 방법 | 이유 |
|---|---|---|
| `AiFixSuggestion` 필드명 매핑 | `impactedUsers` → `affectedUsersCount` 등 명시적 매핑 | Entity와 DTO 필드명이 다름 |
| `BigDecimal` → `double` | `.doubleValue()` | DB 정밀도 보존, JSON 응답은 double로 충분 |
| `avg_completion_ms` → 초 | `/ 1000` | DB는 원본(ms) 보존, 프론트 표시용으로 변환 |
| `taskSuccessRate` | `success_event_count / tested_agent_count * 100` | 계산 가능한 값은 Service에서 처리 |
| `dropOffAgents` | `tested_agent_count - success_event_count` | 계산 가능한 값은 Service에서 처리 |
| `panelSuccessRate` | `passed / entered * 100` | 계산 가능한 값은 Service에서 처리 |
| `affectedUsersCount` | `SUM(affected_users) GROUP BY issue_id` | issues 테이블에 직접 컬럼 없음 |
| `affectedUsersPercent` | `AVG(affected_percent) GROUP BY issue_id` | issues 테이블에 직접 컬럼 없음 |
| `WcagDistribution` | `COUNT(*) GROUP BY severity` | wcag_results에 분류별 집계 컬럼 없음 |
| `ErrorPointDto.count` | `timeout_count + network_count + console_count` | 직접 count 컬럼 없음 |
| `screenshotPath` → URL | 경로 → URL 변환 로직 추가 | 파일 경로와 공개 URL은 다른 개념 |

---

## G. 운영 전 제거

### 문제
`System.out.println` 디버그 로그가 Controller와 Service에 대량으로 남아있고, userId를 못 찾으면 자동으로 User를 생성하는 코드가 있다.

### 왜 제거해야 하는가
디버그 로그에는 userId, 설정값 등 민감 정보가 포함되어 있어 운영 환경에서 보안 문제가 될 수 있다.
User 자동 생성은 개발 편의용으로 만든 것으로, 운영에서 존재하지 않는 userId로 요청이 들어와도 계정이 생성되는 심각한 보안 취약점이다.

### 수정 내용
- `SimulationController`, `SimulationService`의 `System.out.println` 제거 또는 `log.debug()`로 교체
- `SimulationService.createSimulation()` User 자동 생성 로직 제거, JWT 인증으로 교체

---

## 우선순위 및 이유

| 순위 | 항목 | 이유 |
|---|---|---|
| 1 | 연령대 구조 변경 | Mock 단계에서 안 고치면 DB 연동 후 실제 데이터까지 마이그레이션해야 함 |
| 2 | DB 컬럼 추가 | 없으면 DB 연동 시 해당 API 응답값이 null |
| 3 | DB 설계 정리 | 중복/불일치 데이터가 실제로 쌓이기 전에 정리 |
| 4 | DTO 수정 | 프론트 개발이 잘못된 구조로 진행되기 전에 수정 |
| 5 | Entity 수정 | DB 연동 전에 타입 안전성 확보 |
| 6 | Service | DB 연동 시점에 함께 작업 |
| 7 | 운영 전 제거 | 배포 직전 필수 |
