# API 연동 가이드

> 프론트엔드 API 연동 시 참고 문서
> 테스트용 simulationId: `aaaaaaaa-2222-2222-2222-000000000001`

---

## 공통 사항

- Base URL: `http://localhost:8080`
- 모든 ID 타입: **UUID** (String)
- 인증: 현재 비활성화 상태

---

## 1. 시뮬레이션 생성

### `POST /api/simulations?userId={userId}`

```json
{
  "title": "A몰 로그인 플로우 UX 테스트",
  "targetUrl": "https://shopping-mall.com",
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

**주의사항**
- `ageCount10~70` 전부 필수값, 0 이상 정수
- 총 페르소나 수 = 7개 합산 (프론트에서 자동 계산해서 표시)
- `visionImpairment`, `attentionLevel` 은 선택값 (0~100)
- `digitalLiteracy`: `high` / `medium` / `low`
- `personaDevice`: `desktop` / `mobile` / `tablet`

**Response**
```json
{
  "id": "UUID",
  "title": "A몰 로그인 플로우 UX 테스트",
  "status": "pending",
  "createdAt": "2026-05-02T00:00:00Z"
}
```

---

## 2. 시뮬레이션 목록 조회 (사이드바용)

### `GET /api/simulations?userId={userId}`

```json
[
  {
    "id": "UUID",
    "title": "A몰 로그인 플로우 UX 테스트",
    "status": "completed",
    "createdAt": "2026-05-02T00:00:00Z"
  }
]
```

---

## 3. Overview 탭

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
      "pageName": "로그인 페이지",
      "pageUrl": "https://a-mall.com/login",
      "totalEntered": 1000,
      "totalPassed": 850,
      "panelSuccessRate": 85.0,
      "avgTimeSeconds": 12,
      "agentsByAge": {
        "10대": { "entered": 50, "passed": 48, "dropOff": 2, "successRate": 96.0 },
        "20대": { "entered": 150, "passed": 135, "dropOff": 15, "successRate": 90.0 },
        "30대": { "entered": 200, "passed": 170, "dropOff": 30, "successRate": 85.0 },
        "40대": { "entered": 200, "passed": 160, "dropOff": 40, "successRate": 80.0 },
        "50대": { "entered": 150, "passed": 105, "dropOff": 45, "successRate": 70.0 },
        "60대": { "entered": 100, "passed": 60,  "dropOff": 40, "successRate": 60.0 },
        "70대": { "entered": 50,  "passed": 20,  "dropOff": 30, "successRate": 40.0 }
      }
    }
  ]
}
```

**주의사항**
- `agentsByAge` 키는 항상 7개 고정 (`10대`~`70대`), 데이터 없는 연령대도 `entered: 0` 으로 포함
- `avgCompletionSeconds` 는 초 단위. 프론트 표시: `Math.floor(n/60) + '분' + (n%60) + '초'`

---

## 4. Issues 탭

### `GET /api/simulations/{simulationId}/issues`

```json
{
  "pages": [
    {
      "order": 1,
      "pageName": "로그인 페이지",
      "pageUrl": "https://a-mall.com/login",
      "screenshotUrl": "https://버킷URL/screenshot.png",
      "totalIssueCount": 3,
      "issues": [
        {
          "issueId": "UUID",
          "title": "입력 레이블이 낮은 대비율",
          "category": "Accessibility",
          "severity": "HIGH",
          "affectedUsersCount": 142,
          "affectedUsersPercent": 14.2,
          "description": "흰색 배경 위의 회색 텍스트로 인해 WCAG 2.1 AA 기준(4.5:1) 미달",
          "targetHtml": ".form-label",
          "tags": ["contrast", "wcag_aa"]
        }
      ]
    }
  ]
}
```

**주의사항**
- `severity`: `CRITICAL` / `HIGH` / `MEDIUM` / `LOW` 순으로 정렬
- `issueId`: UUID 타입 (Issues ↔ AI Fix ↔ Heatmap 탭 연동 키)
- `screenshotUrl`: 버킷에 저장된 스크린샷 URL

---

## 5. AI Fix 탭

### `GET /api/simulations/{simulationId}/ai-fix`

```json
{
  "pages": [
    {
      "order": 1,
      "pageName": "로그인 페이지",
      "pageUrl": "https://a-mall.com/login",
      "screenshotUrl": "https://버킷URL/screenshot.png",
      "totalFixCount": 3,
      "fixes": [
        {
          "issueId": "UUID",
          "title": "입력 레이블이 낮은 대비율",
          "severity": "HIGH",
          "affectedUsersCount": 142,
          "beforeCode": ".form-label { color: #999999; }",
          "afterCode": ".form-label { color: #334155; font-weight: 500; }",
          "impactDescription": "142명의 사용자가 이제 레이블을 명확하게 읽을 수 있음",
          "changeDescription": "레이블 색상을 #999999에서 #334155로 변경하여 WCAG 기준 충족"
        }
      ]
    }
  ]
}
```

**주의사항**
- `issueId` 는 Issues 탭의 `issueId` 와 동일 (탭 간 연동 기준 키)

---

## 6. Heatmap 탭

### `GET /api/simulations/{simulationId}/heatmap?ageGroup=all&page=0&size=100`

**파라미터**
| 파라미터 | 허용값 | 기본값 |
|---|---|---|
| `ageGroup` | `all` / `10대` / `20대` / `30대` / `40대` / `50대` / `60대` / `70대` | `all` |
| `page` | 0부터 시작 | `0` |
| `size` | 한 페이지당 오류점 수 | `100` |

```json
{
  "pages": [
    {
      "order": 1,
      "pageName": "로그인 페이지",
      "pageUrl": "https://a-mall.com/login",
      "screenshotUrl": "https://버킷URL/screenshot.png",
      "totalErrorCount": 11,
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
          "issueId": "UUID",
          "ageBand": "all"
        }
      ],
      "pagination": {
        "totalCount": 11,
        "currentPage": 0,
        "pageSize": 100,
        "hasMore": false
      }
    }
  ]
}
```

**주의사항**
- `x`, `y`: 0.0~1.0 비율값 → 오버레이 시 `x * imageWidth`, `y * imageHeight` 로 픽셀 변환
- `severity` 계산 기준: `count` 1~3=`LOW`, 4~7=`MEDIUM`, 8~14=`HIGH`, 15+=`CRITICAL`
- `errorType`: `Timeout` / `Network` / `Console`

---

## 7. WCAG 탭

### `GET /api/simulations/{simulationId}/wcag`

```json
{
  "summary": {
    "complianceScore": 45.0,
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
      "wcagIssueId": "UUID",
      "title": "텍스트 대비율",
      "severity": "Critical",
      "description": "본문 텍스트 대비가 WCAG 2.1 AA 기준 미달"
    }
  ]
}
```

**주의사항**
- `severity`: `Critical` / `Moderate` / `Minor` 순으로 정렬 (일반 이슈 severity와 표기 다름)
- `wcagIssueId`: UUID 타입

---

## 8. 에러 응답 형식

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Overview 데이터를 찾을 수 없습니다.",
  "path": "/api/simulations/UUID/overview"
}
```

| 상태코드 | 상황 |
|---|---|
| `400` | 필수 파라미터 누락, ageGroup 잘못된 값 |
| `404` | simulationId에 해당하는 데이터 없음 |
| `500` | 서버 내부 오류 |

---

## 9. 현재 상태

| 항목 | 상태 |
|---|---|
| 시뮬레이션 생성 (`POST`) | 실제 DB 저장 완료 |
| 각 탭 조회 (`GET`) | 실제 DB 조회 (테스트 데이터 삽입 완료) |
| DB 스키마 | V3 Migration 완료 |
| 연령대 | 10대~70대 7개 고정 |

---

## 10. 탭 간 연동 구조

```
Issues 탭 issueId
    └─ AI Fix 탭 issueId  (동일한 UUID)
    └─ Heatmap 탭 issueId (동일한 UUID)
```

이슈 클릭 시 해당 `issueId` 기준으로 AI Fix / Heatmap 탭과 연동 가능합니다.
