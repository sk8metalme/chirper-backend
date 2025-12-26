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
class GetFollowingUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IFollowRepository followRepository;

    private GetFollowingUseCase getFollowingUseCase;

    private Username testUsername;
    private User testUser;
    private UserId currentUserId;
    private User followingUser1;
    private User followingUser2;

    @BeforeEach
    void setUp() {
        getFollowingUseCase = new GetFollowingUseCase(
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

        followingUser1 = User.create(
            new Username("following1"),
            new Email("following1@example.com"),
            "hashedPassword"
        );

        followingUser2 = User.create(
            new Username("following2"),
            new Email("following2@example.com"),
            "hashedPassword"
        );
    }

    @Test
    @DisplayName("フォロー中一覧取得成功 - 認証あり")
    void getFollowing_success_withAuthentication() {
        // Arrange
        int page = 0;
        int size = 20;
        int offset = page * size;

        List<UserId> followingIds = List.of(followingUser1.getId(), followingUser2.getId());
        Map<UserId, User> followingUsersMap = Map.of(
            followingUser1.getId(), followingUser1,
            followingUser2.getId(), followingUser2
        );
        List<UserId> followedByCurrentUserIds = List.of(followingUser1.getId());

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowing(testUser.getId())).thenReturn(2L);
        when(followRepository.findFollowingUserIds(testUser.getId(), offset, size)).thenReturn(followingIds);
        when(userRepository.findByIds(followingIds)).thenReturn(followingUsersMap);
        when(followRepository.findFollowedUserIdsIn(currentUserId, followingIds)).thenReturn(followedByCurrentUserIds);

        // Act
        GetFollowingUseCase.FollowingResult result = getFollowingUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.following()).hasSize(2);
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(1);

        // 1人目のフォロー中ユーザー（currentUserもフォロー中）
        assertThat(result.following().get(0).user().getUsername().value()).isEqualTo("following1");
        assertThat(result.following().get(0).followedByCurrentUser()).isTrue();

        // 2人目のフォロー中ユーザー（currentUserはフォローしていない）
        assertThat(result.following().get(1).user().getUsername().value()).isEqualTo("following2");
        assertThat(result.following().get(1).followedByCurrentUser()).isFalse();

        // モックの検証
        verify(userRepository).findByUsername(testUsername);
        verify(followRepository).countFollowing(testUser.getId());
        verify(followRepository).findFollowingUserIds(testUser.getId(), offset, size);
        verify(userRepository).findByIds(followingIds);
        verify(followRepository).findFollowedUserIdsIn(currentUserId, followingIds);
    }

    @Test
    @DisplayName("フォロー中一覧取得成功 - 認証なし")
    void getFollowing_success_withoutAuthentication() {
        // Arrange
        int page = 0;
        int size = 20;
        int offset = page * size;

        List<UserId> followingIds = List.of(followingUser1.getId());
        Map<UserId, User> followingUsersMap = Map.of(followingUser1.getId(), followingUser1);

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowing(testUser.getId())).thenReturn(1L);
        when(followRepository.findFollowingUserIds(testUser.getId(), offset, size)).thenReturn(followingIds);
        when(userRepository.findByIds(followingIds)).thenReturn(followingUsersMap);

        // Act
        GetFollowingUseCase.FollowingResult result = getFollowingUseCase.execute(testUsername, null, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.following()).hasSize(1);
        assertThat(result.following().get(0).followedByCurrentUser()).isFalse();

        // 認証なしの場合はfindFollowedUserIdsInが呼ばれないことを確認
        verify(followRepository, never()).findFollowedUserIdsIn(any(), any());
    }

    @Test
    @DisplayName("フォロー中ユーザーが0人の場合")
    void getFollowing_emptyFollowing() {
        // Arrange
        int page = 0;
        int size = 20;
        int offset = page * size;

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowing(testUser.getId())).thenReturn(0L);
        when(followRepository.findFollowingUserIds(testUser.getId(), offset, size)).thenReturn(List.of());

        // Act
        GetFollowingUseCase.FollowingResult result = getFollowingUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.following()).isEmpty();
        assertThat(result.currentPage()).isEqualTo(0);
        assertThat(result.totalPages()).isEqualTo(0);

        // フォロー中が0人の場合はfindByIdsが呼ばれないことを確認
        verify(userRepository, never()).findByIds(any());
    }

    @Test
    @DisplayName("ページネーション - 2ページ目")
    void getFollowing_pagination_secondPage() {
        // Arrange
        int page = 1;
        int size = 20;
        int offset = page * size;

        List<UserId> followingIds = List.of(followingUser1.getId());
        Map<UserId, User> followingUsersMap = Map.of(followingUser1.getId(), followingUser1);

        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(followRepository.countFollowing(testUser.getId())).thenReturn(25L);
        when(followRepository.findFollowingUserIds(testUser.getId(), offset, size)).thenReturn(followingIds);
        when(userRepository.findByIds(followingIds)).thenReturn(followingUsersMap);
        when(followRepository.findFollowedUserIdsIn(currentUserId, followingIds)).thenReturn(List.of());

        // Act
        GetFollowingUseCase.FollowingResult result = getFollowingUseCase.execute(testUsername, currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.currentPage()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);  // 25 users / 20 per page = 2 pages
    }

    @Test
    @DisplayName("ユーザーが見つからない場合は例外をスロー")
    void getFollowing_userNotFound_throwsException() {
        // Arrange
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getFollowingUseCase.execute(testUsername, currentUserId, 0, 20))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("ユーザーが見つかりません");

        verify(userRepository).findByUsername(testUsername);
        verifyNoInteractions(followRepository);
    }

    @Test
    @DisplayName("usernameがnullの場合は例外をスロー")
    void getFollowing_nullUsername_throwsException() {
        // Act & Assert
        assertThatThrownBy(() -> getFollowingUseCase.execute(null, currentUserId, 0, 20))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Username cannot be null");

        verifyNoInteractions(userRepository);
        verifyNoInteractions(followRepository);
    }
}
