package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpdateProfileUseCaseのテスト
 * TDD: Red - テストを先に書く
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProfileUseCase単体テスト")
class UpdateProfileUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    private UpdateProfileUseCase updateProfileUseCase;

    @BeforeEach
    void setUp() {
        updateProfileUseCase = new UpdateProfileUseCase(userRepository);
    }

    @Test
    @DisplayName("正常系: プロフィール情報を更新できる")
    void shouldUpdateProfileSuccessfully() {
        // Arrange
        UserId userId = UserId.generate();
        String displayName = "New Display Name";
        String bio = "New bio";
        String avatarUrl = "https://example.com/avatar.jpg";

        User user = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            "password123"
        );

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = updateProfileUseCase.execute(userId, displayName, bio, avatarUrl);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo(displayName);
        assertThat(result.getBio()).isEqualTo(bio);
        assertThat(result.getAvatarUrl()).isEqualTo(avatarUrl);

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("正常系: 一部のプロフィール情報のみを更新できる")
    void shouldUpdatePartialProfileSuccessfully() {
        // Arrange
        UserId userId = UserId.generate();
        String displayName = "New Display Name";

        User user = User.create(
            new Username("testuser"),
            new Email("test@example.com"),
            "password123"
        );

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = updateProfileUseCase.execute(userId, displayName, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo(displayName);
        assertThat(result.getBio()).isNull();
        assertThat(result.getAvatarUrl()).isNull();

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: ユーザーが存在しない場合はエラー")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        UserId userId = UserId.generate();
        String displayName = "New Display Name";

        when(userRepository.findById(userId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateProfileUseCase.execute(userId, displayName, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: ユーザーIDがnullの場合はエラー")
    void shouldThrowExceptionWhenUserIdIsNull() {
        // Arrange
        UserId userId = null;
        String displayName = "New Display Name";

        // Act & Assert
        assertThatThrownBy(() -> updateProfileUseCase.execute(userId, displayName, null, null))
            .isInstanceOf(NullPointerException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(userRepository, never()).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
