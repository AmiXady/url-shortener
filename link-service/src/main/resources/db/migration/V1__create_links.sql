create sequence links_id_seq start with 1 increment by 50;

create TABLE links
(
    id           BIGINT      PRIMARY KEY DEFAULT nextval('links_id_seq'),
    code         VARCHAR(30) NOT NULL,
    original_url TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ,
    token_hash   VARCHAR(64),

    CONSTRAINT uk_links_code UNIQUE (code)
);