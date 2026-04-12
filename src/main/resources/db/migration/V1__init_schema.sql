CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================
-- 사용자
-- =============================================
CREATE TABLE users (
                       id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username      VARCHAR(255) UNIQUE NOT NULL,
                       email         VARCHAR(255) UNIQUE,
                       password_hash VARCHAR(255),
                       initials      VARCHAR(10),
                       provider      VARCHAR(20) DEFAULT 'local',
                       created_at    TIMESTAMPTZ DEFAULT now(),
                       updated_at    TIMESTAMPTZ DEFAULT now()
);

-- =============================================
-- 시뮬레이션
-- =============================================
CREATE TABLE simulations (
                             id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id       UUID REFERENCES users(id) ON DELETE CASCADE,
                             title         VARCHAR(255) NOT NULL,
                             target_url    VARCHAR(1000) NOT NULL,
                             persona_count INT DEFAULT 500,
                             status        VARCHAR(20) DEFAULT 'pending',
                             started_at    TIMESTAMPTZ,
                             completed_at  TIMESTAMPTZ,
                             created_at    TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_simulations_user ON simulations(user_id);

-- 시뮬레이션 세팅 (1:1)
CREATE TABLE simulation_settings (
                                     simulation_id     UUID PRIMARY KEY REFERENCES simulations(id) ON DELETE CASCADE,
                                     age_ratio_teen    INT DEFAULT 25,
                                     age_ratio_fifty   INT DEFAULT 25,
                                     age_ratio_eighty  INT DEFAULT 50,
                                     digital_literacy  VARCHAR(20),
                                     vision_impairment INT DEFAULT 0,
                                     attention_level   INT DEFAULT 50,
                                     persona_device    VARCHAR(50),
                                     success_condition TEXT
);

-- =============================================
-- 시뮬레이션 페이지
-- =============================================
CREATE TABLE simulation_pages (
                                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  simulation_id   UUID REFERENCES simulations(id) ON DELETE CASCADE,
                                  page_key        VARCHAR(100) NOT NULL,
                                  page_name       VARCHAR(255) NOT NULL,
                                  screenshot_path VARCHAR(500),
                                  viewport_width  INT DEFAULT 1440,
                                  viewport_height INT DEFAULT 900,
                                  page_order      INT DEFAULT 0,
                                  UNIQUE (simulation_id, page_key)
);

CREATE INDEX idx_simulation_pages_simulation ON simulation_pages(simulation_id);

-- =============================================
-- 개요 탭
-- =============================================
CREATE TABLE simulation_overview (
                                     simulation_id       UUID PRIMARY KEY REFERENCES simulations(id) ON DELETE CASCADE,
                                     conversion_rate     DECIMAL(5,2),
                                     tested_agent_count  INT,
                                     avg_completion_ms   INT,
                                     success_event_count INT,
                                     updated_at          TIMESTAMPTZ DEFAULT now()
);

-- 페이지별 연령대 성공률
CREATE TABLE page_age_stats (
                                id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                page_id      UUID REFERENCES simulation_pages(id) ON DELETE CASCADE,
                                age_band     VARCHAR(10) NOT NULL,
                                success_rate DECIMAL(5,2),
                                entered      INT DEFAULT 0,
                                passed       INT DEFAULT 0,
                                drop_off     INT DEFAULT 0
);

CREATE INDEX idx_page_age_stats_page ON page_age_stats(page_id);

-- =============================================
-- 주요 이슈 탭
-- =============================================
CREATE TABLE issues (
                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        simulation_id UUID REFERENCES simulations(id) ON DELETE CASCADE,
                        page_id       UUID REFERENCES simulation_pages(id) ON DELETE SET NULL,
                        tags          JSONB DEFAULT '[]',
                        category      VARCHAR(50),
                        sub_category  VARCHAR(100),
                        severity      VARCHAR(20),
                        title         VARCHAR(255),
                        description   TEXT,
                        target_html   VARCHAR(500),
                        benefit_label VARCHAR(100),
                        benefit_delta VARCHAR(20),
                        created_at    TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_issues_simulation ON issues(simulation_id);
CREATE INDEX idx_issues_page       ON issues(page_id);

-- 이슈별 연령대 히트맵 마커
CREATE TABLE issue_age_stats (
                                 issue_id         UUID REFERENCES issues(id) ON DELETE CASCADE,
                                 age_band         VARCHAR(10) NOT NULL,
                                 coord_x          DECIMAL(6,4),
                                 coord_y          DECIMAL(6,4),
                                 scroll_y         INT,
                                 affected_users   INT DEFAULT 0,
                                 affected_percent DECIMAL(5,2) DEFAULT 0,
                                 block_rate       DECIMAL(5,2) DEFAULT 0,
                                 repeat_count     DECIMAL(5,2) DEFAULT 0,
                                 error_type       VARCHAR(20),
                                 timeout_count    INT DEFAULT 0,
                                 network_count    INT DEFAULT 0,
                                 console_count    INT DEFAULT 0,
                                 PRIMARY KEY (issue_id, age_band)
);

CREATE INDEX idx_issue_age_stats_issue ON issue_age_stats(issue_id);

-- =============================================
-- WCAG 검사 탭
-- =============================================
CREATE TABLE wcag_results (
                              id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              simulation_id    UUID REFERENCES simulations(id) ON DELETE CASCADE,
                              page_id          UUID REFERENCES simulation_pages(id) ON DELETE CASCADE,
                              compliance_score INT,
                              wcag_label       VARCHAR(50),
                              passed_tests     INT,
                              total_tests      INT,
                              found_issues     INT,
                              created_at       TIMESTAMPTZ DEFAULT now()
);

-- WCAG 세부 이슈
CREATE TABLE wcag_issues (
                             id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             wcag_result_id UUID REFERENCES wcag_results(id) ON DELETE CASCADE,
                             issue_no       INT,
                             title          VARCHAR(255),
                             severity       VARCHAR(20),
                             description    TEXT
);

CREATE INDEX idx_wcag_issues_result ON wcag_issues(wcag_result_id);

-- =============================================
-- AI 수정 탭
-- =============================================
CREATE TABLE ai_fix_suggestions (
                                    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    simulation_id        UUID REFERENCES simulations(id) ON DELETE CASCADE,
                                    page_id              UUID REFERENCES simulation_pages(id) ON DELETE SET NULL,
                                    title                VARCHAR(255),
                                    severity             VARCHAR(20),
                                    before_code          TEXT,
                                    after_code           TEXT,
                                    impact_summary       TEXT,
                                    change_summary_title VARCHAR(255),
                                    change_summary_body  TEXT,
                                    impacted_users       INT DEFAULT 0,
                                    created_at           TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_ai_fix_simulation ON ai_fix_suggestions(simulation_id);
CREATE INDEX idx_ai_fix_page       ON ai_fix_suggestions(page_id);