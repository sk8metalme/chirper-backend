package com.chirper.infrastructure.persistence.entity;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.valueobject.FollowId;
import com.chirper.domain.valueobject.UserId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * FollowJpaEntity
 * followsテーブルにマッピングされるJPAエンティティ
 */
@Entity
@Table(name = "follows")
public class FollowJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "follower_user_id", nullable = false)
    private UUID followerUserId;

    @Column(name = "followed_user_id", nullable = false)
    private UUID followedUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // JPAのためのデフォルトコンストラクタ
    protected FollowJpaEntity() {
    }

    // すべてのフィールドを設定するコンストラクタ
    public FollowJpaEntity(UUID id, UUID followerUserId, UUID followedUserId, Instant createdAt) {
        this.id = id;
        this.followerUserId = followerUserId;
        this.followedUserId = followedUserId;
        this.createdAt = createdAt;
    }

    /**
     * Domain EntityからJPA Entityへ変換
     * @param follow Domain Entity
     * @return JPA Entity
     */
    public static FollowJpaEntity fromDomainEntity(Follow follow) {
        return new FollowJpaEntity(
            follow.getId().value(),
            follow.getFollowerUserId().value(),
            follow.getFollowedUserId().value(),
            follow.getCreatedAt()
        );
    }

    /**
     * JPA EntityからDomain Entityへ変換
     * @return Domain Entity
     */
    public Follow toDomainEntity() {
        return Follow.reconstruct(
            new FollowId(id),
            new UserId(followerUserId),
            new UserId(followedUserId),
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

    public UUID getFollowerUserId() {
        return followerUserId;
    }

    public void setFollowerUserId(UUID followerUserId) {
        this.followerUserId = followerUserId;
    }

    public UUID getFollowedUserId() {
        return followedUserId;
    }

    public void setFollowedUserId(UUID followedUserId) {
        this.followedUserId = followedUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
