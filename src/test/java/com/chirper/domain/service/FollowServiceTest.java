package com.chirper.domain.service;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.valueobject.FollowId;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService Tests")
class FollowServiceTest {

    @Mock
    private IFollowRepository followRepository;

    private FollowService followService;

    @BeforeEach
    void setUp() {
        followService = new FollowService(followRepository);
    }

    @Nested
    @DisplayName("コンストラクタテスト")
    class ConstructorTests {

        @Test
        @DisplayName("正常なリポジトリでインスタンス化できる")
        void shouldCreateInstanceWithValidRepository() {
            // When/Then
            assertDoesNotThrow(() -> new FollowService(followRepository));
        }

        @Test
        @DisplayName("nullのリポジトリで例外が発生する")
        void shouldThrowExceptionWithNullRepository() {
            // When/Then
            assertThrows(NullPointerException.class,
                () -> new FollowService(null));
        }
    }

    @Nested
    @DisplayName("フォロー検証テスト")
    class ValidateFollowTests {

        @Test
        @DisplayName("正常なフォローリクエストは検証を通過する")
        void shouldPassValidationForValidFollowRequest() {
            // Given
            UserId followerUserId = UserId.generate();
            UserId followedUserId = UserId.generate();

            when(followRepository.findByFollowerAndFollowed(followerUserId, followedUserId))
                .thenReturn(Optional.empty());

            // When/Then
            assertDoesNotThrow(() ->
                followService.validateFollow(followerUserId, followedUserId));
            verify(followRepository).findByFollowerAndFollowed(followerUserId, followedUserId);
        }

        @Test
        @DisplayName("自分自身をフォローしようとすると例外が発生する")
        void shouldThrowExceptionWhenFollowingSelf() {
            // Given
            UserId userId = UserId.generate();

            // When/Then
            assertThrows(IllegalArgumentException.class,
                () -> followService.validateFollow(userId, userId));
            verify(followRepository, never()).findByFollowerAndFollowed(any(), any());
        }

        @Test
        @DisplayName("既にフォロー済みの場合は例外が発生する")
        void shouldThrowExceptionWhenAlreadyFollowing() {
            // Given
            UserId followerUserId = UserId.generate();
            UserId followedUserId = UserId.generate();
            Follow existingFollow = Follow.create(followerUserId, followedUserId);

            when(followRepository.findByFollowerAndFollowed(followerUserId, followedUserId))
                .thenReturn(Optional.of(existingFollow));

            // When/Then
            assertThrows(IllegalStateException.class,
                () -> followService.validateFollow(followerUserId, followedUserId));
            verify(followRepository).findByFollowerAndFollowed(followerUserId, followedUserId);
        }
    }

    @Nested
    @DisplayName("フォロー解除検証テスト")
    class ValidateUnfollowTests {

        @Test
        @DisplayName("正常なフォロー解除リクエストは検証を通過する")
        void shouldPassValidationForValidUnfollowRequest() {
            // Given
            UserId followerUserId = UserId.generate();
            UserId followedUserId = UserId.generate();
            Follow existingFollow = Follow.create(followerUserId, followedUserId);

            when(followRepository.findByFollowerAndFollowed(followerUserId, followedUserId))
                .thenReturn(Optional.of(existingFollow));

            // When/Then
            assertDoesNotThrow(() ->
                followService.validateUnfollow(followerUserId, followedUserId));
            verify(followRepository).findByFollowerAndFollowed(followerUserId, followedUserId);
        }

        @Test
        @DisplayName("フォローしていないユーザーを解除しようとすると例外が発生する")
        void shouldThrowExceptionWhenNotFollowing() {
            // Given
            UserId followerUserId = UserId.generate();
            UserId followedUserId = UserId.generate();

            when(followRepository.findByFollowerAndFollowed(followerUserId, followedUserId))
                .thenReturn(Optional.empty());

            // When/Then
            assertThrows(IllegalStateException.class,
                () -> followService.validateUnfollow(followerUserId, followedUserId));
            verify(followRepository).findByFollowerAndFollowed(followerUserId, followedUserId);
        }
    }

    @Nested
    @DisplayName("フォロー済みチェックテスト")
    class IsFollowingTests {

        @Test
        @DisplayName("フォロー済みの場合はtrueを返す")
        void shouldReturnTrueWhenFollowing() {
            // Given
            UserId followerUserId = UserId.generate();
            UserId followedUserId = UserId.generate();
            Follow existingFollow = Follow.create(followerUserId, followedUserId);

            when(followRepository.findByFollowerAndFollowed(followerUserId, followedUserId))
                .thenReturn(Optional.of(existingFollow));

            // When
            boolean result = followService.isFollowing(followerUserId, followedUserId);

            // Then
            assertTrue(result);
            verify(followRepository).findByFollowerAndFollowed(followerUserId, followedUserId);
        }

        @Test
        @DisplayName("フォローしていない場合はfalseを返す")
        void shouldReturnFalseWhenNotFollowing() {
            // Given
            UserId followerUserId = UserId.generate();
            UserId followedUserId = UserId.generate();

            when(followRepository.findByFollowerAndFollowed(followerUserId, followedUserId))
                .thenReturn(Optional.empty());

            // When
            boolean result = followService.isFollowing(followerUserId, followedUserId);

            // Then
            assertFalse(result);
            verify(followRepository).findByFollowerAndFollowed(followerUserId, followedUserId);
        }
    }
}
