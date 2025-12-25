package com.chirper.presentation.dto.user;

import java.util.UUID;

/**
 * UserSearchResponse
 * 検索結果のユーザー情報レスポンス
 */
public record UserSearchResponse(
    UUID userId,
    String username,
    String displayName,
    String bio,
    String avatarUrl
) {}
