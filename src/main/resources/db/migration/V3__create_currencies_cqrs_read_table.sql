-- V3__create_currencies_cqrs_read_table.sql
-- Migration #3: create table currencies_cqrs_read

-- Creating table 'currencies_cqrs_read' for reading main data of currencies (CQRS pattern)
CREATE TABLE IF NOT EXISTS currencies_cqrs_read
(
    id              BIGSERIAL PRIMARY KEY,      -- BIGSERIAL для автоинкремента
    cc              VARCHAR(255) NOT NULL,      -- currency code (например, USD, EUR)
    r030            INT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    rate            DOUBLE PRECISION NOT NULL,  -- вместо FLOAT
    exchangedate    VARCHAR(255) NOT NULL,

    updated_at      TIMESTAMP(6) DEFAULT NOW()  -- datetime(6) -> TIMESTAMP(6) с микросекундами
);

-- Creation of index for fast search by currency code
CREATE INDEX idx_cqrs_read_cc ON currencies_cqrs_read(cc);

-- Creation of index for fast search by exchange date
CREATE INDEX idx_cqrs_read_exchangedate ON currencies_cqrs_read(exchangedate);

