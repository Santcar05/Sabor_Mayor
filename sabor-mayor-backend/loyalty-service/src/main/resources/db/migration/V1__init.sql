CREATE TABLE loyalty_accounts (
    user_id         UUID PRIMARY KEY,
    points          INT         NOT NULL DEFAULT 0,
    lifetime_points INT         NOT NULL DEFAULT 0,
    level           VARCHAR(30) NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE loyalty_transactions (
    id          UUID PRIMARY KEY,
    user_id     UUID        NOT NULL,
    points      INT         NOT NULL,
    type        VARCHAR(10) NOT NULL,
    order_id    UUID,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_loyalty_transactions_user ON loyalty_transactions (user_id);
CREATE INDEX idx_loyalty_transactions_order ON loyalty_transactions (order_id);
