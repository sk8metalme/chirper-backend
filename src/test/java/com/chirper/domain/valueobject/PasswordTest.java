package com.chirper.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Value Object Tests")
class PasswordTest {

    @Nested
    @DisplayName("正常系テスト")
    class ValidTests {

        @Test
        @DisplayName("平文パスワードからPasswordを作成できる")
        void shouldCreateFromPlainText() {
            // Given
            String plainPassword = "password123";

            // When
            Password password = Password.fromPlainText(plainPassword);

            // Then
            assertNotNull(password);
            assertNotNull(password.hashedValue());
            assertNotEquals(plainPassword, password.hashedValue());
            assertTrue(password.hashedValue().startsWith("$2a$"));
        }

        @Test
        @DisplayName("正しいパスワードで検証が成功する")
        void shouldMatchWithCorrectPassword() {
            // Given
            String plainPassword = "password123";
            Password password = Password.fromPlainText(plainPassword);

            // When
            boolean matches = password.matches(plainPassword);

            // Then
            assertTrue(matches);
        }

        @Test
        @DisplayName("誤ったパスワードで検証が失敗する")
        void shouldNotMatchWithWrongPassword() {
            // Given
            String plainPassword = "password123";
            Password password = Password.fromPlainText(plainPassword);

            // When
            boolean matches = password.matches("wrongpassword");

            // Then
            assertFalse(matches);
        }

        @Test
        @DisplayName("toStringはハッシュ値を露出しない")
        void shouldNotExposeHashInToString() {
            // Given
            Password password = Password.fromPlainText("password123");

            // When/Then
            assertEquals("[PROTECTED]", password.toString());
            assertNotEquals(password.hashedValue(), password.toString());
        }
    }

    @Nested
    @DisplayName("異常系: エッジケーステスト - fromPlainText")
    class FromPlainTextEdgeCaseTests {

        @Test
        @DisplayName("nullの平文パスワードで例外が発生する")
        void shouldThrowExceptionWhenPlainPasswordIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> Password.fromPlainText(null),
                "Plain password cannot be null or blank");
        }

        @Test
        @DisplayName("空文字の平文パスワードで例外が発生する")
        void shouldThrowExceptionWhenPlainPasswordIsEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> Password.fromPlainText(""),
                "Plain password cannot be null or blank");
        }

        @ParameterizedTest
        @DisplayName("空白文字のみの平文パスワードで例外が発生する")
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
        void shouldThrowExceptionWhenPlainPasswordIsBlank(String blankPassword) {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> Password.fromPlainText(blankPassword),
                "Plain password cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("異常系: エッジケーステスト - コンストラクタ")
    class ConstructorEdgeCaseTests {

        @Test
        @DisplayName("nullのハッシュ値で例外が発生する")
        void shouldThrowExceptionWhenHashIsNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Password(null),
                "Password hash cannot be null or blank");
        }

        @Test
        @DisplayName("空文字のハッシュ値で例外が発生する")
        void shouldThrowExceptionWhenHashIsEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Password(""),
                "Password hash cannot be null or blank");
        }

        @ParameterizedTest
        @DisplayName("空白文字のみのハッシュ値で例外が発生する")
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
        void shouldThrowExceptionWhenHashIsBlank(String blankHash) {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Password(blankHash),
                "Password hash cannot be null or blank");
        }
    }

    @Nested
    @DisplayName("matches メソッドテスト")
    class MatchesTests {

        @Test
        @DisplayName("nullのパスワードで検証が失敗する")
        void shouldReturnFalseWhenMatchingNull() {
            // Given
            Password password = Password.fromPlainText("password123");

            // When
            boolean matches = password.matches(null);

            // Then
            assertFalse(matches);
        }

        @Test
        @DisplayName("空文字のパスワードで検証が失敗する")
        void shouldReturnFalseWhenMatchingEmpty() {
            // Given
            Password password = Password.fromPlainText("password123");

            // When
            boolean matches = password.matches("");

            // Then
            assertFalse(matches);
        }
    }

    @Nested
    @DisplayName("Equals/HashCode テスト")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("同じハッシュ値を持つPasswordは等価である")
        void shouldBeEqualWithSameHash() {
            // Given
            String hash = "$2a$10$abcdefghijklmnopqrstuvwxyz1234567890";
            Password password1 = new Password(hash);
            Password password2 = new Password(hash);

            // When/Then
            assertEquals(password1, password2);
            assertEquals(password1.hashCode(), password2.hashCode());
        }

        @Test
        @DisplayName("異なるハッシュ値を持つPasswordは等価でない")
        void shouldNotBeEqualWithDifferentHash() {
            // Given
            Password password1 = Password.fromPlainText("password123");
            Password password2 = Password.fromPlainText("password123");

            // When/Then
            // bcryptは同じパスワードでも異なるハッシュを生成する（ソルト付き）
            assertNotEquals(password1, password2);
        }
    }
}
