package com.chirper.presentation.dto.user;

import java.util.UUID;

/**
 * FollowUserResponse
 * フォロワー/フォロー中一覧の各ユーザー情報レスポンス
 */
public record FollowUserResponse(
    UUID userId,
    String username,
    String displayName,
    String avatarUrl,
    boolean followedByCurrentUser
) {}
