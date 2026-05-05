-- Deterministic single-cycle seed aligned with FE defaults.
-- Keeps one known completed simulation available at
-- 11111111-1111-1111-1111-111111111111 without deleting user-created rows.

BEGIN;

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
)
ON CONFLICT (id) DO UPDATE SET
  username = EXCLUDED.username,
  email = EXCLUDED.email,
  initials = EXCLUDED.initials,
  provider = EXCLUDED.provider,
  updated_at = now();

INSERT INTO simulations (
  id, user_id, title, target_url, status, started_at, completed_at, created_at
) VALUES (
  '11111111-1111-1111-1111-111111111111',
  '550e8400-e29b-41d4-a716-446655440000',
  'A-몰 로그인 플로우 UX 테스트',
  'https://demo.a-mall.com/login',
  'completed',
  now() - interval '30 minute',
  now() - interval '10 minute',
  now() - interval '35 minute'
)
ON CONFLICT (id) DO UPDATE SET
  user_id = EXCLUDED.user_id,
  title = EXCLUDED.title,
  target_url = EXCLUDED.target_url,
  status = EXCLUDED.status,
  started_at = EXCLUDED.started_at,
  completed_at = EXCLUDED.completed_at;

INSERT INTO simulation_settings (
  simulation_id, digital_literacy, vision_impairment, attention_level, persona_device, success_condition,
  age_count_10, age_count_20, age_count_30, age_count_40, age_count_50, age_count_60, age_count_70
) VALUES (
  '11111111-1111-1111-1111-111111111111',
  'medium', 20, 70, 'desktop', '프로필 페이지 도달',
  60, 220, 220, 180, 150, 110, 60
)
ON CONFLICT (simulation_id) DO UPDATE SET
  digital_literacy = EXCLUDED.digital_literacy,
  vision_impairment = EXCLUDED.vision_impairment,
  attention_level = EXCLUDED.attention_level,
  persona_device = EXCLUDED.persona_device,
  success_condition = EXCLUDED.success_condition,
  age_count_10 = EXCLUDED.age_count_10,
  age_count_20 = EXCLUDED.age_count_20,
  age_count_30 = EXCLUDED.age_count_30,
  age_count_40 = EXCLUDED.age_count_40,
  age_count_50 = EXCLUDED.age_count_50,
  age_count_60 = EXCLUDED.age_count_60,
  age_count_70 = EXCLUDED.age_count_70;

INSERT INTO simulation_overview (
  simulation_id, tested_agent_count, avg_completion_ms, success_event_count, updated_at
) VALUES (
  '11111111-1111-1111-1111-111111111111',
  1000, 252000, 280, now()
)
ON CONFLICT (simulation_id) DO UPDATE SET
  tested_agent_count = EXCLUDED.tested_agent_count,
  avg_completion_ms = EXCLUDED.avg_completion_ms,
  success_event_count = EXCLUDED.success_event_count,
  updated_at = now();

INSERT INTO simulation_pages (
  id, simulation_id, page_key, page_name, page_url, screenshot_path, viewport_width, viewport_height, page_order
) VALUES
(
  '20000000-0000-0000-0000-000000000001',
  '11111111-1111-1111-1111-111111111111',
  'landing',
  '랜딩 페이지',
  'https://demo.a-mall.com',
  'https://storage.example.com/screenshots/sim1-landing.png',
  1440, 900, 1
),
(
  '20000000-0000-0000-0000-000000000002',
  '11111111-1111-1111-1111-111111111111',
  'login',
  '로그인 페이지',
  'https://demo.a-mall.com/login',
  'https://storage.example.com/screenshots/sim1-login.png',
  1440, 900, 2
)
ON CONFLICT (id) DO UPDATE SET
  simulation_id = EXCLUDED.simulation_id,
  page_key = EXCLUDED.page_key,
  page_name = EXCLUDED.page_name,
  page_url = EXCLUDED.page_url,
  screenshot_path = EXCLUDED.screenshot_path,
  viewport_width = EXCLUDED.viewport_width,
  viewport_height = EXCLUDED.viewport_height,
  page_order = EXCLUDED.page_order;

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
('30000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000002','70대',38.0, 34, 13, 21, 19000)
ON CONFLICT (id) DO UPDATE SET
  page_id = EXCLUDED.page_id,
  age_band = EXCLUDED.age_band,
  success_rate = EXCLUDED.success_rate,
  entered = EXCLUDED.entered,
  passed = EXCLUDED.passed,
  drop_off = EXCLUDED.drop_off,
  avg_time_ms = EXCLUDED.avg_time_ms;

INSERT INTO issues (
  id, simulation_id, page_id, tags, category, sub_category, severity, title, description, target_html, benefit_label, benefit_delta, created_at
) VALUES
(
  '40000000-0000-0000-0000-000000000001',
  '11111111-1111-1111-1111-111111111111',
  '20000000-0000-0000-0000-000000000002',
  '["contrast","wcag_aa"]'::jsonb,
  '접근성',
  '대비율',
  'HIGH',
  '입력 레이블의 대비율이 너무 낮음',
  'WCAG AA 기준(4.5:1)을 충족하지 않는 레이블 대비율입니다.',
  '.form-label',
  '대비율 개선',
  '+12%',
  now() - interval '8 minute'
),
(
  '40000000-0000-0000-0000-000000000002',
  '11111111-1111-1111-1111-111111111111',
  '20000000-0000-0000-0000-000000000002',
  '["keyboard","focus"]'::jsonb,
  '사용성',
  '포커스',
  'MEDIUM',
  '제출 버튼이 키보드로 도달하기 어려움',
  '포커스 순서가 불필요한 키보드 이동을 만듭니다.',
  'button[type=submit]',
  '키보드 UX',
  '+7%',
  now() - interval '7 minute'
)
ON CONFLICT (id) DO UPDATE SET
  simulation_id = EXCLUDED.simulation_id,
  page_id = EXCLUDED.page_id,
  tags = EXCLUDED.tags,
  category = EXCLUDED.category,
  sub_category = EXCLUDED.sub_category,
  severity = EXCLUDED.severity,
  title = EXCLUDED.title,
  description = EXCLUDED.description,
  target_html = EXCLUDED.target_html,
  benefit_label = EXCLUDED.benefit_label,
  benefit_delta = EXCLUDED.benefit_delta;

INSERT INTO issue_age_stats (
  issue_id, age_band, coord_x, coord_y, scroll_y, affected_users, affected_percent, block_rate, repeat_count,
  error_type, timeout_count, network_count, console_count, description
) VALUES
('40000000-0000-0000-0000-000000000001','10대',0.42,0.31,120,  8,13.3,22.0,2.1,'콘솔',0,0,3,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000001','20대',0.42,0.31,120, 25,11.4,24.0,2.3,'콘솔',0,0,4,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000001','30대',0.42,0.31,120, 28,12.7,25.0,2.4,'콘솔',1,0,4,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000001','40대',0.42,0.31,120, 30,16.7,31.0,2.8,'콘솔',1,0,4,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000001','50대',0.42,0.31,120, 26,17.3,35.0,3.1,'콘솔',1,0,4,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000001','60대',0.42,0.31,120, 15,13.6,40.0,3.6,'콘솔',1,0,4,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000001','70대',0.42,0.31,120, 10,16.7,48.0,4.0,'콘솔',1,0,4,'레이블 근처 낮은 대비율'),
('40000000-0000-0000-0000-000000000002','10대',0.67,0.78,680,  7,11.7,30.0,2.5,'타임아웃',1,0,0,'키보드 포커스 이동'),
('40000000-0000-0000-0000-000000000002','20대',0.67,0.78,680, 20, 9.1,34.0,2.7,'타임아웃',2,0,0,'키보드 포커스 이동'),
('40000000-0000-0000-0000-000000000002','30대',0.67,0.78,680, 22,10.0,36.0,2.9,'타임아웃',2,0,0,'키보드 포커스 이동'),
('40000000-0000-0000-0000-000000000002','40대',0.67,0.78,680, 24,13.3,43.0,3.2,'타임아웃',3,0,0,'키보드 포커스 이동'),
('40000000-0000-0000-0000-000000000002','50대',0.67,0.78,680, 20,13.3,49.0,3.6,'타임아웃',3,0,0,'키보드 포커스 이동'),
('40000000-0000-0000-0000-000000000002','60대',0.67,0.78,680, 13,11.8,54.0,4.1,'타임아웃',4,0,0,'키보드 포커스 이동'),
('40000000-0000-0000-0000-000000000002','70대',0.67,0.78,680,  9,15.0,63.0,4.8,'타임아웃',4,0,0,'키보드 포커스 이동')
ON CONFLICT (issue_id, age_band) DO UPDATE SET
  coord_x = EXCLUDED.coord_x,
  coord_y = EXCLUDED.coord_y,
  scroll_y = EXCLUDED.scroll_y,
  affected_users = EXCLUDED.affected_users,
  affected_percent = EXCLUDED.affected_percent,
  block_rate = EXCLUDED.block_rate,
  repeat_count = EXCLUDED.repeat_count,
  error_type = EXCLUDED.error_type,
  timeout_count = EXCLUDED.timeout_count,
  network_count = EXCLUDED.network_count,
  console_count = EXCLUDED.console_count,
  description = EXCLUDED.description;

INSERT INTO ai_fix_suggestions (
  id, simulation_id, title, severity, before_code, after_code, impact_summary, change_summary_title, change_summary_body,
  impacted_users, created_at, issue_id
) VALUES
(
  '60000000-0000-0000-0000-000000000001',
  '11111111-1111-1111-1111-111111111111',
  '레이블 대비율 개선',
  'HIGH',
  '.form-label { color: #9CA3AF; }',
  '.form-label { color: #334155; font-weight: 500; }',
  '저시력 사용자의 가독성을 향상시킵니다.',
  '대비율 조정',
  'AA 수준의 대비 가이드를 충족하도록 색상과 굵기를 업데이트했습니다.',
  142,
  now() - interval '5 minute',
  '40000000-0000-0000-0000-000000000001'
),
(
  '60000000-0000-0000-0000-000000000002',
  '11111111-1111-1111-1111-111111111111',
  '키보드 제출 경로 수정',
  'MEDIUM',
  '<button type="submit" tabindex="-1">Submit</button>',
  '<button type="submit">Submit</button>',
  '키보드 탐색 마찰을 줄입니다.',
  '키보드 접근성',
  '잘못된 tabindex를 제거하고 포커스 순서를 정규화했습니다.',
  115,
  now() - interval '4 minute',
  '40000000-0000-0000-0000-000000000002'
)
ON CONFLICT (id) DO UPDATE SET
  simulation_id = EXCLUDED.simulation_id,
  title = EXCLUDED.title,
  severity = EXCLUDED.severity,
  before_code = EXCLUDED.before_code,
  after_code = EXCLUDED.after_code,
  impact_summary = EXCLUDED.impact_summary,
  change_summary_title = EXCLUDED.change_summary_title,
  change_summary_body = EXCLUDED.change_summary_body,
  impacted_users = EXCLUDED.impacted_users,
  issue_id = EXCLUDED.issue_id;

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
)
ON CONFLICT (id) DO UPDATE SET
  simulation_id = EXCLUDED.simulation_id,
  page_id = EXCLUDED.page_id,
  compliance_score = EXCLUDED.compliance_score,
  wcag_label = EXCLUDED.wcag_label,
  passed_tests = EXCLUDED.passed_tests,
  total_tests = EXCLUDED.total_tests,
  found_issues = EXCLUDED.found_issues;

INSERT INTO wcag_issues (
  id, wcag_result_id, issue_no, title, severity, description
) VALUES
(
  '51000000-0000-0000-0000-000000000001',
  '50000000-0000-0000-0000-000000000001',
  1,
  '텍스트 대비율이 기준 이하',
  'Critical',
  '본문 및 보조 텍스트의 대비가 WCAG AA를 충족하지 않습니다.'
),
(
  '51000000-0000-0000-0000-000000000002',
  '50000000-0000-0000-0000-000000000001',
  2,
  '접근 가능한 레이블 누락',
  'Moderate',
  '일부 입력 요소에 보조 기술을 위한 프로그래밍 방식의 레이블이 없습니다.'
),
(
  '51000000-0000-0000-0000-000000000003',
  '50000000-0000-0000-0000-000000000001',
  3,
  '장식용 이미지 대체 텍스트 정리',
  'Minor',
  '장식용 이미지는 빈 alt 또는 프레젠테이션 역할을 사용해야 합니다.'
)
ON CONFLICT (id) DO UPDATE SET
  wcag_result_id = EXCLUDED.wcag_result_id,
  issue_no = EXCLUDED.issue_no,
  title = EXCLUDED.title,
  severity = EXCLUDED.severity,
  description = EXCLUDED.description;

COMMIT;
