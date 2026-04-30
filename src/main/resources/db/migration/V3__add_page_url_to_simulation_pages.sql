ALTER TABLE simulation_pages
    ADD COLUMN IF NOT EXISTS page_url VARCHAR(1000);

UPDATE simulation_pages
SET page_url = CASE page_key
    WHEN 'landing' THEN 'https://demo.a-mall.com'
    WHEN 'login' THEN 'https://demo.a-mall.com/login'
    WHEN 'otp' THEN 'https://demo.a-mall.com/login/otp'
    WHEN 'mypage' THEN 'https://demo.a-mall.com/mypage'
    ELSE NULL
END
WHERE page_url IS NULL;
