package com.chirper.presentation.dto.tweet;

import java.time.Instant;
import java.util.UUID;

public record TweetResponse(
    UUID tweetId,
    UUID userId,
    String username,
    String displayName,
    String avatarUrl,
    String content,
    Instant createdAt,
    int likesCount,
    int retweetsCount,
    boolean likedByCurrentUser,
    boolean retweetedByCurrentUser
) {}
