CREATE TABLE categories (
    id            UUID PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    slug          VARCHAR(120) NOT NULL UNIQUE,
    description   VARCHAR(500),
    display_order INT          NOT NULL DEFAULT 0
);

CREATE TABLE dishes (
    id           UUID PRIMARY KEY,
    category_id  UUID          NOT NULL REFERENCES categories (id),
    name         VARCHAR(150)  NOT NULL,
    slug         VARCHAR(180)  NOT NULL UNIQUE,
    description  VARCHAR(1000),
    price        NUMERIC(12, 2) NOT NULL,
    cost         NUMERIC(12, 2),
    available    BOOLEAN       NOT NULL DEFAULT TRUE,
    featured     BOOLEAN       NOT NULL DEFAULT FALSE,
    image_url    VARCHAR(255),
    prep_minutes INT,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_dishes_category ON dishes (category_id);

CREATE TABLE dish_tags (
    dish_id UUID        NOT NULL REFERENCES dishes (id) ON DELETE CASCADE,
    tag     VARCHAR(50) NOT NULL
);

CREATE TABLE dish_allergens (
    dish_id  UUID        NOT NULL REFERENCES dishes (id) ON DELETE CASCADE,
    allergen VARCHAR(50) NOT NULL
);

CREATE TABLE dish_pairings (
    dish_id        UUID NOT NULL REFERENCES dishes (id) ON DELETE CASCADE,
    paired_dish_id UUID NOT NULL
);

CREATE TABLE dish_price_history (
    id         UUID PRIMARY KEY,
    dish_id    UUID           NOT NULL REFERENCES dishes (id) ON DELETE CASCADE,
    price      NUMERIC(12, 2) NOT NULL,
    changed_by UUID,
    changed_at TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_dish_price_history_dish ON dish_price_history (dish_id);
