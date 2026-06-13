CREATE TABLE customer_profiles (
    id             UUID PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    full_name      VARCHAR(255) NOT NULL,
    phone          VARCHAR(30),
    frequent_guest BOOLEAN      NOT NULL DEFAULT FALSE,
    visits         INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE customer_dietary_preferences (
    customer_id UUID         NOT NULL REFERENCES customer_profiles (id) ON DELETE CASCADE,
    preference  VARCHAR(100) NOT NULL
);

CREATE TABLE customer_allergies (
    customer_id UUID         NOT NULL REFERENCES customer_profiles (id) ON DELETE CASCADE,
    allergy     VARCHAR(100) NOT NULL
);

CREATE TABLE addresses (
    id          UUID PRIMARY KEY,
    customer_id UUID         NOT NULL,
    label       VARCHAR(60)  NOT NULL,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    notes       VARCHAR(500),
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_addresses_customer ON addresses (customer_id);

CREATE TABLE payment_method_refs (
    id            UUID PRIMARY KEY,
    customer_id   UUID         NOT NULL,
    gateway_token VARCHAR(255) NOT NULL UNIQUE,
    brand         VARCHAR(30)  NOT NULL,
    last4         VARCHAR(4)   NOT NULL
);

CREATE INDEX idx_payment_method_refs_customer ON payment_method_refs (customer_id);

CREATE TABLE staff_members (
    id        UUID PRIMARY KEY,
    email     VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    role      VARCHAR(30)  NOT NULL,
    position  VARCHAR(100),
    active    BOOLEAN      NOT NULL DEFAULT TRUE,
    hired_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
