-- V2__create_outbox_events_table.sql
-- Migration #2: create table outbox_events

-- Creating table a universal Outbox table 'outbox_events' for all event types related to the Currency entity.
-- This design allows us to store all events in a single table, making it easier to manage and query events
-- for different entities if needed in the future.
CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,    -- Entity type (e.g., 'Currency')
    aggregate_id VARCHAR(255) NOT NULL,     -- Entity ID (card_id)
    event_type VARCHAR(100) NOT NULL,       -- Event name (e.g., 'CurrencyUpdated')
    payload JSONB NOT NULL,                 -- Event data in JSON format
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP(6)
);

-- Index for the background worker (critical for performance)
CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed ON outbox_events(created_at) WHERE processed = FALSE;

