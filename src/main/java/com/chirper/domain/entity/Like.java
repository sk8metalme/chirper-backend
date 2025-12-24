package com.chirper.domain.entity;

import com.chirper.domain.valueobject.LikeId;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Like Entity
 * いいねのビジネスロジックとビジネスルールをカプセル化するドメインエンティティ
 */
public class Like {
    private final LikeId id;
    private final UserId userId;
    private final TweetId tweetId;
    private final Instant createdAt;

    private Like(LikeId id, UserId userId, TweetId tweetId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "LikeId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.tweetId = Objects.requireNonNull(tweetId, "TweetId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    /**
     * 新規いいねを作成
     * @param userId いいねをするユーザーのID
     * @param tweetId いいね対象のツイートID
     * @return 新規Like Entity
     */
    public static Like create(UserId userId, TweetId tweetId) {
        return new Like(
            LikeId.generate(),
            userId,
            tweetId,
            Instant.now()
        );
    }

    /**
     * 既存いいねを再構築（リポジトリから取得時）
     * @param id いいねID
     * @param userId いいねをするユーザーのID
     * @param tweetId いいね対象のツイートID
     * @param createdAt 作成日時
     * @return 再構築されたLike Entity
     */
    public static Like reconstruct(LikeId id, UserId userId, TweetId tweetId, Instant createdAt) {
        return new Like(id, userId, tweetId, createdAt);
    }

    // Getters
    public LikeId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public TweetId getTweetId() {
        return tweetId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Like like = (Like) o;
        return Objects.equals(id, like.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Like{" +
            "id=" + id +
            ", userId=" + userId +
            ", tweetId=" + tweetId +
            ", createdAt=" + createdAt +
            '}';
    }
}
