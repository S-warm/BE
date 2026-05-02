-- ============================================================
-- V3: 연령대 구조 변경 + 불필요 컬럼 제거 + 누락 컬럼 추가
-- ============================================================

-- [1] simulations: persona_count 제거 (연령대별 합산으로 대체)
ALTER TABLE simulations
    DROP COLUMN IF EXISTS persona_count;

-- [2] simulation_settings: 연령대 3그룹 비율 → 7개 연령대 인원수로 교체
ALTER TABLE simulation_settings
    DROP COLUMN IF EXISTS age_ratio_teen,
    DROP COLUMN IF EXISTS age_ratio_fifty,
    DROP COLUMN IF EXISTS age_ratio_eighty;

ALTER TABLE simulation_settings
    ADD COLUMN IF NOT EXISTS age_count_10 INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS age_count_20 INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS age_count_30 INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS age_count_40 INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS age_count_50 INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS age_count_60 INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS age_count_70 INT DEFAULT 0;

-- [3] simulation_overview: conversion_rate 제거 (Service에서 계산)
ALTER TABLE simulation_overview
    DROP COLUMN IF EXISTS conversion_rate;

-- [4] ai_fix_suggestions: page_id 제거 (issue_id → issues.page_id 로 접근)
DROP INDEX IF EXISTS idx_ai_fix_page;

ALTER TABLE ai_fix_suggestions
    DROP COLUMN IF EXISTS page_id;

-- [5] page_age_stats: avg_time_ms 추가 (FunnelPanel 평균 체류 시간)
ALTER TABLE page_age_stats
    ADD COLUMN IF NOT EXISTS avg_time_ms INT DEFAULT 0;

-- [6] issue_age_stats: description 추가 (히트맵 오류 지점 AI 설명)
ALTER TABLE issue_age_stats
    ADD COLUMN IF NOT EXISTS description TEXT;
