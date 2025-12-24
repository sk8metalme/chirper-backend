package com.chirper.domain.entity;

import com.chirper.domain.valueobject.FollowId;
import com.chirper.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Follow Entity
 * フォロー関係のビジネスロジックとビジネスルールをカプセル化するドメインエンティティ
 */
public class Follow {
    private final FollowId id;
    private final UserId followerUserId;
    private final UserId followedUserId;
    private final Instant createdAt;

    private Follow(FollowId id, UserId followerUserId, UserId followedUserId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "FollowId cannot be null");
        this.followerUserId = Objects.requireNonNull(followerUserId, "FollowerUserId cannot be null");
        this.followedUserId = Objects.requireNonNull(followedUserId, "FollowedUserId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");

        // ビジネスルール: 自分自身をフォローできない
        if (followerUserId.equals(followedUserId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }
    }

    /**
     * 新規フォロー関係を作成
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @return 新規Follow Entity
     * @throws IllegalArgumentException 自分自身をフォローしようとした場合
     */
    public static Follow create(UserId followerUserId, UserId followedUserId) {
        return new Follow(
            FollowId.generate(),
            followerUserId,
            followedUserId,
            Instant.now()
        );
    }

    /**
     * 既存フォロー関係を再構築（リポジトリから取得時）
     * @param id フォローID
     * @param followerUserId フォローするユーザーのID
     * @param followedUserId フォローされるユーザーのID
     * @param createdAt 作成日時
     * @return 再構築されたFollow Entity
     */
    public static Follow reconstruct(FollowId id, UserId followerUserId, UserId followedUserId, Instant createdAt) {
        return new Follow(id, followerUserId, followedUserId, createdAt);
    }

    // Getters
    public FollowId getId() {
        return id;
    }

    public UserId getFollowerUserId() {
        return followerUserId;
    }

    public UserId getFollowedUserId() {
        return followedUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Follow follow = (Follow) o;
        return Objects.equals(id, follow.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Follow{" +
            "id=" + id +
            ", followerUserId=" + followerUserId +
            ", followedUserId=" + followedUserId +
            ", createdAt=" + createdAt +
            '}';
    }
}
