package com.chirper.application.usecase;

import com.chirper.domain.entity.Follow;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.service.FollowService;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUserUseCase単体テスト")
class FollowUserUseCaseTest {

    @Mock
    private IFollowRepository followRepository;

    @Mock
    private FollowService followService;

    private FollowUserUseCase followUserUseCase;

    @BeforeEach
    void setUp() {
        followUserUseCase = new FollowUserUseCase(followRepository, followService);
    }

    @Test
    @DisplayName("正常系: ユーザーをフォローできる")
    void shouldFollowUserSuccessfully() {
        // Arrange
        UserId followerUserId = UserId.generate();
        UserId followedUserId = UserId.generate();

        doNothing().when(followService).validateFollow(followerUserId, followedUserId);
        when(followRepository.save(any(Follow.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        followUserUseCase.execute(followerUserId, followedUserId);

        // Assert
        verify(followService, times(1)).validateFollow(followerUserId, followedUserId);
        verify(followRepository, times(1)).save(any(Follow.class));
    }

    @Test
    @DisplayName("異常系: 自分自身をフォローしようとした場合はエラー")
    void shouldThrowExceptionWhenTryingToFollowSelf() {
        // Arrange
        UserId userId = UserId.generate();

        doThrow(new IllegalArgumentException("User cannot follow themselves"))
            .when(followService).validateFollow(userId, userId);

        // Act & Assert
        assertThatThrownBy(() -> followUserUseCase.execute(userId, userId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User cannot follow themselves");

        verify(followService, times(1)).validateFollow(userId, userId);
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("異常系: 既にフォロー済みの場合はエラー")
    void shouldThrowExceptionWhenAlreadyFollowing() {
        // Arrange
        UserId followerUserId = UserId.generate();
        UserId followedUserId = UserId.generate();

        doThrow(new IllegalStateException("Already following this user"))
            .when(followService).validateFollow(followerUserId, followedUserId);

        // Act & Assert
        assertThatThrownBy(() -> followUserUseCase.execute(followerUserId, followedUserId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Already following this user");

        verify(followService, times(1)).validateFollow(followerUserId, followedUserId);
        verify(followRepository, never()).save(any(Follow.class));
    }
}
