package com.chirper.domain.entity;

import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tweet Entity Tests")
class TweetTest {

    @Nested
    @DisplayName("ツイート作成テスト")
    class CreateTests {

        @Test
        @DisplayName("正常なツイート作成ができる")
        void shouldCreateTweetSuccessfully() {
            // Given
            UserId userId = UserId.generate();
            TweetContent content = new TweetContent("Hello, World!");

            // When
            Tweet tweet = Tweet.create(userId, content);

            // Then
            assertNotNull(tweet);
            assertNotNull(tweet.getId());
            assertEquals(userId, tweet.getUserId());
            assertEquals(content, tweet.getContent());
            assertFalse(tweet.isDeleted());
            assertNotNull(tweet.getCreatedAt());
            assertNotNull(tweet.getUpdatedAt());
        }

        @Test
        @DisplayName("新規作成時はis_deletedがfalse")
        void shouldHaveIsDeletedFalseWhenCreated() {
            // Given
            UserId userId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");

            // When
            Tweet tweet = Tweet.create(userId, content);

            // Then
            assertFalse(tweet.isDeleted());
        }
    }

    @Nested
    @DisplayName("ツイート削除テスト")
    class DeleteTests {

        @Test
        @DisplayName("投稿者本人が削除できる")
        void shouldAllowAuthorToDelete() {
            // Given
            UserId authorId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");
            Tweet tweet = Tweet.create(authorId, content);

            // When
            tweet.delete(authorId);

            // Then
            assertTrue(tweet.isDeleted());
        }

        @Test
        @DisplayName("投稿者以外は削除できない")
        void shouldNotAllowNonAuthorToDelete() {
            // Given
            UserId authorId = UserId.generate();
            UserId otherUserId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");
            Tweet tweet = Tweet.create(authorId, content);

            // When/Then
            assertThrows(SecurityException.class, () -> tweet.delete(otherUserId));
            assertFalse(tweet.isDeleted());
        }

        @Test
        @DisplayName("既に削除済みのツイートは再削除できない")
        void shouldNotAllowDeletingAlreadyDeletedTweet() {
            // Given
            UserId authorId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");
            Tweet tweet = Tweet.create(authorId, content);
            tweet.delete(authorId);

            // When/Then
            assertThrows(IllegalStateException.class, () -> tweet.delete(authorId));
        }

        @Test
        @DisplayName("削除時にupdatedAtが更新される")
        void shouldUpdateUpdatedAtWhenDeleted() {
            // Given
            UserId authorId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");
            Tweet tweet = Tweet.create(authorId, content);
            var originalUpdatedAt = tweet.getUpdatedAt();
            var newUpdatedAt = originalUpdatedAt.plusSeconds(1);

            // When
            tweet.delete(authorId, newUpdatedAt);

            // Then
            assertTrue(tweet.getUpdatedAt().isAfter(originalUpdatedAt));
            assertEquals(newUpdatedAt, tweet.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("投稿者確認テスト")
    class IsAuthorTests {

        @Test
        @DisplayName("投稿者のユーザーIDに対してtrueを返す")
        void shouldReturnTrueForAuthor() {
            // Given
            UserId authorId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");
            Tweet tweet = Tweet.create(authorId, content);

            // When
            boolean result = tweet.isAuthor(authorId);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("投稿者以外のユーザーIDに対してfalseを返す")
        void shouldReturnFalseForNonAuthor() {
            // Given
            UserId authorId = UserId.generate();
            UserId otherUserId = UserId.generate();
            TweetContent content = new TweetContent("Test tweet");
            Tweet tweet = Tweet.create(authorId, content);

            // When
            boolean result = tweet.isAuthor(otherUserId);

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Equals/HashCode テスト")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("同じIDを持つツイートは等価である")
        void shouldBeEqualWithSameId() {
            // Given
            TweetId tweetId = TweetId.generate();
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();
            TweetContent content1 = new TweetContent("Content 1");
            TweetContent content2 = new TweetContent("Content 2");

            Tweet tweet1 = Tweet.reconstruct(tweetId, userId1, content1, false,
                java.time.Instant.now(), java.time.Instant.now());
            Tweet tweet2 = Tweet.reconstruct(tweetId, userId2, content2, false,
                java.time.Instant.now(), java.time.Instant.now());

            // When/Then
            assertEquals(tweet1, tweet2);
            assertEquals(tweet1.hashCode(), tweet2.hashCode());
        }

        @Test
        @DisplayName("異なるIDを持つツイートは等価でない")
        void shouldNotBeEqualWithDifferentId() {
            // Given
            UserId userId = UserId.generate();
            TweetContent content = new TweetContent("Test content");

            Tweet tweet1 = Tweet.create(userId, content);
            Tweet tweet2 = Tweet.create(userId, content);

            // When/Then
            assertNotEquals(tweet1, tweet2);
        }
    }
}
