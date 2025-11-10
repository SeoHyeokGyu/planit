-- Create initial schema for Todo table (existing)
CREATE TABLE IF NOT EXISTS todo (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create index on completed for faster queries
CREATE INDEX IF NOT EXISTS idx_todo_completed ON todo(completed);
