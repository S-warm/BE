-- Single-cycle deterministic seed for pre-live integration checks
-- Goal: keep exactly 1 user + 1 simulation and enough child data
-- to validate Overview / Issues / AI Fix / Heatmap / WCAG endpoints.

BEGIN;

-- ------------------------------------------------------------
-- 0) Clean existing data (child -> parent order)
-- ------------------------------------------------------------
DELETE FROM wcag_issues;
DELETE FROM wcag_results;
DELETE FROM ai_fix_suggestions;
DELETE FROM issue_age_stats;
DELETE FROM issues;
DELETE FROM page_age_stats;
DELETE FROM simulation_pages;
DELETE FROM simulation_overview;
DELETE FROM simulation_settings;
DELETE FROM simulations;
DELETE FROM users;

-- ------------------------------------------------------------
-- 1) Fixed IDs
-- ------------------------------------------------------------
-- user:       550e8400-e29b-41d4-a716-446655440000
-- simulation: 11111111-1111-1111-1111-111111111111
-- page(1):    20000000-0000-0000-0000-000000000001
-- page(2):    20000000-0000-0000-0000-000000000002
-- issue(1):   40000000-0000-0000-0000-000000000001
-- issue(2):   40000000-0000-0000-0000-000000000002

-- ------------------------------------------------------------
-- 2) User + Simulation root
-- ------------------------------------------------------------
INSERT INTO users (
  id, username, email, password_hash, initials, provider, created_at, updated_at
) VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  'seed_user',
  'test@swarm.com',
  NULL,
  'SW',
  'local',
  now(),
  now()
);

INSERT INTO simulations (
  id, user_id, title, target_url, status, started_at, completed_at, created_at
) VALUES (
  '11111111-1111-1111-1111-111111111111',
  '550e8400-e29b-41d4-a716-446655440000',
  'A-Mall login flow UX test',
  'https://demo.a-mall.com/login',
  'completed',
  now() - interval '30 minute',
  now() - interval '10 minute',
  now() - interval '35 minute'
);

INSERT INTO simulation_settings (
  simulation_id, digital_literacy, vision_impairment, attention_level, persona_device, success_condition,
  age_count_10, age_count_20, age_count_30, age_count_40, age_count_50, age_count_60, age_count_70
) VALUES (
  '11111111-1111-1111-1111-111111111111',
  'medium', 20, 70, 'desktop', 'Reach profile page after login',
  60, 220, 220, 180, 150, 110, 60
);

INSERT INTO simulation_overview (
  simulation_id, tested_agent_count, avg_completion_ms, success_event_count, updated_at
) VALUES (
  '11111111-1111-1111-1111-111111111111',
  1000, 252000, 280, now()
);

-- ------------------------------------------------------------
-- 3) Pages + age stats
-- ------------------------------------------------------------
INSERT INTO simulation_pages (
  id, simulation_id, page_key, page_name, page_url, screenshot_path, viewport_width, viewport_height, page_order
) VALUES
(
  '20000000-0000-0000-0000-000000000001',
  '11111111-1111-1111-1111-111111111111',
  'landing',
  'Landing Page',
  'https://demo.a-mall.com',
  'https://storage.example.com/screenshots/sim1-landing.png',
  1440, 900, 1
),
(
  '20000000-0000-0000-0000-000000000002',
  '11111111-1111-1111-1111-111111111111',
  'login',
  'Login Page',
  'https://demo.a-mall.com/login',
  'https://storage.example.com/screenshots/sim1-login.png',
  1440, 900, 2
);

INSERT INTO page_age_stats (id, page_id, age_band, success_rate, entered, passed, drop_off, avg_time_ms) VALUES
('30000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','10대',96.0, 60, 58,  2,  9000),
('30000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','20대',90.0,220,198, 22, 11000),
('30000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000001','30대',88.0,220,194, 26, 12000),
('30000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000001','40대',82.0,180,148, 32, 13000),
('30000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000001','50대',77.0,150,116, 34, 14500),
('30000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000001','60대',69.0,110, 76, 34, 15800),
('30000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000001','70대',56.0, 60, 34, 26, 17000),
('30000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000002','10대',93.0, 58, 54,  4, 10000),
('30000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000002','20대',86.0,198,170, 28, 12500),
('30000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000002','30대',81.0,194,157, 37, 13500),
('30000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000002','40대',72.0,148,107, 41, 14500),
('30000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000002','50대',64.0,116, 74, 42, 16000),
('30000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000002','60대',54.0, 76, 41, 35, 17800),
('30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000002','70대',38.0, 34, 13, 21, 19000);

-- ------------------------------------------------------------
-- 4) Issues + issue age stats (heatmap source)
-- ------------------------------------------------------------
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
);

INSERT INTO issue_age_stats (
  issue_id, age_band, coord_x, coord_y, scroll_y, affected_users, affected_percent, block_rate, repeat_count,
  error_type, timeout_count, network_count, console_count, description
) VALUES
('40000000-0000-0000-0000-000000000001','10대',0.42,0.31,120,  8,13.3,22.0,2.1,'Console',0,0,3,'Low contrast near label'),
('40000000-0000-0000-0000-000000000001','20대',0.42,0.31,120, 25,11.4,24.0,2.3,'Console',0,0,4,'Low contrast near label'),
('40000000-0000-0000-0000-000000000001','30대',0.42,0.31,120, 28,12.7,25.0,2.4,'Console',1,0,4,'Low contrast near label'),
('40000000-0000-0000-0000-000000000001','40대',0.42,0.31,120, 30,16.7,31.0,2.8,'Console',1,0,4,'Low contrast near label'),
('40000000-0000-0000-0000-000000000001','50대',0.42,0.31,120, 26,17.3,35.0,3.1,'Console',1,0,4,'Low contrast near label'),
('40000000-0000-0000-0000-000000000001','60대',0.42,0.31,120, 15,13.6,40.0,3.6,'Console',1,0,4,'Low contrast near label'),
('40000000-0000-0000-0000-000000000001','70대',0.42,0.31,120, 10,16.7,48.0,4.0,'Console',1,0,4,'Low contrast near label'),
('40000000-0000-0000-0000-000000000002','10대',0.67,0.78,680,  7,11.7,30.0,2.5,'Timeout',1,0,0,'Keyboard focus travel'),
('40000000-0000-0000-0000-000000000002','20대',0.67,0.78,680, 20, 9.1,34.0,2.7,'Timeout',2,0,0,'Keyboard focus travel'),
('40000000-0000-0000-0000-000000000002','30대',0.67,0.78,680, 22,10.0,36.0,2.9,'Timeout',2,0,0,'Keyboard focus travel'),
('40000000-0000-0000-0000-000000000002','40대',0.67,0.78,680, 24,13.3,43.0,3.2,'Timeout',3,0,0,'Keyboard focus travel'),
('40000000-0000-0000-0000-000000000002','50대',0.67,0.78,680, 20,13.3,49.0,3.6,'Timeout',3,0,0,'Keyboard focus travel'),
('40000000-0000-0000-0000-000000000002','60대',0.67,0.78,680, 13,11.8,54.0,4.1,'Timeout',4,0,0,'Keyboard focus travel'),
('40000000-0000-0000-0000-000000000002','70대',0.67,0.78,680,  9,15.0,63.0,4.8,'Timeout',4,0,0,'Keyboard focus travel');

-- ------------------------------------------------------------
-- 5) AI fix
-- ------------------------------------------------------------
INSERT INTO ai_fix_suggestions (
  id, simulation_id, title, severity, before_code, after_code, impact_summary, change_summary_title, change_summary_body,
  impacted_users, created_at, issue_id
) VALUES
(
  '60000000-0000-0000-0000-000000000001',
  '11111111-1111-1111-1111-111111111111',
  'Improve label contrast',
  'HIGH',
  '.form-label { color: #9CA3AF; }',
  '.form-label { color: #334155; font-weight: 500; }',
  'Improves readability for low-vision users.',
  'Contrast adjustment',
  'Updated color and weight to meet AA-level contrast guidance.',
  142,
  now() - interval '5 minute',
  '40000000-0000-0000-0000-000000000001'
),
(
  '60000000-0000-0000-0000-000000000002',
  '11111111-1111-1111-1111-111111111111',
  'Fix keyboard submit access path',
  'MEDIUM',
  '<button type=\"submit\" tabindex=\"-1\">Submit</button>',
  '<button type=\"submit\">Submit</button>',
  'Reduces keyboard navigation friction.',
  'Keyboard accessibility',
  'Removed invalid tabindex and normalized focus order.',
  115,
  now() - interval '4 minute',
  '40000000-0000-0000-0000-000000000002'
);

-- ------------------------------------------------------------
-- 6) WCAG
-- ------------------------------------------------------------
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
);

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
);

COMMIT;
