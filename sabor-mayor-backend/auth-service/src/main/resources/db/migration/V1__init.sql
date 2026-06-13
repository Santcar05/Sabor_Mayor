CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100),
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    provider      VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id   VARCHAR(255),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users (lower(email));
CREATE INDEX idx_users_provider ON users (provider, provider_id);

CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE audit_log (
    id         UUID PRIMARY KEY,
    actor_id   UUID,
    action     VARCHAR(100) NOT NULL,
    detail     TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
