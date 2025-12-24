package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DeleteTweetUseCaseのテスト
 * TDD: Red - テストを先に書く
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteTweetUseCase単体テスト")
class DeleteTweetUseCaseTest {

    @Mock
    private ITweetRepository tweetRepository;

    private DeleteTweetUseCase deleteTweetUseCase;

    @BeforeEach
    void setUp() {
        deleteTweetUseCase = new DeleteTweetUseCase(tweetRepository);
    }

    @Test
    @DisplayName("正常系: 投稿者本人がツイートを削除できる")
    void shouldDeleteTweetSuccessfully() {
        // Arrange
        UserId userId = UserId.generate();
        TweetId tweetId = TweetId.generate();
        Tweet tweet = Tweet.create(userId, new TweetContent("Test tweet"));

        when(tweetRepository.findById(tweetId))
            .thenReturn(Optional.of(tweet));
        when(tweetRepository.save(any(Tweet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        deleteTweetUseCase.execute(tweetId, userId);

        // Assert
        assertThat(tweet.isDeleted()).isTrue();

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(tweetRepository, times(1)).findById(tweetId);
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ツイートが存在しない場合はエラー")
    void shouldThrowExceptionWhenTweetDoesNotExist() {
        // Arrange
        TweetId tweetId = TweetId.generate();
        UserId userId = UserId.generate();

        when(tweetRepository.findById(tweetId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deleteTweetUseCase.execute(tweetId, userId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tweet not found");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(tweetRepository, times(1)).findById(tweetId);
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: 投稿者以外がツイートを削除しようとした場合はエラー")
    void shouldThrowExceptionWhenUserIsNotAuthor() {
        // Arrange
        UserId authorUserId = UserId.generate();
        UserId otherUserId = UserId.generate();
        TweetId tweetId = TweetId.generate();
        Tweet tweet = Tweet.create(authorUserId, new TweetContent("Test tweet"));

        when(tweetRepository.findById(tweetId))
            .thenReturn(Optional.of(tweet));

        // Act & Assert
        assertThatThrownBy(() -> deleteTweetUseCase.execute(tweetId, otherUserId))
            .isInstanceOf(SecurityException.class)
            .hasMessageContaining("Only the tweet author can delete this tweet");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(tweetRepository, times(1)).findById(tweetId);
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: 既に削除済みのツイートを削除しようとした場合はエラー")
    void shouldThrowExceptionWhenTweetAlreadyDeleted() {
        // Arrange
        UserId userId = UserId.generate();
        TweetId tweetId = TweetId.generate();
        Tweet tweet = Tweet.create(userId, new TweetContent("Test tweet"));
        tweet.delete(userId); // 先に削除

        when(tweetRepository.findById(tweetId))
            .thenReturn(Optional.of(tweet));

        // Act & Assert
        assertThatThrownBy(() -> deleteTweetUseCase.execute(tweetId, userId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Tweet is already deleted");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(tweetRepository, times(1)).findById(tweetId);
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ツイートIDがnullの場合はエラー")
    void shouldThrowExceptionWhenTweetIdIsNull() {
        // Arrange
        TweetId tweetId = null;
        UserId userId = UserId.generate();

        // Act & Assert
        assertThatThrownBy(() -> deleteTweetUseCase.execute(tweetId, userId))
            .isInstanceOf(NullPointerException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(tweetRepository, never()).findById(any(TweetId.class));
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ユーザーIDがnullの場合はエラー")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Arrange
        TweetId tweetId = TweetId.generate();
        UserId userId = null;

        // Act & Assert
        assertThatThrownBy(() -> deleteTweetUseCase.execute(tweetId, userId))
            .isInstanceOf(NullPointerException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(tweetRepository, never()).findById(any(TweetId.class));
        verify(tweetRepository, never()).save(any(Tweet.class));
    }
}
