CREATE TABLE reservations (
    id               UUID PRIMARY KEY,
    customer_id      UUID         NOT NULL,
    customer_name    VARCHAR(255) NOT NULL,
    customer_email   VARCHAR(255) NOT NULL,
    reservation_date DATE         NOT NULL,
    reservation_time TIME         NOT NULL,
    party_size       INT          NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    deposit_required BOOLEAN      NOT NULL DEFAULT FALSE,
    deposit_amount   NUMERIC(12, 2),
    special_requests VARCHAR(500),
    reminder_sent_at TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_reservations_customer ON reservations (customer_id);
CREATE INDEX idx_reservations_date_time ON reservations (reservation_date, reservation_time);

CREATE TABLE reservation_pre_order_items (
    reservation_id UUID NOT NULL REFERENCES reservations (id) ON DELETE CASCADE,
    dish_id        UUID NOT NULL,
    quantity       INT  NOT NULL
);

CREATE TABLE schedule_rules (
    id                UUID PRIMARY KEY,
    day_of_week       VARCHAR(10) NOT NULL UNIQUE,
    open_time         TIME        NOT NULL,
    close_time        TIME        NOT NULL,
    slot_minutes      INT         NOT NULL,
    capacity_per_slot INT         NOT NULL
);

CREATE TABLE blocked_dates (
    id           UUID PRIMARY KEY,
    blocked_date DATE NOT NULL UNIQUE,
    reason       VARCHAR(255)
);
