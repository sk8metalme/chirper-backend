package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IFollowRepository;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.service.TimelineService;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTimelineUseCase単体テスト")
class GetTimelineUseCaseTest {

    @Mock
    private IFollowRepository followRepository;

    @Mock
    private TimelineService timelineService;

    @Mock
    private ILikeRepository likeRepository;

    @Mock
    private IRetweetRepository retweetRepository;

    @Mock
    private IUserRepository userRepository;

    private GetTimelineUseCase getTimelineUseCase;

    @BeforeEach
    void setUp() {
        getTimelineUseCase = new GetTimelineUseCase(
            followRepository, timelineService, likeRepository, retweetRepository, userRepository
        );
    }

    @Test
    @DisplayName("正常系: タイムラインを取得できる")
    void shouldGetTimelineSuccessfully() {
        // Arrange
        UserId currentUserId = UserId.generate();
        UserId followedUserId = UserId.generate();
        int page = 0;
        int size = 20;

        Tweet tweet = Tweet.create(followedUserId, new TweetContent("Test tweet"));
        User author = User.create(new Username("testuser"), new Email("test@example.com"), "password");

        when(followRepository.findFollowedUserIds(currentUserId))
            .thenReturn(List.of(followedUserId));
        when(timelineService.getTimeline(anyList(), eq(page), eq(size)))
            .thenReturn(List.of(tweet));
        when(userRepository.findByIds(anyList()))
            .thenReturn(Map.of(followedUserId, author));
        when(likeRepository.countByTweetIds(anyList()))
            .thenReturn(Map.of(tweet.getId(), 5L));
        when(retweetRepository.countByTweetIds(anyList()))
            .thenReturn(Map.of(tweet.getId(), 3L));
        when(likeRepository.findByUserIdAndTweetId(currentUserId, tweet.getId()))
            .thenReturn(Optional.empty());
        when(retweetRepository.findByUserIdAndTweetId(currentUserId, tweet.getId()))
            .thenReturn(Optional.empty());

        // Act
        GetTimelineUseCase.TimelineResult result = getTimelineUseCase.execute(currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.tweets()).hasSize(1);
        assertThat(result.tweets().get(0).tweet()).isEqualTo(tweet);
        assertThat(result.tweets().get(0).author()).isEqualTo(author);
        assertThat(result.tweets().get(0).likesCount()).isEqualTo(5L);
        assertThat(result.tweets().get(0).retweetsCount()).isEqualTo(3L);
        assertThat(result.tweets().get(0).likedByCurrentUser()).isFalse();
        assertThat(result.tweets().get(0).retweetedByCurrentUser()).isFalse();

        verify(followRepository, times(1)).findFollowedUserIds(currentUserId);
        verify(timelineService, times(1)).getTimeline(anyList(), eq(page), eq(size));
        verify(userRepository, times(1)).findByIds(anyList());
        verify(likeRepository, times(1)).countByTweetIds(anyList());
        verify(retweetRepository, times(1)).countByTweetIds(anyList());
    }

    @Test
    @DisplayName("正常系: フォローユーザーが0件の場合は空のタイムラインを返す")
    void shouldReturnEmptyTimelineWhenNoFollowedUsers() {
        // Arrange
        UserId currentUserId = UserId.generate();
        int page = 0;
        int size = 20;

        when(followRepository.findFollowedUserIds(currentUserId))
            .thenReturn(List.of());

        // Act
        GetTimelineUseCase.TimelineResult result = getTimelineUseCase.execute(currentUserId, page, size);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.tweets()).isEmpty();

        verify(followRepository, times(1)).findFollowedUserIds(currentUserId);
        verify(timelineService, never()).getTimeline(anyList(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系: ユーザーIDがnullの場合はエラー")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Arrange
        UserId userId = null;
        int page = 0;
        int size = 20;

        // Act & Assert
        assertThatThrownBy(() -> getTimelineUseCase.execute(userId, page, size))
            .isInstanceOf(NullPointerException.class);

        verify(followRepository, never()).findFollowedUserIds(any(UserId.class));
    }
}
