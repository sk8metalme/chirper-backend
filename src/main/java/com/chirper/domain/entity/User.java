package com.chirper.domain.entity;

import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.Password;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;

import java.time.Instant;
import java.util.Objects;

/**
 * User Entity
 * ユーザーのビジネスロジックとビジネスルールをカプセル化するドメインエンティティ
 */
public class User {
    private final UserId id;
    private final Username username;
    private final Email email;
    private final Password password;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UserId id, Username username, Email email, Password password,
                 String displayName, String bio, String avatarUrl,
                 Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "UserId cannot be null");
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    /**
     * 新規ユーザーを作成（ユーザー登録時）
     * @param username ユーザー名
     * @param email メールアドレス
     * @param plainPassword 平文パスワード
     * @return 新規User Entity
     */
    public static User create(Username username, Email email, String plainPassword) {
        Password hashedPassword = Password.fromPlainText(plainPassword);
        Instant now = Instant.now();
        return new User(
            UserId.generate(),
            username,
            email,
            hashedPassword,
            null,
            null,
            null,
            now,
            now
        );
    }

    /**
     * 既存ユーザーを再構築（リポジトリから取得時）
     * @param id ユーザーID
     * @param username ユーザー名
     * @param email メールアドレス
     * @param password パスワードハッシュ
     * @param displayName 表示名
     * @param bio 自己紹介
     * @param avatarUrl アバターURL
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     * @return 再構築されたUser Entity
     */
    public static User reconstruct(UserId id, Username username, Email email, Password password,
                                    String displayName, String bio, String avatarUrl,
                                    Instant createdAt, Instant updatedAt) {
        return new User(id, username, email, password, displayName, bio, avatarUrl, createdAt, updatedAt);
    }

    /**
     * プロフィール情報を更新
     * @param displayName 表示名（オプション）
     * @param bio 自己紹介（オプション）
     * @param avatarUrl アバターURL（オプション）
     */
    public void updateProfile(String displayName, String bio, String avatarUrl) {
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.updatedAt = Instant.now();
    }

    /**
     * パスワードが一致するか検証
     * @param plainPassword 平文パスワード
     * @return 一致する場合true
     */
    public boolean verifyPassword(String plainPassword) {
        return password.matches(plainPassword);
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
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
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username=" + username +
            ", email=" + email +
            ", displayName='" + displayName + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
