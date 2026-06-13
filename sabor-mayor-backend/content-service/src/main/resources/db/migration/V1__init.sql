CREATE TABLE blog_posts (
    id               UUID PRIMARY KEY,
    title            VARCHAR(200) NOT NULL,
    slug             VARCHAR(230) NOT NULL UNIQUE,
    excerpt          VARCHAR(300),
    body             TEXT         NOT NULL,
    cover_image      VARCHAR(255),
    author_id        UUID         NOT NULL,
    author_name      VARCHAR(255) NOT NULL,
    meta_title       VARCHAR(200),
    meta_description VARCHAR(300),
    published        BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE comments (
    id          UUID PRIMARY KEY,
    post_id     UUID          NOT NULL REFERENCES blog_posts (id) ON DELETE CASCADE,
    author_id   UUID          NOT NULL,
    author_name VARCHAR(255)  NOT NULL,
    body        VARCHAR(1000) NOT NULL,
    approved    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_comments_post ON comments (post_id);

CREATE TABLE gallery_images (
    id            UUID PRIMARY KEY,
    url           VARCHAR(255) NOT NULL,
    caption       VARCHAR(200),
    display_order INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
