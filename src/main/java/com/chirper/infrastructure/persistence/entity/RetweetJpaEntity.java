package com.chirper.infrastructure.persistence.entity;

import com.chirper.domain.entity.Retweet;
import com.chirper.domain.valueobject.RetweetId;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * RetweetJpaEntity
 * retweetsテーブルにマッピングされるJPAエンティティ
 */
@Entity
@Table(name = "retweets")
public class RetweetJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "tweet_id", nullable = false)
    private UUID tweetId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // JPAのためのデフォルトコンストラクタ
    protected RetweetJpaEntity() {
    }

    // すべてのフィールドを設定するコンストラクタ
    public RetweetJpaEntity(UUID id, UUID userId, UUID tweetId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tweetId = tweetId;
        this.createdAt = createdAt;
    }

    /**
     * Domain EntityからJPA Entityへ変換
     * @param retweet Domain Entity
     * @return JPA Entity
     */
    public static RetweetJpaEntity fromDomainEntity(Retweet retweet) {
        return new RetweetJpaEntity(
            retweet.getId().value(),
            retweet.getUserId().value(),
            retweet.getTweetId().value(),
            retweet.getCreatedAt()
        );
    }

    /**
     * JPA EntityからDomain Entityへ変換
     * @return Domain Entity
     */
    public Retweet toDomainEntity() {
        return Retweet.reconstruct(
            new RetweetId(id),
            new UserId(userId),
            new TweetId(tweetId),
            createdAt
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getTweetId() {
        return tweetId;
    }

    public void setTweetId(UUID tweetId) {
        this.tweetId = tweetId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
