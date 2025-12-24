package com.chirper.application.usecase;

import com.chirper.domain.entity.Like;
import com.chirper.domain.repository.ILikeRepository;
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
@DisplayName("LikeTweetUseCase単体テスト")
class LikeTweetUseCaseTest {

    @Mock
    private ILikeRepository likeRepository;

    private LikeTweetUseCase likeTweetUseCase;

    @BeforeEach
    void setUp() {
        likeTweetUseCase = new LikeTweetUseCase(likeRepository);
    }

    @Test
    @DisplayName("正常系: ツイートにいいねできる")
    void shouldLikeTweetSuccessfully() {
        // Arrange
        UserId userId = UserId.generate();
        TweetId tweetId = TweetId.generate();

        when(likeRepository.findByUserIdAndTweetId(userId, tweetId))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any(Like.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        likeTweetUseCase.execute(userId, tweetId);

        // Assert
        verify(likeRepository, times(1)).findByUserIdAndTweetId(userId, tweetId);
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("異常系: 既にいいね済みの場合はエラー")
    void shouldThrowExceptionWhenAlreadyLiked() {
        // Arrange
        UserId userId = UserId.generate();
        TweetId tweetId = TweetId.generate();
        Like existingLike = Like.create(userId, tweetId);

        when(likeRepository.findByUserIdAndTweetId(userId, tweetId))
            .thenReturn(Optional.of(existingLike));

        // Act & Assert
        assertThatThrownBy(() -> likeTweetUseCase.execute(userId, tweetId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Already liked this tweet");

        verify(likeRepository, times(1)).findByUserIdAndTweetId(userId, tweetId);
        verify(likeRepository, never()).save(any(Like.class));
    }
}
