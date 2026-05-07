-- ============================================================
-- V8: 스테이징 테이블, WCAG UNIQUE, screenshot_url rename
-- ============================================================

-- 호출 순서 의존성 해결용 스테이징 테이블
CREATE TABLE staging_payload (
    project_id  UUID         NOT NULL REFERENCES simulations(project_id) ON DELETE CASCADE,
    endpoint    VARCHAR(20)  NOT NULL,
    payload     JSONB        NOT NULL,
    received_at TIMESTAMPTZ  DEFAULT now(),
    PRIMARY KEY (project_id, endpoint),
    CONSTRAINT chk_endpoint CHECK (endpoint IN
        ('overview','issues','heatmap','wcag','fixes'))
);

CREATE INDEX idx_staging_received_at ON staging_payload (received_at);

-- WCAG 위반 중복 방지 (같은 wcag_result의 같은 title은 1행만)
ALTER TABLE wcag_issues
  ADD CONSTRAINT uk_wcag_issues UNIQUE (wcag_result_id, title);

-- simulation_pages.screenshot_path → screenshot_url rename + 길이 확장
ALTER TABLE simulation_pages RENAME COLUMN screenshot_path TO screenshot_url;
ALTER TABLE simulation_pages ALTER COLUMN screenshot_url TYPE VARCHAR(1000);

-- wcag_results에 wcag_label 컬럼 추가 (V7에서 누락)
ALTER TABLE wcag_results ADD COLUMN IF NOT EXISTS wcag_label VARCHAR(10);
