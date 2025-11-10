-- Create posts table for R2DBC reactive database sample
CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on author for faster queries
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author);

-- Create full-text search index
CREATE INDEX IF NOT EXISTS idx_posts_title_content ON posts USING GIN(
    to_tsvector('korean', title || ' ' || content)
);
