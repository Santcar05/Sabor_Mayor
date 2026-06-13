CREATE TABLE payments (
    id                UUID PRIMARY KEY,
    order_id          UUID           NOT NULL,
    customer_id       UUID           NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL,
    tip               NUMERIC(12, 2),
    method            VARCHAR(20)    NOT NULL,
    gateway           VARCHAR(30)    NOT NULL,
    gateway_reference VARCHAR(255),
    status            VARCHAR(25)    NOT NULL,
    refunded_amount   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_payments_order ON payments (order_id);
CREATE INDEX idx_payments_customer ON payments (customer_id);

CREATE TABLE refunds (
    id                UUID PRIMARY KEY,
    payment_id        UUID           NOT NULL REFERENCES payments (id),
    amount            NUMERIC(12, 2) NOT NULL,
    reason            VARCHAR(300),
    gateway_reference VARCHAR(255),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_refunds_payment ON refunds (payment_id);

CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    topic          VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (created_at) WHERE published_at IS NULL;
