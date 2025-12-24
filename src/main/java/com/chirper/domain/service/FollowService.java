package com.chirper.domain.service;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.valueobject.UserId;

import java.util.Objects;
import java.util.Optional;

/**
 * FollowService
 * フォロー関係の妥当性検証を担当するドメインサービス
 */
public class FollowService {

    private final IFollowRepository followRepository;

    /**
     * コンストラクタ
     * @param followRepository フォローリポジトリ
     */
    public FollowService(IFollowRepository followRepository) {
        this.followRepository = Objects.requireNonNull(followRepository, "FollowRepository cannot be null");
    }

    /**
     * フォロー可能性を検証
     * ビジネスルール:
     * - 自分自身をフォローできない
     * - 既にフォロー済みの場合はエラー
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @throws IllegalArgumentException 自分自身をフォローしようとした場合
     * @throws IllegalStateException 既にフォロー済みの場合
     */
    public void validateFollow(UserId followerUserId, UserId followedUserId) {
        // ビジネスルール: 自分自身をフォローできない
        if (followerUserId.equals(followedUserId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        // 既にフォロー済みかチェック
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowed(followerUserId, followedUserId);
        if (existingFollow.isPresent()) {
            throw new IllegalStateException("Already following this user");
        }
    }

    /**
     * フォロー解除可能性を検証
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @throws IllegalStateException フォローしていない場合
     */
    public void validateUnfollow(UserId followerUserId, UserId followedUserId) {
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowed(followerUserId, followedUserId);
        if (existingFollow.isEmpty()) {
            throw new IllegalStateException("Not following this user");
        }
    }

    /**
     * フォロー済みかチェック
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @return フォロー済みの場合true
     */
    public boolean isFollowing(UserId followerUserId, UserId followedUserId) {
        return followRepository.findByFollowerAndFollowed(followerUserId, followedUserId).isPresent();
    }
}
