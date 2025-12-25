package com.chirper.domain.service;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimelineService Tests")
class TimelineServiceTest {

    @Mock
    private ITweetRepository tweetRepository;

    private TimelineService timelineService;

    @BeforeEach
    void setUp() {
        timelineService = new TimelineService(tweetRepository);
    }

    @Nested
    @DisplayName("コンストラクタテスト")
    class ConstructorTests {

        @Test
        @DisplayName("正常なリポジトリでインスタンス化できる")
        void shouldCreateInstanceWithValidRepository() {
            // When/Then
            assertDoesNotThrow(() -> new TimelineService(tweetRepository));
        }

        @Test
        @DisplayName("nullのリポジトリで例外が発生する")
        void shouldThrowExceptionWithNullRepository() {
            // When/Then
            assertThrows(NullPointerException.class,
                () -> new TimelineService(null));
        }
    }

    @Nested
    @DisplayName("タイムライン取得テスト")
    class GetTimelineTests {

        @Test
        @DisplayName("正常にタイムラインを取得できる")
        void shouldGetTimelineSuccessfully() {
            // Given
            UserId userId1 = UserId.generate();
            UserId userId2 = UserId.generate();
            List<UserId> followedUserIds = Arrays.asList(userId1, userId2);
            int page = 0;
            int size = 20;

            Tweet tweet1 = Tweet.create(userId1, new TweetContent("Tweet 1"));
            Tweet tweet2 = Tweet.create(userId2, new TweetContent("Tweet 2"));
            List<Tweet> expectedTweets = Arrays.asList(tweet1, tweet2);

            when(tweetRepository.findByUserIdsWithDetails(followedUserIds, page, size))
                .thenReturn(expectedTweets);

            // When
            List<Tweet> result = timelineService.getTimeline(followedUserIds, page, size);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(expectedTweets, result);
            verify(tweetRepository).findByUserIdsWithDetails(followedUserIds, page, size);
        }

        @Test
        @DisplayName("フォローユーザーがいない場合は空のリストを返す")
        void shouldReturnEmptyListWhenNoFollowedUsers() {
            // Given
            List<UserId> emptyList = Collections.emptyList();
            int page = 0;
            int size = 20;

            // When
            List<Tweet> result = timelineService.getTimeline(emptyList, page, size);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(tweetRepository, never()).findByUserIdsWithDetails(anyList(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("nullのフォローユーザーリストの場合は空のリストを返す")
        void shouldReturnEmptyListWhenFollowedUsersIsNull() {
            // Given
            int page = 0;
            int size = 20;

            // When
            List<Tweet> result = timelineService.getTimeline(null, page, size);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(tweetRepository, never()).findByUserIdsWithDetails(anyList(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("負のページ番号で例外が発生する")
        void shouldThrowExceptionWithNegativePage() {
            // Given
            List<UserId> followedUserIds = Arrays.asList(UserId.generate());
            int page = -1;
            int size = 20;

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> timelineService.getTimeline(followedUserIds, page, size));
        }

        @Test
        @DisplayName("ページサイズが0で例外が発生する")
        void shouldThrowExceptionWithZeroSize() {
            // Given
            List<UserId> followedUserIds = Arrays.asList(UserId.generate());
            int page = 0;
            int size = 0;

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> timelineService.getTimeline(followedUserIds, page, size));
        }

        @Test
        @DisplayName("ページサイズが100を超えると例外が発生する")
        void shouldThrowExceptionWithSizeOverLimit() {
            // Given
            List<UserId> followedUserIds = Arrays.asList(UserId.generate());
            int page = 0;
            int size = 101;

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> timelineService.getTimeline(followedUserIds, page, size));
        }
    }

    @Nested
    @DisplayName("総ページ数計算テスト")
    class CalculateTotalPagesTests {

        @Test
        @DisplayName("calculateTotalPagesは未実装のため例外が発生する")
        void shouldThrowUnsupportedOperationException() {
            // Given
            List<UserId> followedUserIds = Arrays.asList(UserId.generate());
            int size = 20;

            // When/Then
            assertThrows(UnsupportedOperationException.class,
                () -> timelineService.calculateTotalPages(followedUserIds, size));
        }

        @Test
        @DisplayName("空のフォローユーザーリストで0を返す")
        void shouldReturnZeroForEmptyFollowedUsers() {
            // Given
            List<UserId> emptyList = Collections.emptyList();
            int size = 20;

            // When
            int result = timelineService.calculateTotalPages(emptyList, size);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("nullのフォローユーザーリストで0を返す")
        void shouldReturnZeroForNullFollowedUsers() {
            // Given
            int size = 20;

            // When
            int result = timelineService.calculateTotalPages(null, size);

            // Then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("負のページサイズで例外が発生する")
        void shouldThrowExceptionWithNonPositiveSize() {
            // Given
            List<UserId> followedUserIds = Arrays.asList(UserId.generate());
            int size = 0;

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> timelineService.calculateTotalPages(followedUserIds, size));
        }
    }
}
