-- V5__create_retweets_table.sql
-- Create retweets table for tweet retweets

CREATE TABLE retweets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tweet_id UUID NOT NULL REFERENCES tweets(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT uk_user_tweet_retweet UNIQUE (user_id, tweet_id)
);

-- Create indexes for performance
CREATE INDEX idx_user_id_retweet ON retweets(user_id);
CREATE INDEX idx_tweet_id_retweet ON retweets(tweet_id);

-- Add comments for documentation
COMMENT ON TABLE retweets IS 'Tweet retweets by users';
COMMENT ON COLUMN retweets.id IS 'Unique retweet identifier (UUID)';
COMMENT ON COLUMN retweets.user_id IS 'User who retweeted (foreign key to users)';
COMMENT ON COLUMN retweets.tweet_id IS 'Tweet that was retweeted (foreign key to tweets)';
COMMENT ON COLUMN retweets.created_at IS 'Retweet creation timestamp (UTC)';
COMMENT ON CONSTRAINT uk_user_tweet_retweet ON retweets IS 'Prevent duplicate retweets from same user on same tweet';
