-- Run against the local postgres database before backend integration tests.
SELECT 'CREATE DATABASE url_shortener_test'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'url_shortener_test'
);
\gexec
