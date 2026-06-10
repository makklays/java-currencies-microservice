-- V4__create_currencies_idempotency_table.sql
-- Migration #4: create table currencies_idempotency

-- Creating table 'currencies_idempotency' for saving main data of currencies_idempotency
CREATE TABLE IF NOT EXISTS currencies_idempotency
(
    idempotency_key  VARCHAR(255) PRIMARY KEY, -- Unique client UUID token
    response_body    TEXT,                     -- Response cache (to return the same JSON in case of a duplicate, if needed)
    status           VARCHAR(20) NOT NULL,     -- Operation status: 'PROCESSING' or 'COMPLETED'
    created_at       TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
);

-- Index for scheduled cleanup of expired keys (if needed)
CREATE INDEX IF NOT EXISTS idx_currencies_idempotency_created_at ON currencies_idempotency(created_at);

