package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserProfileUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IFollowRepository followRepository;

    @Mock
    private ITweetRepository tweetRepository;

    private GetUserProfileUseCase getUserProfileUseCase;

    private Username testUsername;
    private User testUser;
    private UserId currentUserId;

    @BeforeEach
    void setUp() {
        getUserProfileUseCase = new GetUserProfileUseCase(
            userRepository,
            followRepository,
            tweetRepository
        );

        testUsername = new Username("testuser");
        testUser = User.create(
            testUsername,
            new Email("test@example.com"),
            "hashedPassword"
        );
        currentUserId = UserId.generate();
    }

    @Test
    @DisplayName("ユーザープロフィール取得成功")
    void getUserProfile_success() {
        // Arrange
        int page = 0;
        int size = 10;
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowers(testUser.getId())).thenReturn(10L);
        when(followRepository.countFollowing(testUser.getId())).thenReturn(5L);
        when(followRepository.existsByFollowerAndFollowed(currentUserId, testUser.getId())).thenReturn(false);
        when(tweetRepository.findByUserId(testUser.getId(), page, size)).thenReturn(List.of());

        // Act
        GetUserProfileUseCase.UserProfileResult result = getUserProfileUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.user().getUsername()).isEqualTo(testUsername);
        assertThat(result.user().getEmail().value()).isEqualTo("test@example.com");
        assertThat(result.followersCount()).isEqualTo(10L);
        assertThat(result.followingCount()).isEqualTo(5L);
        assertThat(result.followedByCurrentUser()).isFalse();

        verify(userRepository, times(1)).findByUsername(testUsername);
        verify(followRepository, times(1)).countFollowers(testUser.getId());
        verify(followRepository, times(1)).countFollowing(testUser.getId());
    }

    @Test
    @DisplayName("ユーザープロフィール取得失敗 - ユーザーが存在しない")
    void getUserProfile_failure_userNotFound() {
        // Arrange
        Username nonExistentUsername = new Username("nonexistent");
        int page = 0;
        int size = 10;
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getUserProfileUseCase.execute(nonExistentUsername, currentUserId, page, size))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("ユーザーが見つかりません: nonexistent");

        verify(userRepository, times(1)).findByUsername(nonExistentUsername);
    }

    @Test
    @DisplayName("ユーザープロフィール取得失敗 - usernameがnull")
    void getUserProfile_failure_nullUsername() {
        // Act & Assert
        assertThatThrownBy(() -> getUserProfileUseCase.execute(null, currentUserId, 0, 10))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Username cannot be null");

        verify(userRepository, never()).findByUsername(any());
    }
}
