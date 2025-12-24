package com.chirper.domain.entity;

import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

/**
 * Tweet Entity
 * ツイートのビジネスロジックとビジネスルールをカプセル化するドメインエンティティ
 */
public class Tweet {
    private final TweetId id;
    private final UserId userId;
    private final TweetContent content;
    private boolean isDeleted;
    private final Instant createdAt;
    private Instant updatedAt;

    private Tweet(TweetId id, UserId userId, TweetContent content, boolean isDeleted,
                  Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "TweetId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.content = Objects.requireNonNull(content, "TweetContent cannot be null");
        this.isDeleted = isDeleted;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    /**
     * 新規ツイートを作成
     * @param userId 投稿者のユーザーID
     * @param content ツイート本文
     * @return 新規Tweet Entity
     */
    public static Tweet create(UserId userId, TweetContent content) {
        Instant now = Instant.now();
        return new Tweet(
            TweetId.generate(),
            userId,
            content,
            false,
            now,
            now
        );
    }

    /**
     * 既存ツイートを再構築（リポジトリから取得時）
     * @param id ツイートID
     * @param userId 投稿者のユーザーID
     * @param content ツイート本文
     * @param isDeleted 論理削除フラグ
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 再構築されたTweet Entity
     */
    public static Tweet reconstruct(TweetId id, UserId userId, TweetContent content,
                                     boolean isDeleted, Instant createdAt, Instant updatedAt) {
        return new Tweet(id, userId, content, isDeleted, createdAt, updatedAt);
    }

    /**
     * ツイートを論理削除
     * ビジネスルール: 投稿者本人のみが削除可能
     * @param requestingUserId 削除リクエストを送信したユーザーID
     * @throws IllegalStateException 既に削除済みの場合
     * @throws SecurityException 投稿者以外が削除しようとした場合
     */
    public void delete(UserId requestingUserId) {
        if (this.isDeleted) {
            throw new IllegalStateException("Tweet is already deleted");
        }

        if (!this.userId.equals(requestingUserId)) {
            throw new SecurityException("Only the tweet author can delete this tweet");
        }

        this.isDeleted = true;
        this.updatedAt = Instant.now();
    }

    /**
     * ツイートが削除されているか確認
     * @return 削除済みの場合true
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * 指定されたユーザーがこのツイートの投稿者か確認
     * @param userId 確認するユーザーID
     * @return 投稿者の場合true
     */
    public boolean isAuthor(UserId userId) {
        return this.userId.equals(userId);
    }

    // Getters
    public TweetId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public TweetContent getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tweet tweet = (Tweet) o;
        return Objects.equals(id, tweet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tweet{" +
            "id=" + id +
            ", userId=" + userId +
            ", content=" + content +
            ", isDeleted=" + isDeleted +
            ", createdAt=" + createdAt +
            '}';
    }
}
