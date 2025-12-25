package com.chirper.domain.entity;

import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.Password;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Nested
    @DisplayName("ユーザー作成テスト")
    class CreateTests {

        @Test
        @DisplayName("正常なユーザー作成ができる")
        void shouldCreateUserSuccessfully() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            String plainPassword = "password123";

            // When
            User user = User.create(username, email, plainPassword);

            // Then
            assertNotNull(user);
            assertNotNull(user.getId());
            assertEquals(username, user.getUsername());
            assertEquals(email, user.getEmail());
            assertNotNull(user.getPassword());
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
            assertNull(user.getDisplayName());
            assertNull(user.getBio());
            assertNull(user.getAvatarUrl());
        }

        @Test
        @DisplayName("パスワードが自動的にハッシュ化される")
        void shouldHashPasswordAutomatically() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            String plainPassword = "password123";

            // When
            User user = User.create(username, email, plainPassword);

            // Then
            assertNotNull(user.getPassword());
            // ハッシュ化されたパスワードは平文と異なる
            assertNotEquals(plainPassword, user.getPassword().hashedValue());
            // bcryptハッシュは$2aで始まる
            assertTrue(user.getPassword().hashedValue().startsWith("$2a$"));
        }
    }

    @Nested
    @DisplayName("パスワード検証テスト")
    class PasswordVerificationTests {

        @Test
        @DisplayName("正しいパスワードで検証が成功する")
        void shouldVerifyCorrectPassword() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            String plainPassword = "password123";
            User user = User.create(username, email, plainPassword);

            // When
            boolean result = user.verifyPassword(plainPassword);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("誤ったパスワードで検証が失敗する")
        void shouldFailVerificationWithWrongPassword() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            String plainPassword = "password123";
            User user = User.create(username, email, plainPassword);

            // When
            boolean result = user.verifyPassword("wrongpassword");

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("プロフィール更新テスト")
    class UpdateProfileTests {

        @Test
        @DisplayName("プロフィール情報を更新できる")
        void shouldUpdateProfileSuccessfully() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            User user = User.create(username, email, "password123");
            String displayName = "Test User";
            String bio = "This is my bio";
            String avatarUrl = "https://example.com/avatar.jpg";

            // When
            user.updateProfile(displayName, bio, avatarUrl);

            // Then
            assertEquals(displayName, user.getDisplayName());
            assertEquals(bio, user.getBio());
            assertEquals(avatarUrl, user.getAvatarUrl());
        }

        @Test
        @DisplayName("プロフィール更新時にupdatedAtが更新される")
        void shouldUpdateUpdatedAtWhenProfileIsUpdated() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            User user = User.create(username, email, "password123");
            var originalUpdatedAt = user.getUpdatedAt();
            var newUpdatedAt = originalUpdatedAt.plusSeconds(1);

            // When
            user.updateProfile("New Name", "New Bio", "https://example.com/new.jpg", newUpdatedAt);

            // Then
            assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
            assertEquals(newUpdatedAt, user.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Equals/HashCode テスト")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("同じIDを持つユーザーは等価である")
        void shouldBeEqualWithSameId() {
            // Given
            UserId userId = UserId.generate();
            Username username1 = new Username("user1");
            Username username2 = new Username("user2");
            Email email1 = new Email("user1@example.com");
            Email email2 = new Email("user2@example.com");
            Password password = Password.fromPlainText("password123");

            User user1 = User.reconstruct(userId, username1, email1, password,
                "Name1", "Bio1", "url1", java.time.Instant.now(), java.time.Instant.now());
            User user2 = User.reconstruct(userId, username2, email2, password,
                "Name2", "Bio2", "url2", java.time.Instant.now(), java.time.Instant.now());

            // When/Then
            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("異なるIDを持つユーザーは等価でない")
        void shouldNotBeEqualWithDifferentId() {
            // Given
            Username username = new Username("testuser");
            Email email = new Email("test@example.com");
            Password password = Password.fromPlainText("password123");

            User user1 = User.reconstruct(UserId.generate(), username, email, password,
                null, null, null, java.time.Instant.now(), java.time.Instant.now());
            User user2 = User.reconstruct(UserId.generate(), username, email, password,
                null, null, null, java.time.Instant.now(), java.time.Instant.now());

            // When/Then
            assertNotEquals(user1, user2);
        }
    }
}
