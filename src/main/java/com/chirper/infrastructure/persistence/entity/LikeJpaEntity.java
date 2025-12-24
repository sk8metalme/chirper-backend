package com.chirper.infrastructure.persistence.entity;

import com.chirper.domain.entity.Like;
import com.chirper.domain.valueobject.LikeId;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * LikeJpaEntity
 * likesテーブルにマッピングされるJPAエンティティ
 */
@Entity
@Table(name = "likes")
public class LikeJpaEntity {

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
    protected LikeJpaEntity() {
    }

    // すべてのフィールドを設定するコンストラクタ
    public LikeJpaEntity(UUID id, UUID userId, UUID tweetId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tweetId = tweetId;
        this.createdAt = createdAt;
    }

    /**
     * Domain EntityからJPA Entityへ変換
     * @param like Domain Entity
     * @return JPA Entity
     */
    public static LikeJpaEntity fromDomainEntity(Like like) {
        return new LikeJpaEntity(
            like.getId().value(),
            like.getUserId().value(),
            like.getTweetId().value(),
            like.getCreatedAt()
        );
    }

    /**
     * JPA EntityからDomain Entityへ変換
     * @return Domain Entity
     */
    public Like toDomainEntity() {
        return Like.reconstruct(
            new LikeId(id),
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
