-- ============================================================
-- V5: page_age_stats (page_id, age_band) 유니크 제약 추가
-- ON CONFLICT DO NOTHING 이 UUID PK만 체크해서 중복 삽입되던 문제 수정
-- ============================================================

-- [1] 중복 행 제거 (id가 큰 쪽 삭제, 작은 id 하나만 남김)
DELETE FROM page_age_stats a
USING page_age_stats b
WHERE a.id > b.id
  AND a.page_id = b.page_id
  AND a.age_band = b.age_band;

-- [2] 유니크 제약 추가
ALTER TABLE page_age_stats
    ADD CONSTRAINT uq_page_age_stats_page_ageband UNIQUE (page_id, age_band);
