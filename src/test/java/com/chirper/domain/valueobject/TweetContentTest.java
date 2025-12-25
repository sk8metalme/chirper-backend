package com.chirper.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TweetContent Value Object Tests")
class TweetContentTest {

    @Nested
    @DisplayName("正常系テスト")
    class ValidTests {

        @Test
        @DisplayName("有効なツイート内容で作成できる")
        void shouldCreateWithValidContent() {
            // Given
            String validContent = "Hello, World!";

            // When
            TweetContent content = new TweetContent(validContent);

            // Then
            assertNotNull(content);
            assertEquals("Hello, World!", content.value());
        }

        @Test
        @DisplayName("1文字のツイート内容で作成できる")
        void shouldCreateWithMinLengthContent() {
            // Given/When
            TweetContent content = new TweetContent("a");

            // Then
            assertEquals("a", content.value());
        }

        @Test
        @DisplayName("280文字のツイート内容で作成できる")
        void shouldCreateWithMaxLengthContent() {
            // Given
            String maxContent = "a".repeat(280);

            // When
            TweetContent content = new TweetContent(maxContent);

            // Then
            assertEquals(280, content.value().length());
        }

        @Test
        @DisplayName("前後の空白はトリムされる")
        void shouldTrimWhitespace() {
            // Given/When
            TweetContent content = new TweetContent("  Hello, World!  ");

            // Then
            assertEquals("Hello, World!", content.value());
        }
    }

    @Nested
    @DisplayName("異常系: エッジケーステスト")
    class EdgeCaseTests {

        @Test
        @DisplayName("nullのツイート内容で例外が発生する")
        void shouldThrowExceptionWhenNull() {
            // When
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TweetContent(null));

            // Then
            assertEquals("Tweet content cannot be null or blank", ex.getMessage());
        }

        @Test
        @DisplayName("空文字のツイート内容で例外が発生する")
        void shouldThrowExceptionWhenEmpty() {
            // When
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TweetContent(""));

            // Then
            assertEquals("Tweet content cannot be null or blank", ex.getMessage());
        }

        @ParameterizedTest
        @DisplayName("空白文字のみのツイート内容で例外が発生する")
        @ValueSource(strings = {" ", "  ", "\t", "\n", "   \t\n   "})
        void shouldThrowExceptionWhenBlank(String blankContent) {
            // When
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TweetContent(blankContent));

            // Then
            assertEquals("Tweet content cannot be null or blank", ex.getMessage());
        }

        @Test
        @DisplayName("281文字のツイート内容で例外が発生する（最大長超過）")
        void shouldThrowExceptionWhenTooLong() {
            // Given
            String tooLongContent = "a".repeat(281);

            // When
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TweetContent(tooLongContent));

            // Then
            assertTrue(ex.getMessage().contains("Tweet content must be between"));
        }

        @Test
        @DisplayName("トリム後に空文字になる場合は例外が発生する")
        void shouldThrowExceptionWhenTrimmedEmpty() {
            // When
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new TweetContent("     "));

            // Then
            assertEquals("Tweet content cannot be null or blank", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Equals/HashCode テスト")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("同じ値を持つTweetContentは等価である")
        void shouldBeEqualWithSameValue() {
            // Given
            TweetContent content1 = new TweetContent("Hello");
            TweetContent content2 = new TweetContent("Hello");

            // When/Then
            assertEquals(content1, content2);
            assertEquals(content1.hashCode(), content2.hashCode());
        }

        @Test
        @DisplayName("異なる値を持つTweetContentは等価でない")
        void shouldNotBeEqualWithDifferentValue() {
            // Given
            TweetContent content1 = new TweetContent("Hello");
            TweetContent content2 = new TweetContent("World");

            // When/Then
            assertNotEquals(content1, content2);
        }
    }

    @Nested
    @DisplayName("toString テスト")
    class ToStringTests {

        @Test
        @DisplayName("toStringは値を返す")
        void shouldReturnValueInToString() {
            // Given
            TweetContent content = new TweetContent("Hello");

            // When/Then
            assertEquals("Hello", content.toString());
        }
    }
}
