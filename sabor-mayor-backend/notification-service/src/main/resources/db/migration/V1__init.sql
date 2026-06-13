CREATE TABLE notifications (
    id         UUID PRIMARY KEY,
    user_id    UUID,
    channel    VARCHAR(15)  NOT NULL,
    recipient  VARCHAR(255) NOT NULL,
    subject    VARCHAR(255),
    body       TEXT         NOT NULL,
    template   VARCHAR(60),
    status     VARCHAR(10)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_created ON notifications (created_at);
