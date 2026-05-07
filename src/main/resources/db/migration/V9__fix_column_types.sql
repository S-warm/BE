-- ============================================================
-- V9: 컬럼 타입 수정 (Hibernate validate 통과용)
-- ============================================================

-- simulation_overview.avg_duration_ms: int4 → bigint
ALTER TABLE simulation_overview
    ALTER COLUMN avg_duration_ms TYPE BIGINT USING avg_duration_ms::BIGINT;
