-- V2__create_tweets_table.sql
-- Create tweets table for Chirper backend

CREATE TABLE tweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_content_length CHECK (char_length(content) <= 280)
);

-- Create indexes for performance
CREATE INDEX idx_user_id_created_at ON tweets(user_id, created_at DESC);
CREATE INDEX idx_created_at ON tweets(created_at DESC);
CREATE INDEX idx_is_deleted ON tweets(is_deleted) WHERE is_deleted = FALSE;

-- Add comments for documentation
COMMENT ON TABLE tweets IS 'User tweets/posts for Chirper application';
COMMENT ON COLUMN tweets.id IS 'Unique tweet identifier (UUID)';
COMMENT ON COLUMN tweets.user_id IS 'Foreign key to users table (tweet author)';
COMMENT ON COLUMN tweets.content IS 'Tweet content (1-280 characters)';
COMMENT ON COLUMN tweets.created_at IS 'Tweet creation timestamp (UTC)';
COMMENT ON COLUMN tweets.updated_at IS 'Last tweet update timestamp (UTC)';
COMMENT ON COLUMN tweets.is_deleted IS 'Logical deletion flag (soft delete)';
