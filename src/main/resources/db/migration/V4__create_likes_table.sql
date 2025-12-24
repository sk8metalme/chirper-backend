-- V4__create_likes_table.sql
-- Create likes table for tweet likes

CREATE TABLE likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tweet_id UUID NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT uk_user_tweet_like UNIQUE (user_id, tweet_id)
);

-- Create indexes for performance
CREATE INDEX idx_user_id_like ON likes(user_id);
CREATE INDEX idx_tweet_id_like ON likes(tweet_id);

-- Add comments for documentation
COMMENT ON TABLE likes IS 'Tweet likes by users';
COMMENT ON COLUMN likes.id IS 'Unique like identifier (UUID)';
COMMENT ON COLUMN likes.user_id IS 'User who liked the tweet (foreign key to users)';
COMMENT ON COLUMN likes.tweet_id IS 'Tweet that was liked (foreign key to tweets)';
COMMENT ON COLUMN likes.created_at IS 'Like creation timestamp (UTC)';
COMMENT ON CONSTRAINT uk_user_tweet_like ON likes IS 'Prevent duplicate likes from same user on same tweet';
