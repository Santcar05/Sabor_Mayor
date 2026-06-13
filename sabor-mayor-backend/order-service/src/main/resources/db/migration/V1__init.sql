CREATE TABLE restaurant_tables (
    id           UUID PRIMARY KEY,
    table_number INT         NOT NULL UNIQUE,
    capacity     INT         NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'LIBRE',
    qr_token     VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE orders (
    id               UUID PRIMARY KEY,
    customer_id      UUID           NOT NULL,
    order_type       VARCHAR(20)    NOT NULL,
    status           VARCHAR(20)    NOT NULL,
    table_id         UUID,
    delivery_address VARCHAR(500),
    subtotal         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    tip              NUMERIC(12, 2),
    total            NUMERIC(12, 2) NOT NULL DEFAULT 0,
    payment_id       UUID,
    notes            VARCHAR(500),
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id          UUID PRIMARY KEY,
    order_id    UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    dish_id     UUID           NOT NULL,
    dish_name   VARCHAR(150)   NOT NULL,
    unit_price  NUMERIC(12, 2) NOT NULL,
    quantity    INT            NOT NULL,
    notes       VARCHAR(300),
    station     VARCHAR(20)    NOT NULL,
    item_status VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_order_items_order ON order_items (order_id);

CREATE TABLE carts (
    customer_id UUID PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_items (
    id               UUID PRIMARY KEY,
    cart_customer_id UUID NOT NULL REFERENCES carts (customer_id) ON DELETE CASCADE,
    dish_id          UUID NOT NULL,
    quantity         INT  NOT NULL,
    notes            VARCHAR(300)
);

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
