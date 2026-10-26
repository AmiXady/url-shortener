create TABLE link_clicks
(
    link_id    BIGINT      PRIMARY KEY,
    clicks     BIGINT      NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);