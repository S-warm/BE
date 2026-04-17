-- ============================================================
-- V2: page_url 컬럼 추가 + ai_fix_suggestions에 issue_id FK 추가
-- ============================================================

-- [1] simulation_pages: AI 에이전트가 방문한 실제 URL 저장
ALTER TABLE simulation_pages
    ADD COLUMN IF NOT EXISTS page_url VARCHAR(1000);

-- [2] ai_fix_suggestions: Issues 탭 연동 기준 FK 추가
ALTER TABLE ai_fix_suggestions
    ADD COLUMN IF NOT EXISTS issue_id UUID;

ALTER TABLE ai_fix_suggestions
    ADD CONSTRAINT fk_ai_fix_issue
    FOREIGN KEY (issue_id) REFERENCES issues(id)
    ON DELETE SET NULL;

-- [3] issues.severity 기존 값 대문자 통일
--     (프로덕션 데이터가 있을 경우 실행 전 백업 권장)
UPDATE issues SET severity = UPPER(severity) WHERE severity IS NOT NULL;
UPDATE ai_fix_suggestions SET severity = UPPER(severity) WHERE severity IS NOT NULL;
