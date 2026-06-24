DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS articles;

CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text VARCHAR(4000) NOT NULL
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    article_fk BIGINT NOT NULL,
    text VARCHAR(2000) NOT NULL,
    score INTEGER NOT NULL CHECK (score BETWEEN 1 AND 100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_article
        FOREIGN KEY (article_fk) REFERENCES articles (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_comments_article_fk ON comments(article_fk);

