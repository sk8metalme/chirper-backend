package com.chirper.infrastructure.persistence.entity;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * TweetJpaEntity
 * tweetsテーブルにマッピングされるJPAエンティティ
 */
@Entity
@Table(name = "tweets")
public class TweetJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // JPAのためのデフォルトコンストラクタ
    protected TweetJpaEntity() {
    }

    // すべてのフィールドを設定するコンストラクタ
    public TweetJpaEntity(UUID id, UUID userId, String content, boolean isDeleted,
                          Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Domain EntityからJPA Entityへ変換
     * @param tweet Domain Entity
     * @return JPA Entity
     */
    public static TweetJpaEntity fromDomainEntity(Tweet tweet) {
        return new TweetJpaEntity(
            tweet.getId().value(),
            tweet.getUserId().value(),
            tweet.getContent().value(),
            tweet.isDeleted(),
            tweet.getCreatedAt(),
            tweet.getUpdatedAt()
        );
    }

    /**
     * JPA EntityからDomain Entityへ変換
     * @return Domain Entity
     */
    public Tweet toDomainEntity() {
        return Tweet.reconstruct(
            new TweetId(id),
            new UserId(userId),
            new TweetContent(content),
            isDeleted,
            createdAt,
            updatedAt
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
