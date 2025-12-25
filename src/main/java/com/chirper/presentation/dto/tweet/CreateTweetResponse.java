package com.chirper.presentation.dto.tweet;

import java.time.Instant;
import java.util.UUID;

public record CreateTweetResponse(
    UUID tweetId,
    Instant createdAt
) {}
