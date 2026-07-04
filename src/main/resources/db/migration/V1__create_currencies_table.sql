-- V1__create_currencies_table.sql
-- Migration #1: create table currencies

-- Creating table 'currencies' for saving main data of currencies from National Bank Ukraine API
-- Эта таблица отправляет в Kafka (rate) из этого микросервиса java-currencies-microservice
-- для получения данных курсов валют в других микросервисах (например, java-credit-cards-microservice)
CREATE TABLE IF NOT EXISTS currencies
(
    id              BIGSERIAL PRIMARY KEY,      -- BIGSERIAL для автоинкремента
    cc              VARCHAR(255) NOT NULL,      -- currency code (например, USD, EUR)
    r030            INT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    buy_price       NUMERIC(19, 4) NOT NULL DEFAULT 0.0,
    sell_price      NUMERIC(19, 4) NOT NULL DEFAULT 0.0,
    exchangedate    VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP(6) DEFAULT NOW()  -- datetime(6) -> TIMESTAMP(6) с микросекундами
);

-- Creation of index for fast search by currency code
CREATE INDEX idx_cc ON currencies(cc);

-- Creation of index for fast search by exchange date
CREATE INDEX idx_exchangedate ON currencies(exchangedate);

