package com.chirper.domain.service;

import com.chirper.TestConstants;
import com.chirper.domain.entity.User;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(TestConstants.Authentication.JWT_SECRET);
    }

    @Nested
    @DisplayName("コンストラクタテスト")
    class ConstructorTests {

        @Test
        @DisplayName("正常なシークレットキーでインスタンス化できる")
        void shouldCreateInstanceWithValidSecret() {
            // When/Then
            assertDoesNotThrow(() -> new AuthenticationService(TestConstants.Authentication.JWT_SECRET));
        }

        @Test
        @DisplayName("nullのシークレットキーで例外が発生する")
        void shouldThrowExceptionWithNullSecret() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new AuthenticationService(null));
        }

        @Test
        @DisplayName("空のシークレットキーで例外が発生する")
        void shouldThrowExceptionWithBlankSecret() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new AuthenticationService("   "));
        }

        @Test
        @DisplayName("32バイト未満のシークレットキーで例外が発生する")
        void shouldThrowExceptionWithShortSecret() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new AuthenticationService("short-key"));
        }
    }

    @Nested
    @DisplayName("パスワード認証テスト")
    class AuthenticateTests {

        @Test
        @DisplayName("正しいパスワードで認証成功")
        void shouldAuthenticateWithCorrectPassword() {
            // Given
            String plainPassword = "password123";
            User user = User.create(
                new Username("testuser"),
                new Email("test@example.com"),
                plainPassword
            );

            // When
            boolean result = authenticationService.authenticate(user, plainPassword);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("誤ったパスワードで認証失敗")
        void shouldFailAuthenticationWithWrongPassword() {
            // Given
            String plainPassword = "password123";
            User user = User.create(
                new Username("testuser"),
                new Email("test@example.com"),
                plainPassword
            );

            // When
            boolean result = authenticationService.authenticate(user, "wrongpassword");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("nullのユーザーで認証失敗")
        void shouldFailAuthenticationWithNullUser() {
            // When
            boolean result = authenticationService.authenticate(null, "password123");

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("nullのパスワードで認証失敗")
        void shouldFailAuthenticationWithNullPassword() {
            // Given
            User user = User.create(
                new Username("testuser"),
                new Email("test@example.com"),
                "password123"
            );

            // When
            boolean result = authenticationService.authenticate(user, null);

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("JWT生成テスト")
    class GenerateJwtTokenTests {

        @Test
        @DisplayName("有効なJWTトークンが生成される")
        void shouldGenerateValidJwtToken() {
            // Given
            UserId userId = UserId.generate();

            // When
            String token = authenticationService.generateJwtToken(userId);

            // Then
            assertNotNull(token);
            assertFalse(token.isBlank());
            // JWTは3つのパート（header.payload.signature）で構成される
            assertEquals(3, token.split("\\.").length);
        }

        @Test
        @DisplayName("同じユーザーIDでも異なる時刻で異なるトークンが生成される")
        void shouldGenerateDifferentTokensForSameUserIdAtDifferentTimes() {
            // Given
            UserId userId = UserId.generate();

            // 最初のトークンを生成
            java.time.Instant firstTime = java.time.Instant.parse("2024-01-01T00:00:00Z");
            java.time.Clock fixedClock1 = java.time.Clock.fixed(firstTime, java.time.ZoneId.of("UTC"));
            AuthenticationService service1 = new AuthenticationService(TestConstants.Authentication.JWT_SECRET, fixedClock1);
            String token1 = service1.generateJwtToken(userId);

            // 2番目のトークンを異なる時刻で生成
            java.time.Instant secondTime = java.time.Instant.parse("2024-01-01T01:00:00Z");
            java.time.Clock fixedClock2 = java.time.Clock.fixed(secondTime, java.time.ZoneId.of("UTC"));
            AuthenticationService service2 = new AuthenticationService(TestConstants.Authentication.JWT_SECRET, fixedClock2);
            String token2 = service2.generateJwtToken(userId);

            // Then
            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("JWT検証テスト")
    class ValidateJwtTokenTests {

        @Test
        @DisplayName("有効なJWTトークンから UserID を取得できる")
        void shouldValidateValidJwtToken() {
            // Given
            UserId userId = UserId.generate();
            String token = authenticationService.generateJwtToken(userId);

            // When
            UserId result = authenticationService.validateJwtToken(token);

            // Then
            assertNotNull(result);
            assertEquals(userId, result);
        }

        @Test
        @DisplayName("nullのトークンはnullを返す")
        void shouldReturnNullForNullToken() {
            // When
            UserId result = authenticationService.validateJwtToken(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("空のトークンはnullを返す")
        void shouldReturnNullForBlankToken() {
            // When
            UserId result = authenticationService.validateJwtToken("   ");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("不正なフォーマットのトークンはnullを返す")
        void shouldReturnNullForInvalidFormatToken() {
            // When
            UserId result = authenticationService.validateJwtToken("invalid.token");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("異なるシークレットで署名されたトークンはnullを返す")
        void shouldReturnNullForTokenWithDifferentSecret() {
            // Given
            AuthenticationService otherService = new AuthenticationService(
                TestConstants.Authentication.DIFFERENT_JWT_SECRET
            );
            UserId userId = UserId.generate();
            String token = otherService.generateJwtToken(userId);

            // When
            UserId result = authenticationService.validateJwtToken(token);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("有効期限取得テスト")
    class GetExpirationTimeTests {

        @Test
        @DisplayName("有効なトークンの有効期限を取得できる")
        void shouldGetExpirationTimeForValidToken() {
            // Given
            UserId userId = UserId.generate();
            String token = authenticationService.generateJwtToken(userId);
            Instant beforeGeneration = Instant.now();

            // When
            Instant expirationTime = authenticationService.getExpirationTime(token);

            // Then
            assertNotNull(expirationTime);
            // 有効期限は現在時刻から1時間後付近
            assertTrue(expirationTime.isAfter(beforeGeneration));
            assertTrue(expirationTime.isBefore(beforeGeneration.plusSeconds(3700))); // 1時間 + バッファ
        }

        @Test
        @DisplayName("nullのトークンはnullを返す")
        void shouldReturnNullForNullToken() {
            // When
            Instant result = authenticationService.getExpirationTime(null);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("空のトークンはnullを返す")
        void shouldReturnNullForBlankToken() {
            // When
            Instant result = authenticationService.getExpirationTime("   ");

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("不正なトークンはnullを返す")
        void shouldReturnNullForInvalidToken() {
            // When
            Instant result = authenticationService.getExpirationTime("invalid.token");

            // Then
            assertNull(result);
        }
    }
}
