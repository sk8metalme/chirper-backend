package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.exception.EntityNotFoundException;
import com.chirper.domain.repository.ILikeRepository;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTweetUseCaseTest {

    @Mock
    private ITweetRepository tweetRepository;

    @Mock
    private ILikeRepository likeRepository;

    @Mock
    private IRetweetRepository retweetRepository;

    @InjectMocks
    private GetTweetUseCase getTweetUseCase;

    private TweetId testTweetId;
    private UserId testUserId;
    private Tweet testTweet;

    @BeforeEach
    void setUp() {
        testTweetId = new TweetId(UUID.randomUUID());
        testUserId = UserId.of(UUID.randomUUID().toString());
        testTweet = Tweet.create(testUserId, new TweetContent("テストツイート"));
    }

    @Test
    @DisplayName("ツイート取得成功 - いいね・リツイートなし")
    void getTweet_success_noLikesOrRetweets() {
        // Arrange
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        when(likeRepository.findByTweetId(testTweetId)).thenReturn(List.of());
        when(retweetRepository.findByTweetId(testTweetId)).thenReturn(List.of());

        // Act
        GetTweetUseCase.TweetResult result = getTweetUseCase.execute(testTweetId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.tweet()).isEqualTo(testTweet);
        assertThat(result.likesCount()).isEqualTo(0);
        assertThat(result.retweetsCount()).isEqualTo(0);

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(likeRepository, times(1)).findByTweetId(testTweetId);
        verify(retweetRepository, times(1)).findByTweetId(testTweetId);
    }

    @Test
    @DisplayName("ツイート取得成功 - いいね・リツイートあり")
    void getTweet_success_withLikesAndRetweets() {
        // Arrange
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.of(testTweet));
        // サイズ5のリストを返す（実際のLikeエンティティは不要）
        when(likeRepository.findByTweetId(testTweetId)).thenReturn(List.of(
            mock(com.chirper.domain.entity.Like.class),
            mock(com.chirper.domain.entity.Like.class),
            mock(com.chirper.domain.entity.Like.class),
            mock(com.chirper.domain.entity.Like.class),
            mock(com.chirper.domain.entity.Like.class)
        ));
        // サイズ3のリストを返す
        when(retweetRepository.findByTweetId(testTweetId)).thenReturn(List.of(
            mock(com.chirper.domain.entity.Retweet.class),
            mock(com.chirper.domain.entity.Retweet.class),
            mock(com.chirper.domain.entity.Retweet.class)
        ));

        // Act
        GetTweetUseCase.TweetResult result = getTweetUseCase.execute(testTweetId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.tweet()).isEqualTo(testTweet);
        assertThat(result.likesCount()).isEqualTo(5);
        assertThat(result.retweetsCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("ツイート取得失敗 - ツイートが存在しない")
    void getTweet_failure_tweetNotFound() {
        // Arrange
        when(tweetRepository.findById(testTweetId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getTweetUseCase.execute(testTweetId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("ツイートが見つかりません");

        verify(tweetRepository, times(1)).findById(testTweetId);
        verify(likeRepository, never()).findByTweetId(any());
        verify(retweetRepository, never()).findByTweetId(any());
    }

    @Test
    @DisplayName("ツイート取得失敗 - tweetIdがnull")
    void getTweet_failure_nullTweetId() {
        // Act & Assert
        assertThatThrownBy(() -> getTweetUseCase.execute(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("TweetId cannot be null");

        verify(tweetRepository, never()).findById(any());
    }
}
