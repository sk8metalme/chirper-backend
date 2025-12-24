package com.chirper.infrastructure.persistence.entity;

import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.Password;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * UserJpaEntity
 * usersテーブルにマッピングされるJPAエンティティ
 * Domain EntityとJPA Entityを分離することで、永続化層とドメイン層の独立性を保つ
 */
@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // JPAのためのデフォルトコンストラクタ
    protected UserJpaEntity() {
    }

    // すべてのフィールドを設定するコンストラクタ
    public UserJpaEntity(UUID id, String username, String email, String passwordHash,
                         String displayName, String bio, String avatarUrl,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Domain EntityからJPA Entityへ変換
     * @param user Domain Entity
     * @return JPA Entity
     */
    public static UserJpaEntity fromDomainEntity(User user) {
        return new UserJpaEntity(
            user.getId().value(),
            user.getUsername().value(),
            user.getEmail().value(),
            user.getPassword().hashedValue(),
            user.getDisplayName(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    /**
     * JPA EntityからDomain Entityへ変換
     * @return Domain Entity
     */
    public User toDomainEntity() {
        return User.reconstruct(
            new UserId(id),
            new Username(username),
            new Email(email),
            new Password(passwordHash),
            displayName,
            bio,
            avatarUrl,
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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
