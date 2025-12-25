package com.chirper.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email Value Object Tests")
class EmailTest {

    @Nested
    @DisplayName("正常系テスト")
    class ValidTests {

        @ParameterizedTest
        @DisplayName("有効なメールアドレスで作成できる")
        @ValueSource(strings = {
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.jp",
            "test_user@sub.example.com",
            "123@example.com"
        })
        void shouldCreateWithValidEmail(String validEmail) {
            // When
            Email email = new Email(validEmail);

            // Then
            assertNotNull(email);
            assertEquals(validEmail, email.value());
        }

        @Test
        @DisplayName("前後の空白はトリムされる")
        void shouldTrimWhitespace() {
            // Given/When
            Email email = new Email("  test@example.com  ");

            // Then
            assertEquals("test@example.com", email.value());
        }
    }

    @Nested
    @DisplayName("異常系: エッジケーステスト")
    class EdgeCaseTests {

        @Test
        @DisplayName("nullのメールアドレスで例外が発生する")
        void shouldThrowExceptionWhenNull() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Email(null),
                "Email cannot be null or blank");
        }

        @Test
        @DisplayName("空文字のメールアドレスで例外が発生する")
        void shouldThrowExceptionWhenEmpty() {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Email(""),
                "Email cannot be null or blank");
        }

        @ParameterizedTest
        @DisplayName("空白文字のみのメールアドレスで例外が発生する")
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
        void shouldThrowExceptionWhenBlank(String blankEmail) {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Email(blankEmail),
                "Email cannot be null or blank");
        }

        @ParameterizedTest
        @DisplayName("無効な形式のメールアドレスで例外が発生する")
        @ValueSource(strings = {
            "invalid",
            "@example.com",
            "user@",
            "user@@example.com",
            "user@example",
            "user name@example.com",
            "user@example .com"
        })
        void shouldThrowExceptionWhenInvalidFormat(String invalidEmail) {
            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> new Email(invalidEmail));
        }
    }

    @Nested
    @DisplayName("Equals/HashCode テスト")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("同じ値を持つEmailは等価である")
        void shouldBeEqualWithSameValue() {
            // Given
            Email email1 = new Email("test@example.com");
            Email email2 = new Email("test@example.com");

            // When/Then
            assertEquals(email1, email2);
            assertEquals(email1.hashCode(), email2.hashCode());
        }

        @Test
        @DisplayName("異なる値を持つEmailは等価でない")
        void shouldNotBeEqualWithDifferentValue() {
            // Given
            Email email1 = new Email("test@example.com");
            Email email2 = new Email("other@example.com");

            // When/Then
            assertNotEquals(email1, email2);
        }
    }

    @Nested
    @DisplayName("toString テスト")
    class ToStringTests {

        @Test
        @DisplayName("toStringは値を返す")
        void shouldReturnValueInToString() {
            // Given
            Email email = new Email("test@example.com");

            // When/Then
            assertEquals("test@example.com", email.toString());
        }
    }
}
