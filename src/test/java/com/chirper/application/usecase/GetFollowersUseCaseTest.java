package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.IFollowRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFollowersUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IFollowRepository followRepository;

    private GetFollowersUseCase getFollowersUseCase;

    private Username testUsername;
    private User testUser;
    private UserId currentUserId;
    private User followerUser1;
    private User followerUser2;

    @BeforeEach
    void setUp() {
        getFollowersUseCase = new GetFollowersUseCase(
            userRepository,
            followRepository
        );

        testUsername = new Username("targetuser");
        testUser = User.create(
            testUsername,
            new Email("target@example.com"),
            "hashedPassword"
        );

        currentUserId = UserId.generate();

        followerUser1 = User.create(
            new Username("follower1"),
            new Email("follower1@example.com"),
            "hashedPassword"
        );

        followerUser2 = User.create(
            new Username("follower2"),
            new Email("follower2@example.com"),
            "hashedPassword"
        );
    }

    @Test
    @DisplayName("フォロワー一覧取得成功 - 認証あり")
    void getFollowers_success_withAuthentication() {
        // Arrange
        int page = 0;
        int size = 20;
        int offset = page * size;

        List<UserId> followerIds = List.of(followerUser1.getId(), followerUser2.getId());
        Map<UserId, User> followerUsersMap = Map.of(
            followerUser1.getId(), followerUser1,
            followerUser2.getId(), followerUser2
        );
        List<UserId> followedByCurrentUserIds = List.of(followerUser1.getId());

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowers(testUser.getId())).thenReturn(2L);
        when(followRepository.findFollowerUserIds(testUser.getId(), offset, size)).thenReturn(followerIds);
        when(userRepository.findByIds(followerIds)).thenReturn(followerUsersMap);
        when(followRepository.findFollowedUserIdsIn(currentUserId, followerIds)).thenReturn(followedByCurrentUserIds);

        // Act
        GetFollowersUseCase.FollowersResult result = getFollowersUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.followers()).hasSize(2);
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(1);

        // 1人目のフォロワー（currentUserがフォロー中）
        assertThat(result.followers().get(0).user().getUsername().value()).isEqualTo("follower1");
        assertThat(result.followers().get(0).followedByCurrentUser()).isTrue();

        // 2人目のフォロワー（currentUserがフォローしていない）
        assertThat(result.followers().get(1).user().getUsername().value()).isEqualTo("follower2");
        assertThat(result.followers().get(1).followedByCurrentUser()).isFalse();

        // モックの検証
        verify(userRepository).findByUsername(testUsername);
        verify(followRepository).countFollowers(testUser.getId());
        verify(followRepository).findFollowerUserIds(testUser.getId(), offset, size);
        verify(userRepository).findByIds(followerIds);
        verify(followRepository).findFollowedUserIdsIn(currentUserId, followerIds);
    }

    @Test
    @DisplayName("フォロワー一覧取得成功 - 認証なし")
    void getFollowers_success_withoutAuthentication() {
        // Arrange
        int page = 0;
        int size = 20;
        int offset = page * size;

        List<UserId> followerIds = List.of(followerUser1.getId());
        Map<UserId, User> followerUsersMap = Map.of(followerUser1.getId(), followerUser1);

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowers(testUser.getId())).thenReturn(1L);
        when(followRepository.findFollowerUserIds(testUser.getId(), offset, size)).thenReturn(followerIds);
        when(userRepository.findByIds(followerIds)).thenReturn(followerUsersMap);

        // Act
        GetFollowersUseCase.FollowersResult result = getFollowersUseCase.execute(testUsername, null, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.followers()).hasSize(1);
        assertThat(result.followers().get(0).followedByCurrentUser()).isFalse();

        // 認証なしの場合はfindFollowedUserIdsInが呼ばれないことを確認
        verify(followRepository, never()).findFollowedUserIdsIn(any(), any());
    }

    @Test
    @DisplayName("フォロワーが0人の場合")
    void getFollowers_emptyFollowers() {
        // Arrange
        int page = 0;
        int size = 20;
        int offset = page * size;

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowers(testUser.getId())).thenReturn(0L);
        when(followRepository.findFollowerUserIds(testUser.getId(), offset, size)).thenReturn(List.of());

        // Act
        GetFollowersUseCase.FollowersResult result = getFollowersUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.followers()).isEmpty();
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);

        // フォロワーが0人の場合はfindByIdsが呼ばれないことを確認
        verify(userRepository, never()).findByIds(any());
    }

    @Test
    @DisplayName("ページネーション - 2ページ目")
    void getFollowers_pagination_secondPage() {
        // Arrange
        int page = 1;
        int size = 20;
        int offset = page * size;

        List<UserId> followerIds = List.of(followerUser1.getId());
        Map<UserId, User> followerUsersMap = Map.of(followerUser1.getId(), followerUser1);

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowers(testUser.getId())).thenReturn(25L);
        when(followRepository.findFollowerUserIds(testUser.getId(), offset, size)).thenReturn(followerIds);
        when(userRepository.findByIds(followerIds)).thenReturn(followerUsersMap);
        when(followRepository.findFollowedUserIdsIn(currentUserId, followerIds)).thenReturn(List.of());

        // Act
        GetFollowersUseCase.FollowersResult result = getFollowersUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.currentPage()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);  // 25 users / 20 per page = 2 pages
    }

    @Test
    @DisplayName("ユーザーが見つからない場合は例外をスロー")
    void getFollowers_userNotFound_throwsException() {
        // Arrange
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getFollowersUseCase.execute(testUsername, currentUserId, 0, 20))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("ユーザーが見つかりません");

        verify(userRepository).findByUsername(testUsername);
        verifyNoInteractions(followRepository);
    }

    @Test
    @DisplayName("usernameがnullの場合は例外をスロー")
    void getFollowers_nullUsername_throwsException() {
        // Act & Assert
        assertThatThrownBy(() -> getFollowersUseCase.execute(null, currentUserId, 0, 20))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Username cannot be null");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(followRepository);
    }
}
