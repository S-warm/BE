-- ============================================================
-- V7: 전체 DB 스펙 재설계 (idempotent — 부분 적용 상태에서도 안전)
-- ============================================================

-- ============================================================
-- [1] simulations: id → project_id rename, date_prefix 추가
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulations' AND column_name='id') THEN
        ALTER TABLE simulations RENAME COLUMN id TO project_id;
    END IF;
END $$;
ALTER TABLE simulations ADD COLUMN IF NOT EXISTS date_prefix VARCHAR(30);

-- ============================================================
-- [2] simulation_settings: project_id rename + 컬럼 추가/제거/rename
-- ============================================================
-- simulation_id → project_id
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_settings' AND column_name='simulation_id')
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                    WHERE table_schema='public' AND table_name='simulation_settings' AND column_name='project_id') THEN
        ALTER TABLE simulation_settings RENAME COLUMN simulation_id TO project_id;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_schema='public' AND table_name='simulation_settings' AND column_name='simulation_id') THEN
        ALTER TABLE simulation_settings DROP CONSTRAINT IF EXISTS simulation_settings_simulation_id_fkey;
        ALTER TABLE simulation_settings DROP COLUMN simulation_id;
    END IF;
END $$;

ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS goal TEXT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS success_condition_path VARCHAR(500);
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS success_condition_params JSONB;

ALTER TABLE simulation_settings DROP COLUMN IF EXISTS digital_literacy;
ALTER TABLE simulation_settings DROP COLUMN IF EXISTS vision_impairment;
ALTER TABLE simulation_settings DROP COLUMN IF EXISTS attention_level;
ALTER TABLE simulation_settings DROP COLUMN IF EXISTS persona_device;
ALTER TABLE simulation_settings DROP COLUMN IF EXISTS success_condition;

-- age_count 컬럼 rename (old → new, 이미 양쪽 존재하면 old 삭제)
DO $$
DECLARE
    bands TEXT[] := ARRAY['10','20','30','40','50','60','70'];
    b TEXT;
BEGIN
    FOREACH b IN ARRAY bands LOOP
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='simulation_settings'
                   AND column_name='age_count_' || b)
        AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                        WHERE table_schema='public' AND table_name='simulation_settings'
                        AND column_name='age_count_' || b || 's') THEN
            EXECUTE 'ALTER TABLE simulation_settings RENAME COLUMN age_count_' || b || ' TO age_count_' || b || 's';
        ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                      WHERE table_schema='public' AND table_name='simulation_settings'
                      AND column_name='age_count_' || b)
        AND EXISTS (SELECT 1 FROM information_schema.columns
                    WHERE table_schema='public' AND table_name='simulation_settings'
                    AND column_name='age_count_' || b || 's') THEN
            EXECUTE 'ALTER TABLE simulation_settings DROP COLUMN age_count_' || b;
        END IF;
    END LOOP;
END $$;

-- ============================================================
-- [3] simulation_overview: 컬럼명 변경 + success_rate 추가
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_overview' AND column_name='simulation_id') THEN
        ALTER TABLE simulation_overview RENAME COLUMN simulation_id TO project_id;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_overview' AND column_name='tested_agent_count') THEN
        ALTER TABLE simulation_overview RENAME COLUMN tested_agent_count TO total_sessions;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_overview' AND column_name='success_event_count') THEN
        ALTER TABLE simulation_overview RENAME COLUMN success_event_count TO success_count;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_overview' AND column_name='avg_completion_ms') THEN
        ALTER TABLE simulation_overview RENAME COLUMN avg_completion_ms TO avg_duration_ms;
    END IF;
END $$;
ALTER TABLE simulation_overview DROP COLUMN IF EXISTS updated_at;
ALTER TABLE simulation_overview ADD COLUMN IF NOT EXISTS success_rate DECIMAL(8,4);

-- ============================================================
-- [4] simulation_pages: 컬럼 정리 + UNIQUE 재설정
-- ============================================================
ALTER TABLE simulation_pages DROP CONSTRAINT IF EXISTS simulation_pages_simulation_id_page_key_key;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_pages' AND column_name='simulation_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='simulation_pages' AND column_name='project_id') THEN
        ALTER TABLE simulation_pages DROP CONSTRAINT IF EXISTS simulation_pages_simulation_id_fkey;
        ALTER TABLE simulation_pages DROP COLUMN simulation_id;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_schema='public' AND table_name='simulation_pages' AND column_name='simulation_id') THEN
        ALTER TABLE simulation_pages RENAME COLUMN simulation_id TO project_id;
    END IF;
END $$;

ALTER TABLE simulation_pages DROP COLUMN IF EXISTS page_key;
ALTER TABLE simulation_pages DROP COLUMN IF EXISTS page_name;
ALTER TABLE simulation_pages DROP COLUMN IF EXISTS viewport_width;
ALTER TABLE simulation_pages DROP COLUMN IF EXISTS viewport_height;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='simulation_pages' AND column_name='page_url') THEN
        ALTER TABLE simulation_pages RENAME COLUMN page_url TO url;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'simulation_pages_project_url_unique') THEN
        ALTER TABLE simulation_pages ADD CONSTRAINT simulation_pages_project_url_unique UNIQUE (project_id, url);
    END IF;
END $$;

-- ============================================================
-- [5] issues: 컬럼 정리 + fail_count/fail_rate 추가
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='issues' AND column_name='simulation_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='issues' AND column_name='project_id') THEN
        ALTER TABLE issues DROP CONSTRAINT IF EXISTS issues_simulation_id_fkey;
        ALTER TABLE issues DROP COLUMN simulation_id;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_schema='public' AND table_name='issues' AND column_name='simulation_id') THEN
        ALTER TABLE issues RENAME COLUMN simulation_id TO project_id;
    END IF;
END $$;

ALTER TABLE issues DROP COLUMN IF EXISTS benefit_label;
ALTER TABLE issues DROP COLUMN IF EXISTS benefit_delta;
ALTER TABLE issues DROP COLUMN IF EXISTS created_at;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS fail_count INT DEFAULT 0;
ALTER TABLE issues ADD COLUMN IF NOT EXISTS fail_rate DECIMAL(8,4) DEFAULT 0;

-- ============================================================
-- [6] issue_age_stats: 불필요 컬럼 제거
-- ============================================================
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS coord_x;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS coord_y;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS scroll_y;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS affected_percent;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS block_rate;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS repeat_count;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS error_type;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS timeout_count;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS network_count;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS console_count;
ALTER TABLE issue_age_stats DROP COLUMN IF EXISTS description;

-- ============================================================
-- [7] ai_fix_suggestions: 컬럼 정리 + selector 추가
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='ai_fix_suggestions' AND column_name='simulation_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='ai_fix_suggestions' AND column_name='project_id') THEN
        ALTER TABLE ai_fix_suggestions DROP CONSTRAINT IF EXISTS ai_fix_suggestions_simulation_id_fkey;
        ALTER TABLE ai_fix_suggestions DROP COLUMN simulation_id;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_schema='public' AND table_name='ai_fix_suggestions' AND column_name='simulation_id') THEN
        ALTER TABLE ai_fix_suggestions RENAME COLUMN simulation_id TO project_id;
    END IF;
END $$;

ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS title;
ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS severity;
ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS impacted_users;
ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS change_summary_title;
ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS created_at;
ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS effort_level;
ALTER TABLE ai_fix_suggestions DROP COLUMN IF EXISTS wcag_criterion;
ALTER TABLE ai_fix_suggestions ADD COLUMN IF NOT EXISTS selector VARCHAR(500);

-- ============================================================
-- [8] wcag_results: 컬럼 정리 + score/distribution 추가
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='wcag_results' AND column_name='simulation_id')
    AND EXISTS (SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name='wcag_results' AND column_name='project_id') THEN
        ALTER TABLE wcag_results DROP CONSTRAINT IF EXISTS wcag_results_simulation_id_fkey;
        ALTER TABLE wcag_results DROP COLUMN simulation_id;
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_schema='public' AND table_name='wcag_results' AND column_name='simulation_id') THEN
        ALTER TABLE wcag_results RENAME COLUMN simulation_id TO project_id;
    END IF;
END $$;

ALTER TABLE wcag_results DROP COLUMN IF EXISTS compliance_score;
ALTER TABLE wcag_results DROP COLUMN IF EXISTS passed_tests;
ALTER TABLE wcag_results DROP COLUMN IF EXISTS total_tests;
ALTER TABLE wcag_results DROP COLUMN IF EXISTS found_issues;
ALTER TABLE wcag_results DROP COLUMN IF EXISTS created_at;
ALTER TABLE wcag_results DROP COLUMN IF EXISTS tested_at;
ALTER TABLE wcag_results DROP COLUMN IF EXISTS wcag_version;
ALTER TABLE wcag_results ADD COLUMN IF NOT EXISTS score INT DEFAULT 0;
ALTER TABLE wcag_results ADD COLUMN IF NOT EXISTS distribution_critical INT DEFAULT 0;
ALTER TABLE wcag_results ADD COLUMN IF NOT EXISTS distribution_moderate INT DEFAULT 0;
ALTER TABLE wcag_results ADD COLUMN IF NOT EXISTS distribution_minor INT DEFAULT 0;

-- ============================================================
-- [9] wcag_issues: issue_no 제거 + html/wcag_criteria 추가
-- ============================================================
ALTER TABLE wcag_issues DROP COLUMN IF EXISTS issue_no;
ALTER TABLE wcag_issues DROP COLUMN IF EXISTS criterion;
ALTER TABLE wcag_issues DROP COLUMN IF EXISTS element_selector;
ALTER TABLE wcag_issues DROP COLUMN IF EXISTS page_url;
ALTER TABLE wcag_issues ADD COLUMN IF NOT EXISTS html TEXT;
ALTER TABLE wcag_issues ADD COLUMN IF NOT EXISTS wcag_criteria VARCHAR(20);

-- ============================================================
-- [10] page_age_stats 삭제
-- ============================================================
DROP TABLE IF EXISTS page_age_stats;

-- ============================================================
-- [11] age_overview: 기존 잘못된 스키마 → 새 스키마로 재생성
-- ============================================================
DROP TABLE IF EXISTS age_overview;
CREATE TABLE age_overview (
    project_id          UUID         NOT NULL REFERENCES simulations(project_id) ON DELETE CASCADE,
    age_band            VARCHAR(10)  NOT NULL,
    total_sessions      INT          DEFAULT 0,
    success_count       INT          DEFAULT 0,
    success_rate        DECIMAL(8,4) DEFAULT 0,
    fail_rate           DECIMAL(8,4) DEFAULT 0,
    avg_duration_ms     BIGINT       DEFAULT 0,
    avg_actions         DECIMAL(6,2) DEFAULT 0,
    avg_declare_failure DECIMAL(6,2) DEFAULT 0,
    PRIMARY KEY (project_id, age_band)
);

-- ============================================================
-- [12] issue_sessions: 기존 잘못된 스키마 → 새 스키마로 재생성
-- ============================================================
DROP TABLE IF EXISTS issue_sessions;
CREATE TABLE issue_sessions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    issue_id   UUID         NOT NULL REFERENCES issues(id) ON DELETE CASCADE,
    age_band   VARCHAR(10)  NOT NULL,
    session_id VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_issue_sessions_issue ON issue_sessions(issue_id);

-- ============================================================
-- [13] heatmap_points: 기존 잘못된 스키마 → 새 스키마로 재생성
-- ============================================================
DROP TABLE IF EXISTS heatmap_points;
CREATE TABLE heatmap_points (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID         NOT NULL REFERENCES simulations(project_id) ON DELETE CASCADE,
    page_id    UUID         NOT NULL REFERENCES simulation_pages(id) ON DELETE CASCADE,
    issue_id   UUID                  REFERENCES issues(id) ON DELETE SET NULL,
    x          DECIMAL(6,4) NOT NULL,
    y          DECIMAL(6,4) NOT NULL,
    age_band   VARCHAR(10)  NOT NULL,
    count      INT          DEFAULT 0,
    severity   VARCHAR(20),
    error_type VARCHAR(200)
);
CREATE INDEX IF NOT EXISTS idx_heatmap_points_project ON heatmap_points(project_id);
CREATE INDEX IF NOT EXISTS idx_heatmap_points_page    ON heatmap_points(page_id);
