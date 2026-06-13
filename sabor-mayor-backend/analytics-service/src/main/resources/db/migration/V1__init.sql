CREATE TABLE sales_facts (
    order_id    UUID PRIMARY KEY,
    customer_id UUID           NOT NULL,
    sale_date   DATE           NOT NULL,
    total       NUMERIC(12, 2) NOT NULL,
    tip         NUMERIC(12, 2) NOT NULL DEFAULT 0,
    item_count  INT            NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_sales_facts_date ON sales_facts (sale_date);

CREATE TABLE dish_sales_facts (
    id            UUID PRIMARY KEY,
    dish_id       UUID           NOT NULL,
    dish_name     VARCHAR(150)   NOT NULL,
    sale_date     DATE           NOT NULL,
    quantity_sold INT            NOT NULL DEFAULT 0,
    revenue       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_dish_sales UNIQUE (dish_id, sale_date)
);

CREATE INDEX idx_dish_sales_date ON dish_sales_facts (sale_date);

CREATE TABLE reservation_facts (
    reservation_id   UUID PRIMARY KEY,
    reservation_date DATE    NOT NULL,
    party_size       INT     NOT NULL,
    cancelled        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_reservation_facts_date ON reservation_facts (reservation_date);
