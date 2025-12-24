package com.chirper.presentation.dto.user;

import com.chirper.presentation.dto.tweet.TweetResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
    UUID userId,
    String username,
    String displayName,
    String bio,
    String avatarUrl,
    Instant createdAt,
    int followersCount,
    int followingCount,
    boolean followedByCurrentUser,
    List<TweetResponse> userTweets
) {}
