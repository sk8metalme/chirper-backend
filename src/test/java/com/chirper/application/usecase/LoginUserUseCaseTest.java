package com.chirper.application.usecase;

import com.chirper.domain.entity.User;
import com.chirper.domain.exception.UnauthorizedAccessException;
import com.chirper.domain.repository.IUserRepository;
import com.chirper.domain.service.AuthenticationService;
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
 * LoginUserUseCaseのテスト
 * TDD: Red - テストを先に書く
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase単体テスト")
class LoginUserUseCaseTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    private LoginUserUseCase loginUserUseCase;

    @BeforeEach
    void setUp() {
        loginUserUseCase = new LoginUserUseCase(userRepository, authenticationService);
    }

    @Test
    @DisplayName("正常系: 認証成功時にJWTトークンとユーザー情報を返す")
    void shouldReturnJwtTokenAndUserInfoWhenAuthenticationSucceeds() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String expectedToken = "jwt.token.here";

        User user = User.create(
            new Username(username),
            new Email("test@example.com"),
            password
        );

        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.of(user));
        when(authenticationService.authenticate(any(User.class), eq(password)))
            .thenReturn(true);
        when(authenticationService.generateJwtToken(any(UserId.class)))
            .thenReturn(expectedToken);

        // Act
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(username, password);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(expectedToken);
        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.username()).isEqualTo(username);

        // リポジトリとサービスのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(authenticationService, times(1)).authenticate(any(User.class), eq(password));
        verify(authenticationService, times(1)).generateJwtToken(any(UserId.class));
    }

    @Test
    @DisplayName("異常系: ユーザーが存在しない場合は認証失敗エラー")
    void shouldThrowExceptionWhenUserDoesNotExist() {
        // Arrange
        String username = "nonexistentuser";
        String password = "password123";

        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(username, password))
            .isInstanceOf(UnauthorizedAccessException.class)
            .hasMessageContaining("認証に失敗しました");

        // リポジトリのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(authenticationService, never()).authenticate(any(User.class), anyString());
        verify(authenticationService, never()).generateJwtToken(any(UserId.class));
    }

    @Test
    @DisplayName("異常系: パスワードが一致しない場合は認証失敗エラー")
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        User user = User.create(
            new Username(username),
            new Email("test@example.com"),
            correctPassword
        );

        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.of(user));
        when(authenticationService.authenticate(any(User.class), eq(wrongPassword)))
            .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(username, wrongPassword))
            .isInstanceOf(UnauthorizedAccessException.class)
            .hasMessageContaining("認証に失敗しました");

        // リポジトリとサービスのメソッドが正しく呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
        verify(authenticationService, times(1)).authenticate(any(User.class), eq(wrongPassword));
        verify(authenticationService, never()).generateJwtToken(any(UserId.class));
    }

    @Test
    @DisplayName("異常系: ユーザー名がnullの場合はエラー")
    void shouldThrowExceptionWhenUsernameIsNull() {
        // Arrange
        String username = null;
        String password = "password123";

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(username, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username cannot be null or blank");

        // リポジトリのメソッドが呼ばれないことを確認
        verify(userRepository, never()).findByUsername(any(Username.class));
        verify(authenticationService, never()).authenticate(any(User.class), anyString());
    }

    @Test
    @DisplayName("異常系: パスワードがnullの場合はエラー")
    void shouldThrowExceptionWhenPasswordIsNull() {
        // Arrange
        String username = "testuser";
        String password = null;

        User user = User.create(
            new Username(username),
            new Email("test@example.com"),
            "password123"
        );

        when(userRepository.findByUsername(any(Username.class)))
            .thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(username, password))
            .isInstanceOf(UnauthorizedAccessException.class);

        // リポジトリのメソッドが呼ばれたことを確認
        verify(userRepository, times(1)).findByUsername(any(Username.class));
    }
}
