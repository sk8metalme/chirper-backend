package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.valueobject.Email;
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
 * RegisterUserUseCaseのテスト
 * TDD: Red - テストを先に書く
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase単体テスト")
class RegisterUserUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(userRepository);
    }

    @Test
    @DisplayName("正常系: 新規ユーザーを登録できる")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        // ユーザー名の重複チェック: 存在しない
        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.empty());

        // メールアドレスの重複チェック: 存在しない
        when(userRepository.findByEmail(any(Email.class)))
            .thenReturn(Optional.empty());

        // ユーザー保存時のモック
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = registerUserUseCase.execute(username, email, password);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername().value()).isEqualTo(username);
        assertThat(result.getEmail().value()).isEqualTo(email);
        assertThat(result.getId()).isNotNull();

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: ユーザー名が既に存在する場合はエラー")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Arrange
        String username = "existinguser";
        String email = "test@example.com";
        String password = "password123";

        User existingUser = User.create(
            new Username(username),
            new Email("existing@example.com"),
            "password"
        );

        // ユーザー名の重複チェック: 存在する
        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(username, email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already exists");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: メールアドレスが既に存在する場合はエラー")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        String username = "testuser";
        String email = "existing@example.com";
        String password = "password123";

        User existingUser = User.create(
            new Username("existinguser"),
            new Email(email),
            "password"
        );

        // ユーザー名の重複チェック: 存在しない
        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.empty());

        // メールアドレスの重複チェック: 存在する
        when(userRepository.findByEmail(any(Email.class)))
            .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(username, email, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: ユーザー名がnullの場合はエラー")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // Arrange
        String username = null;
        String email = "test@example.com";
        String password = "password123";

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(username, email, password))
            .isInstanceOf(IllegalArgumentException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(userRepository, never()).findByUsername(any(Username.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: メールアドレスが不正な形式の場合はエラー")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Arrange
        String username = "testuser";
        String email = "invalid-email";
        String password = "password123";

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(username, email, password))
            .isInstanceOf(IllegalArgumentException.class);

        // リポジトリのメソッドが呼ばれないことを確認
        verify(userRepository, never()).findByUsername(any(Username.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("異常系: パスワードがnullの場合はエラー")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = null;

        // ユーザー名の重複チェック: 存在しない
        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.empty());

        // メールアドレスの重複チェック: 存在しない
        when(userRepository.findByEmail(any(Email.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(username, email, password))
            .isInstanceOf(IllegalArgumentException.class);

        // リポジトリのメソッドが呼ばれたことを確認（バリデーションは後でチェック）
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
