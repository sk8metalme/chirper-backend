package com.chirper.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Username Value Object Tests")
class UsernameTest {

    @Nested
    @DisplayName("正常系テスト")
    class ValidTests {

        @Test
        @DisplayName("有効なユーザー名で作成できる")
        void shouldCreateWithValidUsername() {
            // Given
            String validUsername = "testuser";

            // When
            Username username = new Username(validUsername);

            // Then
            assertNotNull(username);
            assertEquals("testuser", username.value());
        }

        @Test
        @DisplayName("3文字のユーザー名で作成できる")
        void shouldCreateWithMinLengthUsername() {
            // Given/When
            Username username = new Username("abc");

            // Then
            assertEquals("abc", username.value());
        }

        @Test
        @DisplayName("20文字のユーザー名で作成できる")
        void shouldCreateWithMaxLengthUsername() {
            // Given/When
            Username username = new Username("12345678901234567890");

            // Then
            assertEquals("12345678901234567890", username.value());
        }

        @Test
        @DisplayName("前後の空白はトリムされる")
        void shouldTrimWhitespace() {
            // Given/When
            Username username = new Username("  testuser  ");

            // Then
            assertEquals("testuser", username.value());
        }
    }

    @Nested
    @DisplayName("異常系: エッジケーステスト")
    class EdgeCaseTests {

        @Test
        @DisplayName("nullのユーザー名で例外が発生する")
        void shouldThrowExceptionWhenNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Username(null),
                "Username cannot be null or blank");
        }

        @Test
        @DisplayName("空文字のユーザー名で例外が発生する")
        void shouldThrowExceptionWhenEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Username(""),
                "Username cannot be null or blank");
        }

        @ParameterizedTest
        @DisplayName("空白文字のみのユーザー名で例外が発生する")
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
        void shouldThrowExceptionWhenBlank(String blankUsername) {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Username(blankUsername),
                "Username cannot be null or blank");
        }

        @Test
        @DisplayName("2文字のユーザー名で例外が発生する（最小長未満）")
        void shouldThrowExceptionWhenTooShort() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Username("ab"));
        }

        @Test
        @DisplayName("21文字のユーザー名で例外が発生する（最大長超過）")
        void shouldThrowExceptionWhenTooLong() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Username("123456789012345678901"));
        }

        @Test
        @DisplayName("トリム後に最小長未満になる場合は例外が発生する")
        void shouldThrowExceptionWhenTrimmedTooShort() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Username("  ab  "));
        }
    }

    @Nested
    @DisplayName("Equals/HashCode テスト")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("同じ値を持つUsernameは等価である")
        void shouldBeEqualWithSameValue() {
            // Given
            Username username1 = new Username("testuser");
            Username username2 = new Username("testuser");

            // When/Then
            assertEquals(username1, username2);
            assertEquals(username1.hashCode(), username2.hashCode());
        }

        @Test
        @DisplayName("異なる値を持つUsernameは等価でない")
        void shouldNotBeEqualWithDifferentValue() {
            // Given
            Username username1 = new Username("testuser");
            Username username2 = new Username("otheruser");

            // When/Then
            assertNotEquals(username1, username2);
        }
    }

    @Nested
    @DisplayName("toString テスト")
    class ToStringTests {

        @Test
        @DisplayName("toStringは値を返す")
        void shouldReturnValueInToString() {
            // Given
            Username username = new Username("testuser");

            // When/Then
            assertEquals("testuser", username.toString());
        }
    }
}
