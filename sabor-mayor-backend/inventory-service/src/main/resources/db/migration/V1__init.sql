CREATE TABLE ingredients (
    id             UUID PRIMARY KEY,
    name           VARCHAR(150)   NOT NULL UNIQUE,
    unit           VARCHAR(20)    NOT NULL,
    stock_quantity NUMERIC(12, 3) NOT NULL DEFAULT 0,
    min_stock      NUMERIC(12, 3) NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE dish_recipes (
    id                UUID PRIMARY KEY,
    dish_id           UUID           NOT NULL,
    ingredient_id     UUID           NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    quantity_per_dish NUMERIC(12, 3) NOT NULL
);

CREATE INDEX idx_dish_recipes_dish ON dish_recipes (dish_id);

CREATE TABLE stock_movements (
    id            UUID PRIMARY KEY,
    ingredient_id UUID           NOT NULL REFERENCES ingredients (id) ON DELETE CASCADE,
    type          VARCHAR(15)    NOT NULL,
    quantity      NUMERIC(12, 3) NOT NULL,
    order_id      UUID,
    note          VARCHAR(255),
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_stock_movements_ingredient ON stock_movements (ingredient_id);
CREATE INDEX idx_stock_movements_order ON stock_movements (order_id);
