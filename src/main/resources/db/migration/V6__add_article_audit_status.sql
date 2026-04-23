ALTER TABLE articles
  ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'DRAFT';

UPDATE articles
SET status = CASE
  WHEN published = b'1' THEN 'PUBLISHED'
  ELSE 'DRAFT'
END;

CREATE INDEX idx_articles_status ON articles (status);
