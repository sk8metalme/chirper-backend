-- V3__create_follows_table.sql
-- Create follows table for user follow relationships

CREATE TABLE follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    followed_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT uk_follower_followed UNIQUE (follower_user_id, followed_user_id),
    CONSTRAINT chk_no_self_follow CHECK (follower_user_id <> followed_user_id)
);

-- Create indexes for performance
CREATE INDEX idx_follower_user_id ON follows(follower_user_id);
CREATE INDEX idx_followed_user_id ON follows(followed_user_id);

-- Add comments for documentation
COMMENT ON TABLE follows IS 'User follow relationships';
COMMENT ON COLUMN follows.id IS 'Unique follow relationship identifier (UUID)';
COMMENT ON COLUMN follows.follower_user_id IS 'User who is following (foreign key to users)';
COMMENT ON COLUMN follows.followed_user_id IS 'User who is being followed (foreign key to users)';
COMMENT ON COLUMN follows.created_at IS 'Follow relationship creation timestamp (UTC)';
COMMENT ON CONSTRAINT uk_follower_followed ON follows IS 'Prevent duplicate follow relationships';
COMMENT ON CONSTRAINT chk_no_self_follow ON follows IS 'Prevent users from following themselves';
