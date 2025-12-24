package com.chirper.domain.entity;

import com.chirper.domain.valueobject.RetweetId;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Retweet Entity
 * リツイートのビジネスロジックとビジネスルールをカプセル化するドメインエンティティ
 */
public class Retweet {
    private final RetweetId id;
    private final UserId userId;
    private final TweetId tweetId;
    private final Instant createdAt;

    private Retweet(RetweetId id, UserId userId, TweetId tweetId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "RetweetId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.tweetId = Objects.requireNonNull(tweetId, "TweetId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    /**
     * 新規リツイートを作成
     * @param userId リツイートするユーザーのID
     * @param tweetId リツイート対象のツイートID
     * @return 新規Retweet Entity
     */
    public static Retweet create(UserId userId, TweetId tweetId) {
        return new Retweet(
            RetweetId.generate(),
            userId,
            tweetId,
            Instant.now()
        );
    }

    /**
     * 既存リツイートを再構築（リポジトリから取得時）
     * @param id リツイートID
     * @param userId リツイートするユーザーのID
     * @param tweetId リツイート対象のツイートID
     * @param createdAt 作成日時
     * @return 再構築されたRetweet Entity
     */
    public static Retweet reconstruct(RetweetId id, UserId userId, TweetId tweetId, Instant createdAt) {
        return new Retweet(id, userId, tweetId, createdAt);
    }

    // Getters
    public RetweetId getId() {
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
        Retweet retweet = (Retweet) o;
        return Objects.equals(id, retweet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Retweet{" +
            "id=" + id +
            ", userId=" + userId +
            ", tweetId=" + tweetId +
            ", createdAt=" + createdAt +
            '}';
    }
}
