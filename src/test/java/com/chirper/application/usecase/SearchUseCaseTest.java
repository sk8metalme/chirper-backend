package com.chirper.application.usecase;

import com.chirper.domain.entity.Tweet;
import com.chirper.domain.entity.User;
import com.chirper.domain.repository.ITweetRepository;
import com.chirper.domain.exception.InvalidOperationException;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.exception.InvalidOperationException;
import com.chirper.domain.valueobject.Email;
import com.chirper.domain.valueobject.TweetContent;
import com.chirper.domain.valueobject.UserId;
import com.chirper.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchUseCase単体テスト")
class SearchUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private ITweetRepository tweetRepository;

    private SearchUseCase searchUseCase;

    @BeforeEach
    void setUp() {
        searchUseCase = new SearchUseCase(userRepository, tweetRepository);
    }

    @Test
    @DisplayName("正常系: キーワードでユーザーとツイートを検索できる")
    void shouldSearchSuccessfully() {
        // Arrange
        String keyword = "test";

        // Act
        SearchUseCase.SearchResult result = searchUseCase.execute(keyword, 0, 20);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.users()).isEmpty();
        assertThat(result.tweets()).isEmpty();
    }

    @Test
    @DisplayName("異常系: キーワードが2文字未満の場合はエラー")
    void shouldThrowExceptionWhenKeywordTooShort() {
        // Arrange
        String keyword = "a";

        // Act & Assert
        assertThatThrownBy(() -> searchUseCase.execute(keyword, 0, 20))
            .isInstanceOf(InvalidOperationException.class)
            .hasMessageContaining("検索キーワードは2文字以上で入力してください");
    }

    @Test
    @DisplayName("異常系: キーワードがnullの場合はエラー")
    void shouldThrowExceptionWhenKeywordIsNull() {
        // Arrange
        String keyword = null;

        // Act & Assert
        assertThatThrownBy(() -> searchUseCase.execute(keyword, 0, 20))
            .isInstanceOf(InvalidOperationException.class);
    }
}
