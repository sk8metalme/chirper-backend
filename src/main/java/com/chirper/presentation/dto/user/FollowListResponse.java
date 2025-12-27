package com.chirper.presentation.dto.user;

import java.util.List;

/**
 * FollowListResponse
 * フォロワー/フォロー中一覧レスポンス（ページネーション対応）
 */
public record FollowListResponse(
    List<FollowUserResponse> users,
    int currentPage,
    int totalPages
) {}
