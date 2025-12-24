package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreateTweetUseCaseのテスト
 * TDD: Red - テストを先に書く
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTweetUseCase単体テスト")
class CreateTweetUseCaseTest {

    @Mock
    private ITweetRepository tweetRepository;

    private CreateTweetUseCase createTweetUseCase;

    @BeforeEach
    void setUp() {
        createTweetUseCase = new CreateTweetUseCase(tweetRepository);
    }

    @Test
    @DisplayName("正常系: 新規ツイートを投稿できる")
    void shouldCreateTweetSuccessfully() {
        // Arrange
        UserId userId = UserId.generate();
        String content = "This is a test tweet";

        when(tweetRepository.save(any(Tweet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tweet result = createTweetUseCase.execute(userId, content);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getContent().value()).isEqualTo(content);
        assertThat(result.isDeleted()).isFalse();

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("正常系: 280文字のツイートを投稿できる")
    void shouldCreateTweetWithMaxLengthContent() {
        // Arrange
        UserId userId = UserId.generate();
        String content = "a".repeat(280);

        when(tweetRepository.save(any(Tweet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Tweet result = createTweetUseCase.execute(userId, content);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent().value()).hasSize(280);

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(tweetRepository, times(1)).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ツイート本文が280文字を超える場合はエラー")
    void shouldThrowExceptionWhenContentExceedsMaxLength() {
        // Arrange
        UserId userId = UserId.generate();
        String content = "a".repeat(281);

        // Act & Assert
        assertThatThrownBy(() -> createTweetUseCase.execute(userId, content))
            .isInstanceOf(IllegalArgumentException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ツイート本文が空の場合はエラー")
    void shouldThrowExceptionWhenContentIsEmpty() {
        // Arrange
        UserId userId = UserId.generate();
        String content = "";

        // Act & Assert
        assertThatThrownBy(() -> createTweetUseCase.execute(userId, content))
            .isInstanceOf(IllegalArgumentException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ユーザーIDがnullの場合はエラー")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Arrange
        UserId userId = null;
        String content = "Test tweet";

        // Act & Assert
        assertThatThrownBy(() -> createTweetUseCase.execute(userId, content))
            .isInstanceOf(NullPointerException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(tweetRepository, never()).save(any(Tweet.class));
    }

    @Test
    @DisplayName("異常系: ツイート本文がnullの場合はエラー")
    void shouldThrowExceptionWhenContentIsNull() {
        // Arrange
        UserId userId = UserId.generate();
        String content = null;

        // Act & Assert
        assertThatThrownBy(() -> createTweetUseCase.execute(userId, content))
            .isInstanceOf(IllegalArgumentException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(tweetRepository, never()).save(any(Tweet.class));
    }
}
