-- SWARM API validation seed (pre-real-integration)
-- Purpose:
-- 1) Keep DB seed reproducible/idempotent
-- 2) Guarantee required columns for current JPA mappings
-- 3) Prepare minimum dataset for overview/issues/ai-fix/heatmap/wcag responses

BEGIN;

-- ------------------------------------------------------------------
-- Schema guard: simulation_settings age_count_* columns
-- ------------------------------------------------------------------
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_10 INT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_20 INT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_30 INT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_40 INT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_50 INT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_60 INT;
ALTER TABLE simulation_settings ADD COLUMN IF NOT EXISTS age_count_70 INT;
ALTER TABLE page_age_stats ADD COLUMN IF NOT EXISTS avg_time_ms INT;

-- ------------------------------------------------------------------
-- Fixed IDs
-- ------------------------------------------------------------------
-- user
-- 550e8400-e29b-41d4-a716-446655440000
-- simulation
-- 11111111-1111-1111-1111-111111111111

INSERT INTO users (
    id, username, email, password_hash, initials, provider, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'seed_user',
    'seed-user@swarm.local',
    NULL,
    'SW',
    'system',
    now(),
    now()
) ON CONFLICT (id) DO NOTHING;

INSERT INTO simulations (
    id, user_id, title, target_url, persona_count, status, started_at, completed_at, created_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    '550e8400-e29b-41d4-a716-446655440000',
    'API Validation Simulation',
    'https://demo.a-mall.com',
    1000,
    'completed',
    now() - interval '20 minute',
    now() - interval '10 minute',
    now() - interval '30 minute'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO simulation_settings (
    simulation_id,
    age_ratio_teen, age_ratio_fifty, age_ratio_eighty,
    age_count_10, age_count_20, age_count_30, age_count_40, age_count_50, age_count_60, age_count_70,
    digital_literacy, vision_impairment, attention_level, persona_device, success_condition
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    25, 35, 40,
    60, 220, 220, 180, 150, 110, 60,
    'medium', 20, 70, 'desktop', 'Reach profile page'
) ON CONFLICT (simulation_id) DO NOTHING;

INSERT INTO simulation_overview (
    simulation_id, conversion_rate, tested_agent_count, avg_completion_ms, success_event_count, updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    28.0, 1000, 252000, 280, now()
) ON CONFLICT (simulation_id) DO NOTHING;

INSERT INTO simulation_pages (
    id, simulation_id, page_key, page_name, page_url, screenshot_path, viewport_width, viewport_height, page_order
) VALUES
(
    '20000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'landing',
    'Landing Page',
    'https://demo.a-mall.com',
    'https://storage.example.com/screenshots/sim-1-landing.png',
    1440, 900, 1
),
(
    '20000000-0000-0000-0000-000000000002',
    '11111111-1111-1111-1111-111111111111',
    'login',
    'Login Page',
    'https://demo.a-mall.com/login',
    'https://storage.example.com/screenshots/sim-1-login.png',
    1440, 900, 2
) ON CONFLICT (simulation_id, page_key) DO NOTHING;

INSERT INTO page_age_stats (
    id, page_id, age_band, success_rate, entered, passed, drop_off, avg_time_ms
) VALUES
('30000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','10대',96.0, 60, 58, 2,  9000),
('30000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','20대',90.0,220,198,22, 11000),
('30000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','30대',88.0,220,194,26, 12000),
('30000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','40대',82.0,180,148,32, 13000),
('30000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','50대',77.0,150,116,34, 14500),
('30000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','60대',69.0,110, 76,34, 15800),
('30000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','70대',56.0, 60, 34,26, 17000),

('30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000002','10대',93.0, 58, 54, 4, 10000),
('30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000002','20대',86.0,198,170,28, 12500),
('30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000002','30대',81.0,194,157,37, 13500),
('30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000002','40대',72.0,148,107,41, 14500),
('30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000002','50대',64.0,116, 74,42, 16000),
('30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000002','60대',54.0, 76, 41,35, 17800),
('30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000002','70대',38.0, 34, 13,21, 19000)
ON CONFLICT (page_id, age_band) DO NOTHING;

INSERT INTO issues (
    id, simulation_id, page_id, tags, category, sub_category, severity, title, description, target_html, benefit_label, benefit_delta, created_at
) VALUES
(
    '40000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    '20000000-0000-0000-0000-000000000002',
    '["contrast","wcag_aa"]'::jsonb,
    'Accessibility',
    'Contrast',
    'HIGH',
    'Form label contrast is too low',
    'Label contrast ratio does not meet WCAG AA threshold.',
    '.form-label',
    'Contrast improvement',
    '+12%',
    now() - interval '8 minute'
),
(
    '40000000-0000-0000-0000-000000000002',
    '11111111-1111-1111-1111-111111111111',
    '20000000-0000-0000-0000-000000000002',
    '["keyboard","focus"]'::jsonb,
    'Usability',
    'Focus',
    'MEDIUM',
    'Submit button is hard to reach by keyboard',
    'Focus order creates extra keyboard travel.',
    'button[type=submit]',
    'Keyboard UX',
    '+7%',
    now() - interval '7 minute'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO issue_age_stats (
    issue_id, age_band, coord_x, coord_y, scroll_y, affected_users, affected_percent, block_rate, repeat_count, error_type, timeout_count, network_count, console_count
) VALUES
('40000000-0000-0000-0000-000000000001','10대',0.42,0.31,120,  8,13.3,22.0,2.1,'Console',0,0,3),
('40000000-0000-0000-0000-000000000001','20대',0.42,0.31,120, 25,11.4,24.0,2.3,'Console',0,0,4),
('40000000-0000-0000-0000-000000000001','30대',0.42,0.31,120, 28,12.7,25.0,2.4,'Console',1,0,4),
('40000000-0000-0000-0000-000000000001','40대',0.42,0.31,120, 30,16.7,31.0,2.8,'Console',1,0,4),
('40000000-0000-0000-0000-000000000001','50대',0.42,0.31,120, 26,17.3,35.0,3.1,'Console',1,0,4),
('40000000-0000-0000-0000-000000000001','60대',0.42,0.31,120, 15,13.6,40.0,3.6,'Console',1,0,4),
('40000000-0000-0000-0000-000000000001','70대',0.42,0.31,120, 10,16.7,48.0,4.0,'Console',1,0,4),

('40000000-0000-0000-0000-000000000002','10대',0.67,0.78,680,  7,11.7,30.0,2.5,'Timeout',1,0,0),
('40000000-0000-0000-0000-000000000002','20대',0.67,0.78,680, 20, 9.1,34.0,2.7,'Timeout',2,0,0),
('40000000-0000-0000-0000-000000000002','30대',0.67,0.78,680, 22,10.0,36.0,2.9,'Timeout',2,0,0),
('40000000-0000-0000-0000-000000000002','40대',0.67,0.78,680, 24,13.3,43.0,3.2,'Timeout',3,0,0),
('40000000-0000-0000-0000-000000000002','50대',0.67,0.78,680, 20,13.3,49.0,3.6,'Timeout',3,0,0),
('40000000-0000-0000-0000-000000000002','60대',0.67,0.78,680, 13,11.8,54.0,4.1,'Timeout',4,0,0),
('40000000-0000-0000-0000-000000000002','70대',0.67,0.78,680,  9,15.0,63.0,4.8,'Timeout',4,0,0)
ON CONFLICT (issue_id, age_band) DO NOTHING;

INSERT INTO ai_fix_suggestions (
    id, simulation_id, page_id, issue_id, title, severity, before_code, after_code,
    impact_summary, change_summary_title, change_summary_body, impacted_users, created_at
) VALUES
(
    '60000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    '20000000-0000-0000-0000-000000000002',
    '40000000-0000-0000-0000-000000000001',
    'Improve label contrast',
    'HIGH',
    '.form-label { color: #9CA3AF; }',
    '.form-label { color: #334155; font-weight: 500; }',
    'Improves readability for low-vision users.',
    'Contrast adjustment',
    'Updated color and weight to meet AA-level contrast guidance.',
    142,
    now() - interval '5 minute'
),
(
    '60000000-0000-0000-0000-000000000002',
    '11111111-1111-1111-1111-111111111111',
    '20000000-0000-0000-0000-000000000002',
    '40000000-0000-0000-0000-000000000002',
    'Fix keyboard submit access path',
    'MEDIUM',
    '<button type="submit" tabindex="-1">Submit</button>',
    '<button type="submit">Submit</button>',
    'Reduces keyboard navigation friction.',
    'Keyboard accessibility',
    'Removed invalid tabindex and normalized focus order.',
    115,
    now() - interval '4 minute'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO wcag_results (
    id, simulation_id, page_id, compliance_score, wcag_label, passed_tests, total_tests, found_issues, created_at
) VALUES
(
    '50000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    '20000000-0000-0000-0000-000000000002',
    52,
    'AA',
    9,
    20,
    6,
    now() - interval '3 minute'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO wcag_issues (
    id, wcag_result_id, issue_no, title, severity, description
) VALUES
(
    '51000000-0000-0000-0000-000000000001',
    '50000000-0000-0000-0000-000000000001',
    1,
    'Text contrast below threshold',
    'Critical',
    'Body and helper text contrast does not satisfy WCAG AA.'
),
(
    '51000000-0000-0000-0000-000000000002',
    '50000000-0000-0000-0000-000000000001',
    2,
    'Missing accessible labels',
    'Moderate',
    'Some inputs lack programmatic labels for assistive technologies.'
),
(
    '51000000-0000-0000-0000-000000000003',
    '50000000-0000-0000-0000-000000000001',
    3,
    'Decorative image alt text cleanup',
    'Minor',
    'Decorative images should use empty alt or presentation role.'
) ON CONFLICT (id) DO NOTHING;

COMMIT;
