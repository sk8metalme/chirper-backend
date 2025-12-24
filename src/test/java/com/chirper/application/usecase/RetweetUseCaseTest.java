package com.chirper.application.usecase;

import com.chirper.domain.entity.Retweet;
import com.chirper.domain.repository.IRetweetRepository;
import com.chirper.domain.valueobject.TweetId;
import com.chirper.domain.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetweetUseCase単体テスト")
class RetweetUseCaseTest {

    @Mock
    private IRetweetRepository retweetRepository;

    private RetweetUseCase retweetUseCase;

    @BeforeEach
    void setUp() {
        retweetUseCase = new RetweetUseCase(retweetRepository);
    }

    @Test
    @DisplayName("正常系: ツイートをリツイートできる")
    void shouldRetweetSuccessfully() {
        // Arrange
        UserId userId = UserId.generate();
        TweetId tweetId = TweetId.generate();

        when(retweetRepository.findByUserIdAndTweetId(userId, tweetId))
            .thenReturn(Optional.empty());
        when(retweetRepository.save(any(Retweet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        retweetUseCase.execute(userId, tweetId);

        // Assert
        verify(retweetRepository, times(1)).findByUserIdAndTweetId(userId, tweetId);
        verify(retweetRepository, times(1)).save(any(Retweet.class));
    }

    @Test
    @DisplayName("異常系: 既にリツイート済みの場合はエラー")
    void shouldThrowExceptionWhenAlreadyRetweeted() {
        // Arrange
        UserId userId = UserId.generate();
        TweetId tweetId = TweetId.generate();
        Retweet existingRetweet = Retweet.create(userId, tweetId);

        when(retweetRepository.findByUserIdAndTweetId(userId, tweetId))
            .thenReturn(Optional.of(existingRetweet));

        // Act & Assert
        assertThatThrownBy(() -> retweetUseCase.execute(userId, tweetId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Already retweeted this tweet");

        verify(retweetRepository, times(1)).findByUserIdAndTweetId(userId, tweetId);
        verify(retweetRepository, never()).save(any(Retweet.class));
    }
}
