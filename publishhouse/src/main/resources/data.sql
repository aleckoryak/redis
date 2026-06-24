INSERT INTO articles (title, text)
SELECT
    'Stub Article #' || gs,
    'This is stub article content #' || gs
FROM generate_series(1, 200) AS gs;

INSERT INTO comments (article_fk, text, score)
SELECT
    -- spread comments across articles: comment gs belongs to article ((gs - 1) % 200) + 1
    ((gs - 1) % 200) + 1,
    'Stub comment #' || gs || ' on article #' || (((gs - 1) % 200) + 1),
    -- deterministic score 1..100 using modulo
    (gs % 100) + 1
FROM generate_series(1, 600) AS gs;
