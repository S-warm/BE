-- ============================================================
-- 테스트 데이터 INSERT
-- 실행 순서: users → simulations → simulation_settings
--           → simulation_pages → simulation_overview
--           → page_age_stats → issues → issue_age_stats
--           → wcag_results → wcag_issues → ai_fix_suggestions
-- ============================================================

-- 고정 UUID (조인 기준으로 사용)
-- user_id        : aaaaaaaa-1111-1111-1111-000000000001
-- simulation_id  : aaaaaaaa-2222-2222-2222-000000000001
-- page_id (로그인): aaaaaaaa-3333-3333-3333-000000000001
-- page_id (메인)  : aaaaaaaa-3333-3333-3333-000000000002
-- issue_id 1~5   : aaaaaaaa-4444-4444-4444-00000000000{1~5}
-- wcag_result_id : aaaaaaaa-5555-5555-5555-000000000001
-- ai_fix_id 1~3  : aaaaaaaa-6666-6666-6666-00000000000{1~3}


-- =============================================
-- 1. 사용자
-- =============================================
INSERT INTO users (id, username, email, provider)
VALUES (
    'aaaaaaaa-1111-1111-1111-000000000001',
    'test_user',
    'test@swarm.com',
    'local'
)
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- 2. 시뮬레이션
-- =============================================
INSERT INTO simulations (id, user_id, title, target_url, status, created_at)
VALUES (
    'aaaaaaaa-2222-2222-2222-000000000001',
    'aaaaaaaa-1111-1111-1111-000000000001',
    'A몰 로그인 플로우 UX 테스트',
    'https://a-mall.com',
    'completed',
    now()
)
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- 3. 시뮬레이션 설정
-- =============================================
INSERT INTO simulation_settings (
    simulation_id,
    age_count_10, age_count_20, age_count_30, age_count_40,
    age_count_50, age_count_60, age_count_70,
    digital_literacy, vision_impairment, attention_level,
    persona_device, success_condition
)
VALUES (
    'aaaaaaaa-2222-2222-2222-000000000001',
    50, 150, 200, 200, 150, 100, 50,
    'medium', 20, 70,
    'desktop', '결제 완료 페이지 도달'
)
ON CONFLICT (simulation_id) DO NOTHING;


-- =============================================
-- 4. 시뮬레이션 페이지
-- =============================================
INSERT INTO simulation_pages (id, simulation_id, page_key, page_name, page_url, screenshot_path, page_order)
VALUES
    (
        'aaaaaaaa-3333-3333-3333-000000000001',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'login',
        '로그인 페이지',
        'https://a-mall.com/login',
        '/screenshots/sim1_page1.png',
        1
    ),
    (
        'aaaaaaaa-3333-3333-3333-000000000002',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'main',
        '메인 페이지',
        'https://a-mall.com/',
        '/screenshots/sim1_page2.png',
        2
    )
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- 5. Overview (전체 요약)
-- =============================================
INSERT INTO simulation_overview (simulation_id, tested_agent_count, avg_completion_ms, success_event_count)
VALUES (
    'aaaaaaaa-2222-2222-2222-000000000001',
    1000,       -- totalAgents
    252000,     -- avgCompletionMs (252초 = 4분12초, /1000 변환 후 반환)
    280         -- successEventCount (성공률 = 280/1000 = 28%)
)
ON CONFLICT (simulation_id) DO NOTHING;


-- =============================================
-- 6. 페이지별 연령대 통계 (Overview 퍼널 패널용)
-- =============================================
-- 로그인 페이지 (page_order=1)
INSERT INTO page_age_stats (id, page_id, age_band, entered, passed, drop_off, success_rate, avg_time_ms)
VALUES
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '10대', 50,  48,  2,  96.0, 8000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '20대', 150, 135, 15, 90.0, 10000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '30대', 200, 170, 30, 85.0, 12000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '40대', 200, 160, 40, 80.0, 15000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '50대', 150, 105, 45, 70.0, 20000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '60대', 100,  60, 40, 60.0, 28000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000001', '70대',  50,  20, 30, 40.0, 40000)
ON CONFLICT DO NOTHING;

-- 메인 페이지 (page_order=2)
INSERT INTO page_age_stats (id, page_id, age_band, entered, passed, drop_off, success_rate, avg_time_ms)
VALUES
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '10대', 48,  46,  2,  95.8, 6000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '20대', 135, 120, 15, 88.9, 8000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '30대', 170, 148, 22, 87.1, 9000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '40대', 160, 130, 30, 81.3, 11000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '50대', 105,  70, 35, 66.7, 16000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '60대',  60,  35, 25, 58.3, 22000),
    (gen_random_uuid(), 'aaaaaaaa-3333-3333-3333-000000000002', '70대',  20,   8, 12, 40.0, 35000)
ON CONFLICT DO NOTHING;


-- =============================================
-- 7. 이슈
-- =============================================
INSERT INTO issues (id, simulation_id, page_id, severity, category, title, description, target_html, tags)
VALUES
    (
        'aaaaaaaa-4444-4444-4444-000000000001',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-3333-3333-3333-000000000001',
        'HIGH', 'Accessibility',
        '입력 레이블이 낮은 대비율',
        '흰색 배경 위의 회색 텍스트로 인해 WCAG 2.1 AA 기준(4.5:1) 미달',
        '.form-label',
        '["contrast","wcag_aa"]'
    ),
    (
        'aaaaaaaa-4444-4444-4444-000000000002',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-3333-3333-3333-000000000001',
        'MEDIUM', 'Accessibility',
        '제출 버튼이 키보드로 접근 불가',
        '탭 키로 제출 버튼에 포커스가 되지 않아 키보드 전용 사용자 접근 불가',
        '.submit-btn',
        '["keyboard","focus","wcag_aa"]'
    ),
    (
        'aaaaaaaa-4444-4444-4444-000000000003',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-3333-3333-3333-000000000001',
        'LOW', 'Usability',
        '오류 메시지 노출 시간이 짧음',
        '유효성 검사 실패 시 오류 메시지가 2초 이내 사라져 사용자가 인지하지 못함',
        '.error-message',
        '["timing","feedback"]'
    ),
    (
        'aaaaaaaa-4444-4444-4444-000000000004',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-3333-3333-3333-000000000002',
        'HIGH', 'Accessibility',
        '배너 이미지 alt 텍스트 누락',
        '메인 배너 이미지에 alt 속성이 없어 스크린리더 사용자 접근 불가',
        '.main-banner img',
        '["alt","wcag_aa","screen-reader"]'
    ),
    (
        'aaaaaaaa-4444-4444-4444-000000000005',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-3333-3333-3333-000000000002',
        'MEDIUM', 'Usability',
        '모바일 터치 영역 너무 작음',
        '하단 네비게이션 버튼의 터치 영역이 24px로 권장 최소값(44px) 미달',
        '.nav-btn',
        '["touch-target","mobile"]'
    )
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- 8. 이슈별 연령대 히트맵 통계
-- =============================================
INSERT INTO issue_age_stats (
    issue_id, age_band, coord_x, coord_y,
    affected_users, affected_percent, block_rate, repeat_count,
    error_type, timeout_count, network_count, console_count, description
)
VALUES
    -- issue 1 (입력 레이블 대비율) - 고령일수록 심각
    ('aaaaaaaa-4444-4444-4444-000000000001', '10대', 0.72, 0.35,  2,  4.0, 20.0, 1.0, 'Timeout', 1, 0, 0, '10대 낮은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000001', '20대', 0.72, 0.35,  5,  3.3, 25.0, 1.3, 'Timeout', 2, 0, 0, '20대 낮은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000001', '30대', 0.72, 0.35,  8,  4.7, 35.0, 1.8, 'Timeout', 3, 0, 0, '30대 중간 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000001', '40대', 0.72, 0.35, 15,  9.4, 55.0, 2.5, 'Timeout', 5, 1, 0, '40대 중간 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000001', '50대', 0.72, 0.35, 30, 28.6, 75.0, 4.0, 'Timeout', 8, 2, 0, '50대 높은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000001', '60대', 0.72, 0.35, 45, 75.0, 90.0, 6.5, 'Timeout', 9, 3, 1, '60대 심각'),
    ('aaaaaaaa-4444-4444-4444-000000000001', '70대', 0.72, 0.35, 37, 85.0,100.0, 8.3, 'Timeout', 9, 3, 2, '70대 매우 심각'),

    -- issue 2 (키보드 접근 불가)
    ('aaaaaaaa-4444-4444-4444-000000000002', '20대', 0.38, 0.52,  5,  3.3, 30.0, 1.5, 'Network', 0, 3, 0, '20대 네트워크'),
    ('aaaaaaaa-4444-4444-4444-000000000002', '30대', 0.38, 0.52,  8,  4.7, 40.0, 2.0, 'Network', 0, 4, 0, '30대 네트워크'),
    ('aaaaaaaa-4444-4444-4444-000000000002', '40대', 0.38, 0.52, 20, 12.5, 60.0, 3.2, 'Network', 0, 5, 1, '40대 네트워크'),
    ('aaaaaaaa-4444-4444-4444-000000000002', '50대', 0.38, 0.52, 40, 38.1, 80.0, 5.0, 'Network', 1, 6, 1, '50대 심각'),
    ('aaaaaaaa-4444-4444-4444-000000000002', '60대', 0.38, 0.52, 55, 91.7,100.0, 7.5, 'Network', 2, 8, 2, '60대 매우 심각'),

    -- issue 3 (오류 메시지 노출 시간)
    ('aaaaaaaa-4444-4444-4444-000000000003', '50대', 0.55, 0.68, 20, 19.0, 40.0, 2.1, 'Console', 0, 0, 3, '50대 Console'),
    ('aaaaaaaa-4444-4444-4444-000000000003', '60대', 0.55, 0.68, 35, 58.3, 65.0, 3.8, 'Console', 0, 0, 5, '60대 Console'),
    ('aaaaaaaa-4444-4444-4444-000000000003', '70대', 0.55, 0.68, 38, 76.0, 85.0, 5.2, 'Console', 0, 1, 6, '70대 Console'),

    -- issue 4 (배너 이미지 alt 텍스트 누락) - 스크린리더 의존 고령층일수록 심각
    ('aaaaaaaa-4444-4444-4444-000000000004', '20대', 0.50, 0.22,  3,  2.2, 15.0, 1.0, 'Console', 0, 0, 2, '20대 낮은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000004', '30대', 0.50, 0.22,  6,  3.5, 20.0, 1.2, 'Console', 0, 0, 3, '30대 낮은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000004', '40대', 0.50, 0.22, 18, 11.3, 45.0, 2.0, 'Console', 0, 1, 4, '40대 중간 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000004', '50대', 0.50, 0.22, 32, 30.5, 65.0, 3.5, 'Console', 0, 1, 6, '50대 높은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000004', '60대', 0.50, 0.22, 42, 70.0, 88.0, 5.8, 'Console', 0, 2, 8, '60대 심각'),
    ('aaaaaaaa-4444-4444-4444-000000000004', '70대', 0.50, 0.22, 16, 80.0, 95.0, 7.0, 'Console', 0, 2, 9, '70대 매우 심각'),

    -- issue 5 (모바일 터치 영역 너무 작음) - 손 떨림·운동 능력 저하로 고령층 더 심각
    ('aaaaaaaa-4444-4444-4444-000000000005', '30대', 0.50, 0.88,  5,  2.9, 20.0, 1.3, 'Timeout', 2, 0, 0, '30대 낮은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000005', '40대', 0.50, 0.88, 22, 13.8, 50.0, 2.8, 'Timeout', 4, 1, 0, '40대 중간 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000005', '50대', 0.50, 0.88, 38, 36.2, 72.0, 4.2, 'Timeout', 6, 1, 0, '50대 높은 빈도'),
    ('aaaaaaaa-4444-4444-4444-000000000005', '60대', 0.50, 0.88, 28, 46.7, 85.0, 6.0, 'Timeout', 7, 2, 1, '60대 심각'),
    ('aaaaaaaa-4444-4444-4444-000000000005', '70대', 0.50, 0.88,  7, 35.0, 90.0, 7.5, 'Timeout', 8, 2, 1, '70대 심각')

ON CONFLICT (issue_id, age_band) DO NOTHING;


-- =============================================
-- 9. WCAG 검사 결과
-- compliance_score = passed_tests / total_tests * 100 = 9 / 20 * 100 = 45
-- =============================================
INSERT INTO wcag_results (id, simulation_id, page_id, compliance_score, wcag_label, passed_tests, total_tests, found_issues)
VALUES (
    'aaaaaaaa-5555-5555-5555-000000000001',
    'aaaaaaaa-2222-2222-2222-000000000001',
    'aaaaaaaa-3333-3333-3333-000000000001',
    45, 'AA', 9, 20, 14
)
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- 10. WCAG 세부 이슈
-- =============================================
INSERT INTO wcag_issues (id, wcag_result_id, issue_no, title, severity, description)
VALUES
    ('bbbbbbbb-0000-0000-0000-000000000001', 'aaaaaaaa-5555-5555-5555-000000000001',  1, '텍스트 대비율',          'Critical', '본문 텍스트 대비가 WCAG 2.1 AA 기준 미달'),
    ('bbbbbbbb-0000-0000-0000-000000000002', 'aaaaaaaa-5555-5555-5555-000000000001',  2, '키보드 포커스 표시 없음', 'Critical', '키보드 탐색 시 포커스 인디케이터 미표시'),
    ('bbbbbbbb-0000-0000-0000-000000000003', 'aaaaaaaa-5555-5555-5555-000000000001',  3, '폼 레이블 연결 누락',    'Critical', '입력 필드에 연결된 레이블 없음'),
    ('bbbbbbbb-0000-0000-0000-000000000004', 'aaaaaaaa-5555-5555-5555-000000000001',  4, '이미지 대체 텍스트 누락','Critical', '배너 이미지 alt 속성 없음'),
    ('bbbbbbbb-0000-0000-0000-000000000005', 'aaaaaaaa-5555-5555-5555-000000000001',  5, '최소 글자 크기',         'Moderate', '일부 텍스트 12px 이하'),
    ('bbbbbbbb-0000-0000-0000-000000000006', 'aaaaaaaa-5555-5555-5555-000000000001',  6, '버튼 클릭 영역 부족',    'Moderate', '터치 영역 24px, 권장 44px 미달'),
    ('bbbbbbbb-0000-0000-0000-000000000007', 'aaaaaaaa-5555-5555-5555-000000000001',  7, '오류 메시지 접근성',     'Moderate', 'aria-live 속성 없음'),
    ('bbbbbbbb-0000-0000-0000-000000000008', 'aaaaaaaa-5555-5555-5555-000000000001',  8, '링크 텍스트 불명확',     'Moderate', '여기를 클릭 등 불명확한 링크'),
    ('bbbbbbbb-0000-0000-0000-000000000009', 'aaaaaaaa-5555-5555-5555-000000000001',  9, '키보드 포커스 순서',     'Moderate', '포커스 순서 시각 레이아웃 불일치'),
    ('bbbbbbbb-0000-0000-0000-000000000010', 'aaaaaaaa-5555-5555-5555-000000000001', 10, '제목 계층 구조 오류',    'Moderate', 'h1→h3 건너뜀'),
    ('bbbbbbbb-0000-0000-0000-000000000011', 'aaaaaaaa-5555-5555-5555-000000000001', 11, '위치 수정',              'Minor',    'UI 요소 위치 사용자 예상 동선 불일치'),
    ('bbbbbbbb-0000-0000-0000-000000000012', 'aaaaaaaa-5555-5555-5555-000000000001', 12, '색상만으로 정보 전달',   'Minor',    '색맹 사용자 인지 불가'),
    ('bbbbbbbb-0000-0000-0000-000000000013', 'aaaaaaaa-5555-5555-5555-000000000001', 13, '자동 재생 미디어',       'Minor',    '일시정지/음소거 컨트롤 미제공'),
    ('bbbbbbbb-0000-0000-0000-000000000014', 'aaaaaaaa-5555-5555-5555-000000000001', 14, '언어 속성 누락',         'Minor',    'HTML lang 속성 없음')
ON CONFLICT (id) DO NOTHING;


-- =============================================
-- 11. AI 수정 제안
-- =============================================
INSERT INTO ai_fix_suggestions (
    id, simulation_id, issue_id,
    title, severity,
    before_code, after_code,
    impact_summary, change_summary_body, impacted_users
)
VALUES
    (
        'aaaaaaaa-6666-6666-6666-000000000001',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-4444-4444-4444-000000000001',
        '입력 레이블이 낮은 대비율', 'HIGH',
        '.form-label { color: #999999; font-size: 14px; }',
        '.form-label { color: #334155; font-size: 14px; font-weight: 500; }',
        '142명의 사용자가 이제 레이블을 명확하게 읽을 수 있음',
        '레이블 색상을 #999999에서 #334155로 변경하여 대비율을 달성하고 WCAG 기준을 충족합니다.',
        142
    ),
    (
        'aaaaaaaa-6666-6666-6666-000000000002',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-4444-4444-4444-000000000002',
        '제출 버튼이 키보드로 접근 불가', 'MEDIUM',
        '.submit-btn { outline: none; }',
        '.submit-btn { outline: 2px solid #334155; outline-offset: 2px; }',
        '180명의 키보드 사용자가 버튼에 접근 가능해짐',
        'outline: none 제거 후 명시적 포커스 스타일을 추가하여 키보드 접근성을 확보합니다.',
        180
    ),
    (
        'aaaaaaaa-6666-6666-6666-000000000003',
        'aaaaaaaa-2222-2222-2222-000000000001',
        'aaaaaaaa-4444-4444-4444-000000000003',
        '오류 메시지 노출 시간이 짧음', 'LOW',
        '.error-message { display: none; animation: fadeOut 2s ease; }',
        '.error-message { display: block; animation: fadeOut 5s ease; }',
        '156명의 사용자가 오류 메시지를 충분한 시간 동안 인지할 수 있음',
        '오류 메시지 노출 시간을 2초에서 5초로 늘려 사용자가 내용을 확인할 수 있도록 개선합니다.',
        156
    )
ON CONFLICT (id) DO NOTHING;
