ALTER USER swarm WITH PASSWORD '0000';
ALTER SYSTEM SET password_encryption = 'md5';
SELECT pg_reload_conf();