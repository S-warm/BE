# API 변경 사항 및 연동 가이드

> 백엔드 리팩터링 완료 기준으로 프론트엔드 API 연동 시 참고할 내용 정리

---

## 1. 시뮬레이션 생성 API 변경

### `POST /api/simulations`

#### 변경된 Request Body

기존에 있던 `personaCount`(총 페르소나 수)와 연령대 비율 3개 필드(`ageRatioTeen`, `ageRatioFifty`, `ageRatioEighty`)가 **제거**되었습니다.

대신 **10대~70대 각각의 인원수**를 직접 입력받도록 변경되었습니다.

```json
{
  "title": "Q1 2026 체크아웃 플로우 UX 테스트",
  "targetUrl": "https://shopping-mall.com/checkout",
  "digitalLiteracy": "medium",
  "successCondition": "결제 완료 페이지 도달",
  "personaDevice": "desktop",

  "ageCount10": 50,
  "ageCount20": 150,
  "ageCount30": 200,
  "ageCount40": 200,
  "ageCount50": 150,
  "ageCount60": 100,
  "ageCount70": 50,

  "visionImpairment": 20,
  "attentionLevel": 70
}
```

#### 변경 이유
- 기존 설계는 연령대를 10대~40대 / 50대~70대 / 80대+ 3그룹으로 묶어 **비율(%)** 로 입력받았음
- 그런데 결과(Overview, Heatmap 등)는 **10대/20대/.../70대** 7개로 세분화해서 반환하는 구조였음
- 입력과 출력의 연령대 분류 기준이 달라 AI 에이전트가 연령대별 시뮬레이션을 실행할 수 없는 구조적 문제
- **해결:** 입력도 7개 연령대로 세분화, 인원수를 직접 입력. 총 페르소나 수는 7개 합산으로 자동 계산

#### 프론트 작업 필요 사항
- 기존 `personaCount` 슬라이더 → **연령대별(10대~70대) 개별 슬라이더 7개**로 교체
- 각 슬라이더 옆에 숫자 직접 입력 텍스트 필드 추가
- 상단에 총 페르소나 수(7개 합산) 자동 표시
- `ageCount10~70` 은 현재 **필수값(@NotNull)** 으로, 모두 전송해야 함 (0도 가능)

---

## 2. 각 탭 응답 구조 (변경 없음, 참고용)

### `GET /api/simulations/{simulationId}/overview`

```json
{
  "summary": {
    "taskSuccessRate": 28.0,
    "totalAgents": 1000,
    "avgCompletionSeconds": 252,
    "dropOffAgents": 720
  },
  "funnelPanels": [
    {
      "order": 1,
      "pageName": "랜딩 페이지",
      "pageUrl": "https://...",
      "totalEntered": 1000,
      "totalPassed": 850,
      "panelSuccessRate": 85.0,
      "avgTimeSeconds": 12,
      "agentsByAge": {
        "10대": { "entered": 50, "passed": 48, "dropOff": 2, "successRate": 96.0 },
        "20대": { "entered": 300, "passed": 270, "dropOff": 30, "successRate": 90.0 },
        "30대": { "entered": 250, "passed": 215, "dropOff": 35, "successRate": 86.0 },
        "40대": { "entered": 200, "passed": 160, "dropOff": 40, "successRate": 80.0 },
        "50대": { "entered": 100, "passed": 75, "dropOff": 25, "successRate": 75.0 },
        "60대": { "entered": 70, "passed": 50, "dropOff": 20, "successRate": 71.4 },
        "70대": { "entered": 25, "passed": 10, "dropOff": 15, "successRate": 40.0 },
        "80대": { "entered": 5, "passed": 1, "dropOff": 4, "successRate": 20.0 }
      }
    }
  ]
}
```

> `avgCompletionSeconds` : DB에는 ms로 저장, 서버에서 /1000 변환 후 반환. 프론트 표시: `Math.floor(n/60) + '분' + (n%60) + '초'`

---

### `GET /api/simulations/{simulationId}/issues`

```json
{
  "pages": [
    {
      "order": 1,
      "pageName": "로그인 페이지",
      "pageUrl": "https://...",
      "screenshotUrl": "https://...",
      "totalIssueCount": 3,
      "issues": [
        {
          "issueId": "aaaaaaaa-0000-0000-0000-000000000001",
          "title": "입력 레이블이 낮은 대비율",
          "category": "Accessibility",
          "severity": "HIGH",
          "affectedUsersCount": 142,
          "affectedUsersPercent": 14.2,
          "description": "...",
          "targetHtml": ".form-label",
          "tags": ["contrast", "wcag_aa"]
        }
      ]
    }
  ]
}
```

> `severity` 값: `CRITICAL` / `HIGH` / `MEDIUM` / `LOW`
> `issueId` 타입: **UUID** (String 형태로 반환)

---

### `GET /api/simulations/{simulationId}/ai-fix`

```json
{
  "pages": [
    {
      "order": 1,
      "pageName": "로그인 페이지",
      "pageUrl": "https://...",
      "screenshotUrl": "https://...",
      "totalFixCount": 3,
      "fixes": [
        {
          "issueId": "aaaaaaaa-0000-0000-0000-000000000001",
          "title": "입력 레이블이 낮은 대비율",
          "severity": "HIGH",
          "affectedUsersCount": 142,
          "beforeCode": ".form-label { color: #999999; }",
          "afterCode": ".form-label { color: #334155; }",
          "impactDescription": "142명의 사용자가 레이블을 명확하게 읽을 수 있음",
          "changeDescription": "레이블 색상을 변경하여 대비율 WCAG 기준 충족"
        }
      ]
    }
  ]
}
```

---

### `GET /api/simulations/{simulationId}/heatmap?ageGroup=all&page=0&size=20`

> `ageGroup` 파라미터: `all` / `10대` / `20대` / `30대` / `40대` / `50대` / `60대` / `70대` / `80대`

```json
{
  "pages": [
    {
      "order": 1,
      "pageName": "로그인 페이지",
      "pageUrl": "https://...",
      "screenshotUrl": "https://...",
      "totalErrorCount": 3,
      "currentAgeGroup": "all",
      "errorPoints": [
        {
          "x": 0.72,
          "y": 0.35,
          "count": 18,
          "severity": "CRITICAL",
          "errorType": "Timeout",
          "affectedUsersCount": 12,
          "blockRate": 100.0,
          "repeatCount": 4.5,
          "description": "클릭/스텝 로그에서 Timeout 오류가 집중된 구간입니다.",
          "errorBreakdown": { "timeout": 2, "network": 0, "console": 0 },
          "issueId": "aaaaaaaa-0000-0000-0000-000000000001",
          "ageBand": "all"
        }
      ],
      "pagination": {
        "totalCount": 3,
        "currentPage": 0,
        "pageSize": 20,
        "hasMore": false
      }
    }
  ]
}
```

> `x`, `y`: 0.0~1.0 범위의 비율값. 스크린샷 이미지 위에 좌표 오버레이 시 `x * imageWidth`, `y * imageHeight` 로 변환

---

### `GET /api/simulations/{simulationId}/wcag`

```json
{
  "summary": {
    "complianceScore": 52.0,
    "wcagLabel": "AA",
    "totalTests": 20,
    "passedTests": 9,
    "foundIssues": 14
  },
  "distribution": {
    "critical": 4,
    "moderate": 6,
    "minor": 4
  },
  "issues": [
    {
      "wcagIssueId": "bbbbbbbb-0000-0000-0000-000000000001",
      "title": "텍스트 대비율",
      "severity": "Critical",
      "description": "..."
    }
  ]
}
```

> `severity` 값: `Critical` / `Moderate` / `Minor` (WCAG 표준 용어, 일반 이슈 severity와 다름)
> `wcagIssueId` 타입: **UUID** (String 형태로 반환)

---

## 3. 현재 상태

| 항목 | 상태 |
|---|---|
| 시뮬레이션 생성 (`POST`) | 실제 DB 저장 완료 |
| 각 탭 조회 (`GET`) | 목데이터 반환 중 (DB 데이터 없음) |
| DB 스키마 | V3 Migration 완료 |

각 탭 조회는 DB에 실제 데이터가 들어오면 실제 조회로 전환할 예정입니다.  
**현재는 목데이터를 기준으로 프론트 화면 개발 진행 가능합니다.**

---

## 4. 프론트 작업 필요 사항 요약

| 항목 | 내용 |
|---|---|
| 시뮬레이션 생성 폼 | 연령대 슬라이더 3개 → 7개 (10대~70대), 총 페르소나 수 자동 합산 표시 |
| issueId 타입 | Long → **UUID** (String) |
| wcagIssueId 타입 | Long → **UUID** (String) |
| severity (일반 이슈) | `CRITICAL` / `HIGH` / `MEDIUM` / `LOW` |
| severity (WCAG) | `Critical` / `Moderate` / `Minor` |
| heatmap 좌표 | `x`, `y` 는 0~1 비율값, 이미지 크기 기준으로 변환 필요 |
| avgCompletionSeconds | 이미 초 단위로 반환됨, 프론트에서 분/초 포맷 처리 |
