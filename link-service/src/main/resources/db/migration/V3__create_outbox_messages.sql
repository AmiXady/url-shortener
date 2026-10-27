CREATE TABLE outbox_messages
(
    id              UUID         PRIMARY KEY,
    aggregate_type  VARCHAR(50)  NOT NULL,
    aggregate_id    VARCHAR(50)  NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    attempts        INT          NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ  NOT NULL,
    last_error      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL,
    published_at    TIMESTAMPTZ
);

-- Relay ищет только готовые к отправке: PENDING и время пришло.
-- Частичный индекс: отправленных миллионы, а в индексе только ожидающие.
CREATE INDEX idx_outbox_pending
    ON outbox_messages (next_attempt_at)
    WHERE status = 'PENDING';